package client;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.players.Player;
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
import java.util.concurrent.Semaphore;

/**
 * Class which determines move possibilities based upon a game state
 * @author 140001596
 */
public class MoveProcessor
{
    private final Client client;

    public MoveProcessor(Client client)
    {
        this.client = client;
    }

    /**
     * Retrieve a list of all building possibilities, regardless of game context
     * @return a list of all possible building turns
     */
    private List<Turn> getBuildingPossibilities()
    {
        List<Turn> moves = new ArrayList<Turn>();

        // This player
        Player p = getGame().getPlayer();

        // For each node
        for(Node n : getGame().getGrid().nodes.values())
        {
            Turn turn = new Turn();
            if(checkBuild(n) && n.getSettlement() == null)
            {
                turn.setChosenNode(n);
                turn.setChosenMove(Requests.Request.BodyCase.BUILDSETTLEMENT);

            }
            else if(checkBuild(n) && n.getSettlement() != null)
            {
                turn.setChosenNode(n);
                turn.setChosenMove(Requests.Request.BodyCase.BUILDCITY);
            }
            else continue;

            moves.add(turn);
        }

        // For each edge
        for(Edge e : getGame().getGrid().getEdgesAsList())
        {
            Turn turn = new Turn();
            if(checkBuildRoad(e))
            {
                turn.setChosenEdge(e);
                turn.setChosenMove(Requests.Request.BodyCase.BUILDROAD);
                moves.add(turn);
            }
        }

        return moves;
    }

    /**
     * Processes a turn and ascertains all possible moves
     * @return list of possible choices from this proposed turn
     */
    public List<Turn> getPossibleMoves()
    {
        try
        {
            getGameLock().acquire();
            try
            {
                getTurnLock().acquire();
                try
                {
                    return getPossibleMovesInternal();
                }
                finally
                {
                    getTurnLock().release();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            finally
            {
                getGameLock().release();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Processes a turn and ascertains all possible moves
     * @return list of possible choices from this proposed turn
     */
    private List<Turn> getPossibleMovesInternal()
    {
        List<Turn> possibilities = new ArrayList<Turn>();

        // Add initial possibilities
        possibilities.add(new Turn(Requests.Request.BodyCase.CHATMESSAGE));
        possibilities.addAll(getBuildingPossibilities());

        // Check other possibilities
        if(checkBuyDevCard())
        {
            possibilities.add(new Turn(Requests.Request.BodyCase.BUYDEVCARD));
        }
        if(getExpectedMoves().isEmpty())
        {
            possibilities.add(new Turn(Requests.Request.BodyCase.ENDTURN));
            possibilities.add(new Turn(Requests.Request.BodyCase.INITIATETRADE));

            // If the turn hasn't started, then the player can roll the dice
            if(!getTurn().hasTurnStarted())
                possibilities.add(new Turn(Requests.Request.BodyCase.ROLLDICE));
        }
        else if(getExpectedMoves().contains(Requests.Request.BodyCase.DISCARDRESOURCES))
        {
            possibilities.add(new Turn(Requests.Request.BodyCase.DISCARDRESOURCES));
        }

        // Check owned dev cards
        for(DevelopmentCardType type : getGame().getPlayer().getDevelopmentCards().keySet())
        {
            if(checkPlayDevCard(type))
            {
                Turn turn = new Turn();
                turn.setChosenMove(Requests.Request.BodyCase.PLAYDEVCARD);
                turn.setChosenCard(type);
                possibilities.add(turn);
            }
        }

        // For every owned resource type
        for(ResourceType r : ResourceType.values())
        {
            if(checkChosenResource(r))
            {
                Turn turn = new Turn();
                turn.setChosenMove(Requests.Request.BodyCase.CHOOSERESOURCE);
                turn.setChosenResource(r);
                possibilities.add(turn);
            }
        }

        // For every hex
        for(Hex h : getGame().getGrid().grid.values())
        {
            if(checkHex(h))
            {
                Turn turn = new Turn();
                turn.setChosenHex(h);
                turn.setChosenMove(Requests.Request.BodyCase.MOVEROBBER);
                possibilities.add(turn);
            }
        }

        // For every colour
        for(Colour c : Colour.values())
        {
            if (checkTarget(c))
            {
                Turn turn = new Turn();
                turn.setTarget(c);
                turn.setChosenMove(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
                possibilities.add(turn);
            }
        }

        // For each response type
        for(Trade.Response resp : Trade.Response.values())
        {
            if(checkSendResponse(resp))
            {
                Turn turn = new Turn();
                turn.setChosenMove(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
                turn.setTradeResponse(resp);
                turn.setPlayerTrade(getTurn().getPlayerTrade());
                possibilities.add(turn);
            }
        }

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
                (getExpectedMoves().isEmpty() || (getExpectedMoves().contains(Requests.Request.BodyCase.MOVEROBBER)));
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
        return (!getExpectedMoves().isEmpty() && getExpectedMoves().contains(Requests.Request.BodyCase.DISCARDRESOURCES))
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
        return (checkTurn() && getExpectedMoves().contains(Requests.Request.BodyCase.SUBMITTARGETPLAYER)
                && getGame().getPlayer(target).getNumResources() > 0);
    }

    /**
     * Checks that a CHOOSERESOURCE move is valid
     * @param r the resource in question
     * @return whether or not the move is valid
     */
    private boolean checkChosenResource(ResourceType r)
    {
        // Ensure that a CHOOSE RESOURCE move is expected, and that the bank
        // has the requested resource available
        return (getExpectedMoves().contains(Requests.Request.BodyCase.CHOOSERESOURCE)
                && getGame().getBank().getAvailableResources().get(r) > 0);
    }

    /**
     * Checks the player can play the given card
     * @param type the type of card wanting to be played
     */
    private boolean checkPlayDevCard(DevelopmentCardType type)
    {
        if(type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University))
            return false;

        // If player's turn and no other moves are expected
        if(checkTurn() && getExpectedMoves().isEmpty())
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
    private boolean checkBuyDevCard()
    {
        // If its the user's turn, they have no expected moves, and
        return checkTurn() && getExpectedMoves().isEmpty() && getGame().getPlayer().canAfford(DevelopmentCardType.getCardCost());
    }

    /**
     * Checks if a city or settlement can be built on the given nodeS
     * @param node the desired settlement / city location
     */
    private boolean checkBuild(Node node)
    {
        Player p = getGame().getPlayer();
        return checkTurn() && getExpectedMoves().isEmpty() && (p.canBuildSettlement(node) || p.canBuildCity(node));
    }

    /**
     * @return whether or not it is the player's turn
     */
    private boolean checkTurn()
    {
        return getGame().getCurrentPlayer() == getGame().getPlayer().getColour();
    }

    /**
     * Checks to see if the player can build a road
     * @param edge the desired road location
     */
    private boolean checkBuildRoad(Edge edge)
    {
        return checkTurn() && getGame().getPlayer().canBuildRoad(edge) &&
                (getExpectedMoves().isEmpty() || getExpectedMoves().contains(Requests.Request.BodyCase.BUILDROAD));
    }

    /**
     * Checks to see if the player can submit a trade response
     * @return whether or not this is a valid move
     * @param response the response
     */
    private boolean checkSendResponse(Trade.Response response)
    {
        boolean val = false;

        Trade.WithPlayer trade = getTurn().getPlayerTrade();
        if(trade != null && getTurn().isTradePhase())
        {
            Resource.Counts cost = trade.getOther().getId().equals(getGame().getPlayer().getId()) ? trade.getWanting() : trade.getOffering();
            Resource.Counts wanting = trade.getOther().getId().equals(getGame().getPlayer().getId()) ? trade.getOffering() : trade.getWanting();
            Map<ResourceType, Integer> costMap = getGame().processResources(cost);
            Map<ResourceType, Integer> wantingMap = getGame().processResources(wanting);

            // If the move is expected, and the response is accept AND the player can afford it.
            // OR if it is reject.
            val = getExpectedMoves().contains(Requests.Request.BodyCase.SUBMITTRADERESPONSE) &&
                    ((response.equals(Trade.Response.ACCEPT) && getGame().getBank().canAfford(wantingMap) && getGame().getPlayer().canAfford(costMap))
                            || (response.equals(Trade.Response.REJECT)));
        }

        return val;
    }

    /**
     * Checks whether or not the player can trade the following trade
     * @param initiateTrade the initiate trade request
     * @return whether or not the trade is valid
     */
    public boolean checkInitiateTrade(Trade.Kind initiateTrade)
    {
        Map<ResourceType, Integer> cost = new HashMap<ResourceType, Integer>(), wanting = new HashMap<ResourceType, Integer>();
        boolean val = false;

        try
        {
            getGameLock().acquire();
            try
            {
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
                val = getGame().getPlayer().canAfford(cost) && getGame().getBank().canAfford(wanting);
            }
            finally
            {
                getGameLock().release();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }


        return val;
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
        if(getTurn().isTradePhase() && (!req.getBodyCase().equals(Requests.Request.BodyCase.INITIATETRADE) && checkTurn())
                || (!req.getBodyCase().equals(Requests.Request.BodyCase.SUBMITTRADERESPONSE) && !checkTurn()) )
        {
            return false;
        }

        // If it's not your turn and there are no expected moves from you
        if(getExpectedMoves().isEmpty() && !checkTurn())
        {
            return false;
        }

        // If not a request or the move is not expected
        if(type == null || (!getExpectedMoves().contains(type) && !getExpectedMoves().isEmpty()))
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
        boolean val = false;
        try
        {
            getGameLock().acquire();
            try
            {
                getTurnLock().acquire();
                try
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
                            val = checkBuildRoad(getGame().getGrid().getEdge(req.getBuildRoad().getA(), req.getBuildRoad().getB()));
                            break;
                        case BUILDSETTLEMENT:
                            val = checkBuild(getGame().getGrid().getNode(req.getBuildSettlement().getX(), req.getBuildSettlement().getY()));
                            break;
                        case BUILDCITY:
                            val = checkBuild(getGame().getGrid().getNode(req.getBuildCity().getX(), req.getBuildCity().getY()));
                            break;
                        case BUYDEVCARD:
                            val = checkBuyDevCard();
                            break;
                        case PLAYDEVCARD:
                            val = checkPlayDevCard(DevelopmentCardType.fromProto(req.getPlayDevCard()));
                            break;
                        case SUBMITTARGETPLAYER:
                            val = checkTarget(getGame().getPlayer(req.getSubmitTargetPlayer().getId()).getColour());
                            break;
                        case DISCARDRESOURCES:
                            val = checkDiscard(getGame().processResources(req.getDiscardResources()));
                            break;
                        case MOVEROBBER:
                            val = checkHex(getGame().getGrid().getHex(req.getMoveRobber().getX(), req.getMoveRobber().getY()));
                            break;
                        case CHOOSERESOURCE:
                            val = checkChosenResource(ResourceType.fromProto(req.getChooseResource()));
                            break;
                        case SUBMITTRADERESPONSE:
                            val = checkSendResponse(req.getSubmitTradeResponse());
                            break;
                        case INITIATETRADE:
                            val = checkInitiateTrade(req.getInitiateTrade());
                            break;

                        case ENDTURN:
                        case ROLLDICE:
                        case JOINLOBBY:
                            val = getExpectedMoves().isEmpty();
                            break;


                        case CHATMESSAGE:
                            val = true;
                            break;
                    }
                }
                finally
                {
                    getTurnLock().release();
                }
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            finally
            {
                getGameLock().release();
            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        return val;
    }

    private ClientGame getGame()
    {
        // Block until the game state has been received
        while(client.getState() == null) {}
        return client.getState();
    }

    private Semaphore getGameLock()
    {
        return client.getStateLock();
    }

    private Semaphore getTurnLock()
    {
        return client.getTurnLock();
    }

    private Turn getTurn()
    {
        return client.getTurn();
    }

    private ConcurrentLinkedQueue<Requests.Request.BodyCase> getExpectedMoves()
    {
        return client.getTurn().getExpectedMoves();
    }
}
