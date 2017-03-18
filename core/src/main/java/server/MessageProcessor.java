package server;

import enums.Colour;
import enums.ResourceType;
import exceptions.BankLimitException;
import exceptions.CannotAffordException;
import exceptions.IllegalBankTradeException;
import exceptions.IllegalPortTradeException;
import game.Game;
import game.players.Player;
import intergroup.Events;
import intergroup.Messages;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.trade.Trade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class for processing messages received from the server
 * @author 140001596
 */
public class MessageProcessor
{
    private ServerGame game;
    private final Server server;
    private boolean monopoly;
    private Logger logger;
    private HashMap<Colour, List<Requests.Request.BodyCase>> expectedMoves;
    private ConcurrentLinkedQueue<ReceivedMessage> movesToProcess;
    private Trade.WithPlayer currentTrade;
    private boolean tradePhase, robberMoved;
    private ReceivedMessage lastMessage;
    private ResourceType chosenResource;

    public MessageProcessor(ServerGame game, Server server)
    {
        this.game = game;
        this.server =server;
        logger = new Logger();
        expectedMoves = new HashMap<Colour, List<Requests.Request.BodyCase>>();
        movesToProcess = new ConcurrentLinkedQueue<ReceivedMessage>();
        for(Colour c : Colour.values())
        {
            expectedMoves.put(c, new ArrayList<>());
        }
    }
    
    /**
     * Process the next message, and send any responses and events.
     * @throws IOException
     */
    public Events.Event processMessage() throws IOException
    {
        ReceivedMessage receivedMessage = movesToProcess.poll();
        lastMessage = receivedMessage;
        Messages.Message msg = receivedMessage.getMsg();
        Colour col = receivedMessage.getCol();
        logger.logReceivedMessage(msg);

        // If not valid
        if(!validateMsg(msg, col))
        {
            return Events.Event.newBuilder().setError(Events.Event.
                            Error.newBuilder().setDescription("Move unexpected or invalid.").build()).build();
        }

        // switch on message type
        switch(msg.getTypeCase())
        {
            // User request
            case REQUEST:
                return processMove(msg, col);

            default:
                break;
        }
        
        return Events.Event.newBuilder().setError(Events.Event.
                Error.newBuilder().setDescription("Move type unexpected or invalid.").build()).build();
    }

    /**
     * This method interprets the move sent across the network and attempts
     * to process it
     * @param msg the message received from across the network
     * @return the response message
     */
    private Events.Event processMove(Messages.Message msg, Colour colour) throws IOException
    {
        Requests.Request request = msg.getRequest();
        Events.Event.Builder ev = Events.Event.newBuilder();

        try
        {
            // Switch on message type to interpret the move, then process the move
            // and receive the response
            switch (request.getBodyCase())
            {
                case BUILDROAD:
                    game.buildRoad(request.getBuildRoad());
                    ev.setRoadBuilt(request.getBuildRoad());
                    break;
                case BUILDSETTLEMENT:
                    game.buildSettlement(request.getBuildSettlement());
                    ev.setSettlementBuilt(request.getBuildSettlement());
                    break;
                case BUILDCITY:
                    game.upgradeSettlement(request.getBuildCity());
                    ev.setCityBuilt(request.getBuildCity());
                    break;
                case BUYDEVCARD:
                    ev.setDevCardBought(game.buyDevelopmentCard());
                    break;
                case JOINLOBBY:
                    ev.setLobbyUpdate(game.joinGame(request.getJoinLobby(), colour));
                    break;
                case MOVEROBBER:
                    game.moveRobber(request.getMoveRobber());
                    ev.setRobberMoved(request.getMoveRobber());
                    robberMoved = true;
                    break;
                case DISCARDRESOURCES:
                    game.processDiscard(request.getDiscardResources(), colour);
                    ev.setCardsDiscarded(request.getDiscardResources());
                    break;
                case ENDTURN:
                    if(canEndTurn())
                    {
                        ev.setTurnEnded(game.changeTurn());
                        tradePhase = false;
                        currentTrade = null;
                    }
                    else ev.setError(Events.Event.Error.newBuilder().setDescription("Cannot end turn yet."));
                    break;
                case CHOOSERESOURCE:
                    if(monopoly)
                    {
                        ev.setMonopolyResolution(game.playMonopolyCard(request.getChooseResource()));
                        monopoly = false;
                    }
                    else if(robberMoved)
                    {
                        chosenResource = ResourceType.fromProto(request.getChooseResource());
                    }
                    else
                    {
                        game.chooseResources(request.getChooseResource());
                        ev.setResourceChosen(request.getChooseResource());
                    }
                    break;
                case ROLLDICE:
                    ev.setRolled(game.generateDiceRoll());
                    break;
                case PLAYDEVCARD:
                    game.playDevelopmentCard(request.getPlayDevCard());
                    ev.setDevCardPlayed(request.getPlayDevCard());
                    break;
                case SUBMITTARGETPLAYER:
                    Board.Steal steal = game.takeResource(request.getSubmitTargetPlayer().getId(), chosenResource);
                    if(steal != null) ev.setResourceStolen(steal);
                    break;
                case INITIATETRADE:
                    Trade.WithBank trade = processTradeType(request.getInitiateTrade(), msg);
                    if(trade != null) ev.setBankTrade(trade);
                    break;
                case SUBMITTRADERESPONSE:
                    // TODO timeouts?
                    if(currentTrade != null && request.getSubmitTradeResponse().equals(Trade.Response.ACCEPT))
                    {
                        ev.setPlayerTrade(currentTrade);
                        game.processPlayerTrade(currentTrade);
                        currentTrade = null;
                        break;
                    }
                    else
                    {
                        currentTrade = null; // TODO send rejection?
                        return null;
                    }
                case CHATMESSAGE:
                    ev.setChatMessage(request.getChatMessage());
                    break;

            }
        }
        catch(Exception e)
        {
            ev.setError(Events.Event.Error.newBuilder().setDescription(e.getMessage()).build());
        }

        // Add expected trade response for other player
        if(request.getBodyCase().equals(Requests.Request.BodyCase.INITIATETRADE)
                && request.getInitiateTrade().getTradeCase().equals(Trade.Kind.TradeCase.PLAYER))
        {
            updateExpectedMoves(request, game.getPlayer(request.getInitiateTrade().getPlayer().getOther().getId()).getColour());
        }

        // Update expected moves if no error from the previously processed one
        else if(!ev.getTypeCase().equals(Events.Event.TypeCase.ERROR))
        {
            // Remove move from expected list, and add any new ones
            if(expectedMoves.get(colour).contains(request.getBodyCase()))
            {
                expectedMoves.get(colour).remove(request.getBodyCase());
            }
            updateExpectedMoves(request, colour);
        }

        return ev.build();
    }

    /**
     * Updates the expected moves for the given player
     * @param req the request that just succeeded
     * @param colour the player's colour
     */
    private void updateExpectedMoves(Requests.Request req, Colour colour)
    {
        List<Requests.Request.BodyCase> moves = expectedMoves.get(colour);

        // Switch on request type
        switch(req.getBodyCase())
        {
            // Expect for the player to send a steal request next
            case MOVEROBBER:
                moves.add(Requests.Request.BodyCase.CHOOSERESOURCE);
                break;

            // Once the player chooses a resource steal, expect a message on who to steal from
            case CHOOSERESOURCE:
                if(robberMoved)
                {
                    moves.add(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
                    robberMoved = false;
                }
                break;

            // Add that a response is expected from this player
            case INITIATETRADE:
                moves.add(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
                break;

            // Add expected moves based on recently played dev card
            case PLAYDEVCARD:
            {
                switch(req.getPlayDevCard())
                {
                    case KNIGHT:
                        moves.add(Requests.Request.BodyCase.MOVEROBBER);
                        break;
                    case YEAR_OF_PLENTY:
                        moves.add(Requests.Request.BodyCase.CHOOSERESOURCE);
                        moves.add(Requests.Request.BodyCase.CHOOSERESOURCE);
                        break;
                    case ROAD_BUILDING:
                        moves.add(Requests.Request.BodyCase.BUILDROAD);
                        moves.add(Requests.Request.BodyCase.BUILDROAD);
                        break;
                    case MONOPOLY:
                        monopoly = true;
                        moves.add(Requests.Request.BodyCase.CHOOSERESOURCE);
                        break;
                }
                break;
            }

            // No new expected moves
            default:
                break;
        }
    }

    /**
     * Forwards the trade request to the other player and blocks for a response
     * @param request the trade request
     * @param msg the original request, received from across the network
     * @return the status of the trade "accepted, denied, offer"
     */
    private Trade.WithBank processTradeType(Trade.Kind request, Messages.Message msg) throws IllegalPortTradeException,
            IllegalBankTradeException, CannotAffordException, IOException, BankLimitException
    {
        tradePhase = true;

        // Switch on trade type
        switch(request.getTradeCase())
        {
            // Simply forward the message
            case PLAYER:
                currentTrade = request.getPlayer();
                server.forwardTradeOffer(msg, request.getPlayer());
                return null;

            case BANK:
                game.determineTradeType(request.getBank());
                break;
        }

        return request.getBank();
    }

    /**
     * Get initial placements from each of the connections
     * and send them to the game.
     */
    public void getInitialSettlementsAndRoads() throws IOException
    {
        Board.Player.Id current = game.getPlayer(game.getCurrentPlayer()).getId();
        Board.Player.Id next = null;

        // Get settlements and roads forwards from the first player
        for(int i = 0; i < Game.NUM_PLAYERS; i++)
        {
            next = Board.Player.Id.values()[(current.ordinal() + i) % Game.NUM_PLAYERS];
            receiveInitialMoves(game.getPlayer(next).getColour());
        }

        // Get second set of settlements and roads in reverse order
        for(int i = Game.NUM_PLAYERS - 1; i >= 0; i--)
        {
            receiveInitialMoves(game.getPlayer(next).getColour());
            next = Board.Player.Id.values()[(current.ordinal() + i) % Game.NUM_PLAYERS];
        }
    }

    /**
     * Receives the initial moves for each player in the appropriate order
     * @param c the player to receive the initial moves from
     * @throws IOException
     */
    private void receiveInitialMoves(Colour c) throws IOException
    {
        Player p = game.getPlayers().get(c);
        int oldRoadAmount = p.getRoads().size(), oldSettlementsAmount = p.getSettlements().size();

        // Loop until player sends valid new settlement
        while(p.getSettlements().size() < oldSettlementsAmount)
        {
            expectedMoves.get(c).add(Requests.Request.BodyCase.BUILDSETTLEMENT);
            game.setCurrentPlayer(c);

            processMessage();
        }

        // Loop until player sends valid new road
        while(p.getRoads().size() < oldRoadAmount)
        {
            expectedMoves.get(c).add(Requests.Request.BodyCase.BUILDROAD);
            game.setCurrentPlayer(c);

            processMessage();
        }
    }

    /**
     * Checks that the given player is currently able to make a move of the given type
     * @param msg the received message
     * @param col the current turn
     * @return true / false depending on legality of move
     */
    private boolean isExpected(Messages.Message msg, Colour col)
    {
        Requests.Request.BodyCase type = msg.getTypeCase().equals(Messages.Message.TypeCase.REQUEST) ? msg.getRequest().getBodyCase() : null;
        List<Requests.Request.BodyCase> expected = expectedMoves.get(col);

        // If in trade phase and the given message isn't a trade
        if(tradePhase && (!msg.getRequest().getBodyCase().equals(Requests.Request.BodyCase.INITIATETRADE) && game.getCurrentPlayer().equals(col))
                || (!msg.getRequest().getBodyCase().equals(Requests.Request.BodyCase.SUBMITTRADERESPONSE) && !game.getCurrentPlayer().equals(col)) )
        {
            return false;
        }

        // If it's not your turn and there are no expected moves from you
        if(expected.size() == 0 && !game.getCurrentPlayer().equals(col))
        {
            return false;
        }

        // If not a request or the move is not expected
        if(type == null || (!expected.contains(type) && expected.size() > 0))
        {
            return false;
        }

        return true;
    }

    /**
     * Ensures the message in the queue pertains to the current player
     * @param msg the message polled from the queue
     * @return a boolean indicating success or not
     * @throws IOException
     */
    private boolean validateMsg(Messages.Message msg, Colour col) throws IOException
    {
        Colour playerColour = col;

        // If it is not the player's turn, the message type is unknown OR the given request is NOT expected
        // send error and return false
        if(!isExpected(msg, col))
        {
            return false;
        }

        return true;
    }

    /**
     * Checks to see if there are any expected moves
     * @return if the turn can be ended
     */
    private boolean canEndTurn()
    {
        boolean valid = true;

        // Check if any expected moves first
        for(List<Requests.Request.BodyCase> expected : expectedMoves.values())
        {
            if(expected.size() > 0)
            {
                valid = false;
            }
        }

        return valid;
    }

    public boolean isTradePhase()
    {
        return tradePhase;
    }

    public void addExpectedMove(Colour c, Requests.Request.BodyCase type)
    {
        expectedMoves.get(c).add(type);
    }

    public List<Requests.Request.BodyCase> getExpectedMoves(Colour colour)
    {
        return expectedMoves.get(colour);
    }

    public void addMoveToProcess(ReceivedMessage msg)
    {
        movesToProcess.add(msg);
    }

    public void setGame(ServerGame game)
    {
        this.game = game;
    }

    public ReceivedMessage getLastMessage() 
    {
        return lastMessage;
    }
}
