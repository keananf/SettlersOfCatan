package client;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.players.Player;
import grid.BoardElement;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.Requests;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class which determines move possibilities based upon a game state
 * @author 140001596
 */
public class MoveProcessor
{
    private final Client client;
    private final TurnInProgress turn;
    private final ConcurrentLinkedQueue<Requests.Request.BodyCase> expectedMoves;

    public MoveProcessor(Client client)
    {
        this.turn = client.getTurn();
        expectedMoves = turn.getExpectedMoves();
        this.client = client;
    }

    /**
     * Retrieve a list of all building possibilities
     * @return
     */
    public List<BoardElement> getBuildingPossibilities()
    {
        List<BoardElement> moves = new ArrayList<BoardElement>();

        // This player
        Player p = getGame().getPlayer();

        // For each node
        for(Node n : getGame().getGrid().nodes.values())
        {
            if(p.canBuildSettlement(n))
            {
                moves.add(n);
            }
            else if(p.canBuildCity(n))
            {
                moves.add(n);
            }
        }

        // For each edge
        for(Edge e : getGame().getGrid().getEdgesAsList())
        {
            if(p.canBuildRoad(e))
            {
                moves.add(e);
            }
        }

        return moves;
    }

    /**
     * Processes a turn and ascertains all possible moves
     * @param turn the turn
     * @return list of possible choices from this proposed turn
     */
    public List<Requests.Request.BodyCase> getPossibleMoves(TurnInProgress turn)
    {
        List<Requests.Request.BodyCase> possibilities = new ArrayList<Requests.Request.BodyCase>();

        if(checkBuyDevCard())
        {
            possibilities.add(Requests.Request.BodyCase.BUYDEVCARD);
        }
        if(checkPlayDevCard(turn.getChosenCard()))
        {
            possibilities.add(Requests.Request.BodyCase.PLAYDEVCARD);
        }
        if(checkBuildRoad(turn.getChosenEdge()))
        {
            possibilities.add(Requests.Request.BodyCase.BUILDROAD);
        }
        if(checkBuild(turn.getChosenNode()))
        {
            if(turn.getChosenNode().getSettlement() != null)
            {
                possibilities.add(Requests.Request.BodyCase.BUILDCITY);
            }
            else possibilities.add(Requests.Request.BodyCase.BUILDSETTLEMENT);
        }
        if(checkChosenResource(turn.getChosenResource()))
        {
            possibilities.add(Requests.Request.BodyCase.CHOOSERESOURCE);
        }
        if(checkTarget(turn.getTarget()))
        {
            possibilities.add(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
        }
        if(checkHex(turn.getChosenHex()))
        {
            possibilities.add((Requests.Request.BodyCase.MOVEROBBER));
        }
        if(checkDiscard(turn.getChosenResources()))
        {
            possibilities.add(Requests.Request.BodyCase.DISCARDRESOURCES);
        }
        if(checkSendResponse(turn.getTradeResponse()))
        {
            possibilities.add(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
        }
        if(checkInitiateTrade(Trade.Kind.newBuilder().setBank(turn.getBankTrade()).build()) ||
                checkInitiateTrade(Trade.Kind.newBuilder().setPlayer(turn.getPlayerTrade()).build()))
        {
            possibilities.add(Requests.Request.BodyCase.INITIATETRADE);
        }
        if(expectedMoves.isEmpty())
        {
            possibilities.add(Requests.Request.BodyCase.ENDTURN);
            possibilities.add(Requests.Request.BodyCase.JOINLOBBY);
            possibilities.add(Requests.Request.BodyCase.ROLLDICE);
        }
        possibilities.add(Requests.Request.BodyCase.CHATMESSAGE);

        return possibilities;
    }

    /**
     * Checks that a MOVEROBBER move is valid
     * @param hex the chosen hex
     * @return whether or not the move is valid
     */
    private boolean checkHex(Hex hex)
    {
        // Ensure this hex doesn't already have the robber, and that the move is expected OR no moves are expected
        return checkTurn() && !hex.equals(getGame().getGrid().getHexWithRobber()) &&
                (expectedMoves.isEmpty() || (expectedMoves.contains(Requests.Request.BodyCase.MOVEROBBER)));
    }

    /**
     * Checks that a MOVEROBBER move is valid
     * @param resources the chosen resources
     * @return whether or not the move is valid
     */
    private boolean checkDiscard(Map<ResourceType, Integer> resources)
    {
        int sum = 0;
        for(ResourceType r : resources.keySet())
        {
            sum += resources.get(r);
        }

        // Ensure that a discard is expected, and that the discard can be afforded and that it brings the user
        // into a safe position having 7 or less resources.
        return (!expectedMoves.isEmpty() && expectedMoves.contains(Requests.Request.BodyCase.DISCARDRESOURCES))
                && getGame().getPlayer().canAfford(resources) && sum <= 7;
    }

    /**
     * Checks that a SUBMITTARGETPLAYER move is valid
     * @param target the target
     * @return whether or not the move is valid
     */
    private boolean checkTarget(Colour target)
    {
        // Ensure that a SUBMITTARGETPLAYER move is expected, and that the player
        // has resources
        return (checkTurn() && expectedMoves.contains(Requests.Request.BodyCase.SUBMITTARGETPLAYER)
                && getGame().getPlayer(target).getNumResources() > 0);
    }

    /**
     * Checks that a CHOOSERESOURCE move is valid
     * @param r the resource in question
     * @return whether or not the move is valid
     */
    public boolean checkChosenResource(ResourceType r)
    {
        // Ensure that a CHOOSE RESOURCE move is expected, and that the bank
        // has the requested resource available
        return (expectedMoves.contains(Requests.Request.BodyCase.CHOOSERESOURCE)
                && getGame().getBank().getAvailableResources().get(r) > 0);
    }

    /**
     * Checks the player can play the given card
     * @param type the type of card wanting to be played
     */
    public boolean checkPlayDevCard(DevelopmentCardType type)
    {
        if(type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University))
            return false;

        // If player's turn and no other moves are expected
        if(checkTurn() && expectedMoves.isEmpty())
        {
            Player player = getGame().getPlayer();

            // If the player owns the provided card
            if(player.getDevelopmentCards().containsKey(type) && player.getDevelopmentCards().get(type) > 0)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if the player can buy a dev card
     */
    public boolean checkBuyDevCard()
    {
        // If its the user's turn, they have no expected moves, and
        return checkTurn() && expectedMoves.isEmpty() && getGame().getPlayer().canAfford(DevelopmentCardType.getCardCost());
    }

    /**
     * Checks if a city or settlement can be built on the given nodeS
     * @param node the desired settlement / city location
     */
    public boolean checkBuild(Node node)
    {
        Player p = getGame().getPlayer();
        return checkTurn() && expectedMoves.isEmpty() && (p.canBuildSettlement(node) || p.canBuildCity(node));
    }

    /**
     * @return whether or not it is the player's turn
     */
    public boolean checkTurn()
    {
        return getGame().getCurrentPlayer() == getGame().getPlayer().getColour();
    }

    /**
     * Checks to see if the player can build a road
     * @param edge the desired road location
     */
    public boolean checkBuildRoad(Edge edge)
    {
        return checkTurn() && getGame().getPlayer().canBuildRoad(edge) &&
                (expectedMoves.isEmpty() || expectedMoves.contains(Requests.Request.BodyCase.BUILDROAD));
    }

    /**
     * Checks to see if the player can submit a trade response
     * @return whether or not this is a valid move
     */
    public boolean checkSendResponse(Trade.Response response)
    {
        Trade.WithPlayer trade = turn.getPlayerTrade();
        if(trade != null && turn.isTradePhase())
        {
            Resource.Counts cost = trade.getOther().getId().equals(getGame().getPlayer().getId()) ? trade.getWanting() : trade.getOffering();
            Resource.Counts wanting = trade.getOther().getId().equals(getGame().getPlayer().getId()) ? trade.getOffering() : trade.getWanting();
            Map<ResourceType, Integer> costMap = getGame().processResources(cost);
            Map<ResourceType, Integer> wantingMap = getGame().processResources(wanting);

            // If the move is expected, and the response is accept AND the player can afford it.
            // OR if it is reject.
            return expectedMoves.contains(Requests.Request.BodyCase.SUBMITTRADERESPONSE) &&
            ((response.equals(Trade.Response.ACCEPT) && getGame().getBank().canAfford(wantingMap) && getGame().getPlayer().canAfford(costMap))
                    || (response.equals(Trade.Response.REJECT)));
        }

        return false;
    }

    /**
     * Checks whether or not the player can trade the following trade
     * @param initiateTrade the initiate trade request
     * @return whether or not the trade is valid
     */
    public boolean checkInitiateTrade(Trade.Kind initiateTrade)
    {
        Map<ResourceType, Integer> cost = new HashMap<ResourceType, Integer>(), wanting = new HashMap<ResourceType, Integer>();

        switch(initiateTrade.getTradeCase())
        {
            case BANK:
                cost = getGame().processResources(initiateTrade.getBank().getOffering());
                wanting = getGame().processResources(initiateTrade.getBank().getWanting());
                break;
            case PLAYER:
                cost = getGame().processResources(initiateTrade.getPlayer().getOffering());
                wanting = getGame().processResources(initiateTrade.getPlayer().getWanting());
                break;
        }

        // If both the player and the bank can afford the given trade
        return getGame().getPlayer().canAfford(cost) && getGame().getBank().canAfford(wanting);
    }


    /**
     * Checks that the given player is currently able to make a move of the given type
     * @param req the received message
     * @return true / false depending on legality of move
     */
    private boolean isExpected(Requests.Request req)
    {
        Requests.Request.BodyCase type = req.getBodyCase();

        // If in trade phase and the given message isn't a trade
        if(turn.isTradePhase() && (!req.getBodyCase().equals(Requests.Request.BodyCase.INITIATETRADE) && checkTurn())
                || (!req.getBodyCase().equals(Requests.Request.BodyCase.SUBMITTRADERESPONSE) && !checkTurn()) )
        {
            return false;
        }

        // If it's not your turn and there are no expected moves from you
        if(expectedMoves.isEmpty() && !checkTurn())
        {
            return false;
        }

        // If not a request or the move is not expected
        if(type == null || (!expectedMoves.contains(type) && !expectedMoves.isEmpty()))
        {
            return false;
        }

        return true;
    }

    /**
     * Ensures the request is valid at this current stage of the game
     * @param req the request polled from the queue
     * @return a boolean indicating success or not
     */
    public boolean validateMsg(Requests.Request req)
    {
        // If it is not the player's turn, the message type is unknown OR the given request is NOT expected
        // send error and return false
        if(!isExpected(req))
        {
            return false;
        }

        switch(req.getBodyCase())
        {
            case BUILDROAD:
                return checkBuildRoad(getGame().getGrid().getEdge(req.getBuildRoad().getA(), req.getBuildRoad().getB()));
            case BUILDSETTLEMENT:
                return checkBuild(getGame().getGrid().getNode(req.getBuildSettlement().getX(), req.getBuildSettlement().getY()));
            case BUILDCITY:
                return checkBuild(getGame().getGrid().getNode(req.getBuildCity().getX(), req.getBuildCity().getY()));
            case BUYDEVCARD:
                return checkBuyDevCard();
            case PLAYDEVCARD:
                return checkPlayDevCard(DevelopmentCardType.fromProto(req.getPlayDevCard()));
            case SUBMITTARGETPLAYER:
                return checkTarget(getGame().getPlayer(req.getSubmitTargetPlayer().getId()).getColour());
            case DISCARDRESOURCES:
                return checkDiscard(getGame().processResources(req.getDiscardResources()));
            case MOVEROBBER:
                return checkHex(getGame().getGrid().getHex(req.getMoveRobber().getX(), req.getMoveRobber().getY()));
            case CHOOSERESOURCE:
                return checkChosenResource(ResourceType.fromProto(req.getChooseResource()));
            case SUBMITTRADERESPONSE:
                return checkSendResponse(req.getSubmitTradeResponse());
            case INITIATETRADE:
                return checkInitiateTrade(req.getInitiateTrade());

            case ENDTURN:
            case ROLLDICE:
            case JOINLOBBY:
                return expectedMoves.isEmpty();


            case CHATMESSAGE:
                break;
        }

        return true;
    }

    private ClientGame getGame()
    {
        // Block until the game state has been received
        while(client.getState() == null) {}
        return client.getState();
    }
}
