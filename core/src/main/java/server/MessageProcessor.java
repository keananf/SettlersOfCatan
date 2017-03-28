package server;

import enums.Colour;
import exceptions.BankLimitException;
import exceptions.CannotAffordException;
import exceptions.IllegalBankTradeException;
import exceptions.IllegalPortTradeException;
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
 * 
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
	private boolean tradePhase;
	private ReceivedMessage lastMessage;

	public MessageProcessor(ServerGame game, Server server)
	{
		this.game = game;
		this.server = server;
		logger = new Logger();
		expectedMoves = new HashMap<Colour, List<Requests.Request.BodyCase>>();
		movesToProcess = new ConcurrentLinkedQueue<ReceivedMessage>();
		for (Colour c : Colour.values())
		{
			expectedMoves.put(c, new ArrayList<>());
		}
	}

	/**
	 * Process the next message, and send any responses and events.
	 * 
	 * @throws IOException
	 */
	public Events.Event processMessage() throws IOException
	{
		ReceivedMessage receivedMessage = movesToProcess.poll();
		lastMessage = receivedMessage;
		if (receivedMessage == null || receivedMessage.getMsg() == null) { return null; }

		Messages.Message msg = receivedMessage.getMsg();
		Colour col = receivedMessage.getCol();
		logger.logReceivedMessage(msg);

		// If not valid
		if (!validateMsg(msg,
				col)) { return Events.Event.newBuilder()
						.setError(Events.Event.Error.newBuilder().setDescription("Move unexpected or invalid.").build())
						.build(); }

		// switch on message type
		switch (msg.getTypeCase())
		{
		// User request
		case REQUEST:
			return processMove(msg, col);

		default:
			break;
		}

		return Events.Event.newBuilder()
				.setError(Events.Event.Error.newBuilder().setDescription("Move type unexpected or invalid.").build())
				.build();
	}

	/**
	 * This method interprets the move sent across the network and attempts to
	 * process it
	 * 
	 * @param msg the message received from across the network
	 * @return the response message
	 */
	private Events.Event processMove(Messages.Message msg, Colour colour) throws IOException
	{
		Requests.Request request = msg.getRequest();
		Events.Event.Builder ev = Events.Event.newBuilder();
		ev.setInstigator(Board.Player.newBuilder().setId(game.getPlayer(colour).getId()).build());

		server.log("Server Proc", String.format("Processing request %s from %s", msg.getRequest().getBodyCase().name(),
				ev.getInstigator().getId().name()));

        // Switch on message type to interpret the move, then process the
        // move
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
            break;
        case DISCARDRESOURCES:
            game.processDiscard(request.getDiscardResources(), colour);
            ev.setCardsDiscarded(request.getDiscardResources());
            break;
        case ENDTURN:
            if (canEndTurn())
            {
                ev.setTurnEnded(game.changeTurn());
                tradePhase = false;
                currentTrade = null;
            }
            else
                ev.setError(Events.Event.Error.newBuilder().setDescription("Cannot end turn yet."));
            break;
        case CHOOSERESOURCE:
            if (monopoly)
            {
                ev.setMonopolyResolution(game.playMonopolyCard(request.getChooseResource()));
                monopoly = false;
                break;
            }
            else
            {
                game.chooseResources(request.getChooseResource());
            }
            ev.setResourceChosen(request.getChooseResource());
            break;
        case ROLLDICE:
            ev.setRolled(game.generateDiceRoll());
            break;
        case PLAYDEVCARD:
            game.playDevelopmentCard(request.getPlayDevCard());
            ev.setDevCardPlayed(request.getPlayDevCard());
            break;
        case SUBMITTARGETPLAYER:
            Board.Steal steal = game.takeResource(request.getSubmitTargetPlayer().getId());
            if (steal != null) ev.setResourceStolen(steal);
            break;
        case INITIATETRADE:
            Trade.WithBank trade = processTradeType(request.getInitiateTrade(), msg);
            if (trade != null) ev.setBankTrade(trade);
            break;
        case SUBMITTRADERESPONSE:
            // TODO timeouts?
            if (currentTrade != null && request.getSubmitTradeResponse().equals(Trade.Response.ACCEPT))
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

		// Add expected trade response for other player
		if (request.getBodyCase().equals(Requests.Request.BodyCase.INITIATETRADE)
				&& request.getInitiateTrade().getTradeCase().equals(Trade.Kind.TradeCase.PLAYER))
		{
			updateExpectedMoves(request,
					game.getPlayer(request.getInitiateTrade().getPlayer().getOther().getId()).getColour());
		}

		// Update expected moves if no error from the previously processed one
		else if (!ev.getTypeCase().equals(Events.Event.TypeCase.ERROR))
		{
			// Remove move from expected list, and add any new ones
			if (expectedMoves.get(colour).contains(request.getBodyCase()))
			{
				expectedMoves.get(colour).remove(request.getBodyCase());
			}
			updateExpectedMoves(request, colour);
		}

		// Add discard move if necessary
		if (request.getBodyCase().equals(Requests.Request.BodyCase.ROLLDICE)
				&& ev.getRolled().getA() + ev.getRolled().getB() == 7)
		{
			server.log("Server Proc", String.format("Adding MOVEROBBER to %s", ev.getInstigator().getId().name()));
			expectedMoves.get(colour).add(Requests.Request.BodyCase.MOVEROBBER);
			for (Player p : game.getPlayers().values())
			{
				if (p.getNumResources() > 7)
				{
					server.log("Server Proc", String.format("Adding DISCARDRESOURCES to %s", p.getId().name()));
					expectedMoves.get(p.getColour()).add(Requests.Request.BodyCase.DISCARDRESOURCES);
				}
			}
		}

		return ev.build();
	}

	/**
	 * Updates the expected moves for the given player
	 * 
	 * @param req the request that just succeeded
	 * @param colour the player's colour
	 */
	private void updateExpectedMoves(Requests.Request req, Colour colour)
	{
		List<Requests.Request.BodyCase> moves = expectedMoves.get(colour);

		// Switch on request type
		switch (req.getBodyCase())
		{
		// Expect for the player to send a steal request next
		case MOVEROBBER:
			moves.add(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
			break;

		// Add that a dice roll is expected as the first move for the new player
		case ENDTURN:
			expectedMoves.get(game.getCurrentPlayer()).add(Requests.Request.BodyCase.ROLLDICE);
			break;

		// Add that a response is expected from this player
		case INITIATETRADE:
			moves.add(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
			break;

		// Add expected moves based on recently played dev card
		case PLAYDEVCARD:
		{
			switch (req.getPlayDevCard())
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
	 * 
	 * @param request the trade request
	 * @param msg the original request, received from across the network
	 * @return the status of the trade "accepted, denied, offer"
	 */
	private Trade.WithBank processTradeType(Trade.Kind request, Messages.Message msg) throws IllegalPortTradeException,
			IllegalBankTradeException, CannotAffordException, IOException, BankLimitException
	{
		tradePhase = true;

		// Switch on trade type
		switch (request.getTradeCase())
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
	 * Checks that the given player is currently able to make a move of the
	 * given type
	 * 
	 * @param msg the received message
	 * @param col the current turn
	 * @return true / false depending on legality of move
	 */
	private boolean isExpected(Messages.Message msg, Colour col)
	{
		Requests.Request.BodyCase type = msg.getRequest().getBodyCase();
		List<Requests.Request.BodyCase> expected = expectedMoves.get(col);
		if (type == null || game == null) return false;

		if (!expected.isEmpty() && expected.contains(type))
		{
			return true;
		}

		// Can play dev card on first turn
		else if (!expected.isEmpty() && expected.contains(Requests.Request.BodyCase.ROLLDICE)
				&& msg.getRequest().getBodyCase().equals(Requests.Request.BodyCase.PLAYDEVCARD))
		{
			return true;
		}

		// If the move is not expected
		else if (!expected.isEmpty()) return false;

		// If in trade phase and the given message isn't a trade
		if (tradePhase
				&& ((!type.equals(Requests.Request.BodyCase.INITIATETRADE) && game.getCurrentPlayer().equals(col))
						|| (!type.equals(Requests.Request.BodyCase.SUBMITTRADERESPONSE)
								&& game.getCurrentPlayer().equals(col)))) { return false; }

		// If it's not your turn and there are no expected moves from you
		if (!game.getCurrentPlayer().equals(col) || (!game.getCurrentPlayer().equals(col) && expected.isEmpty()))
			return false;

		return true;
	}

	/**
	 * Ensures the message in the queue pertains to the current player
	 * 
	 * @param msg the message polled from the queue
	 * @return a boolean indicating success or not
	 * @throws IOException
	 */
	private boolean validateMsg(Messages.Message msg, Colour col) throws IOException
	{
		// If it is not the player's turn, the message type is unknown OR the
		// given request is NOT expected
		// send error and return false
		if (!isExpected(msg, col)) { return false; }

		return true;
	}

	/**
	 * Checks to see if there are any expected moves
	 * 
	 * @return if the turn can be ended
	 */
	private boolean canEndTurn()
	{
		boolean valid = true;

		// Check if any expected moves first
		for (List<Requests.Request.BodyCase> expected : expectedMoves.values())
		{
			if (expected.size() > 0)
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
