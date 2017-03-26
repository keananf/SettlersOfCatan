package client;

import connection.IServerConnection;
import game.Game;
import intergroup.Events.Event;
import intergroup.Messages.Message;
import intergroup.Requests;
import intergroup.board.Board;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class which continuously listens for updates from the server Created by
 * 140001596
 */
public class EventProcessor
{
	private final Client client;
	private IServerConnection conn;

	public EventProcessor(IServerConnection conn, Client client)
	{
		this.conn = conn;
		this.client = client;
	}

	/**
	 * Processes the event received from the server and updates the game state
	 * 
	 * @param ev the event
	 */
	private void processEvent(Event ev) throws Exception
	{
		client.log("Event Proc", String.format("Processing event %s", ev.getTypeCase().name()));

		// Switch on type of event
		switch (ev.getTypeCase())
		{
		case GAMEWON:
			getGame().setGameOver();
			break;
		case TURNENDED:
			getGame().updateCurrentPlayer();
			getTurn().reset();
			break;
		case CITYBUILT:
			getGame().processNewCity(ev.getCityBuilt(), ev.getInstigator());
			break;
		case SETTLEMENTBUILT:
			getGame().processNewSettlement(ev.getSettlementBuilt(), ev.getInstigator());
			break;
		case ROADBUILT:
			getGame().processRoad(ev.getRoadBuilt(), ev.getInstigator());
			break;
		case ROLLED:
			int roll = ev.getRolled().getA() + ev.getRolled().getB();
			getTurn().setTurnStarted(true);
			getTurn().setRoll(roll);
			getGame().processDice(roll, ev.getRolled().getResourceAllocationList());
			break;
		case ROBBERMOVED:
			getGame().moveRobber(ev.getRobberMoved());
			break;
		case DEVCARDBOUGHT:
			getGame().recordDevCard(ev.getDevCardBought(), ev.getInstigator());
			break;
		case DEVCARDPLAYED:
			getGame().processPlayedDevCard(ev.getDevCardPlayed(), ev.getInstigator());
			break;
		case BEGINGAME:
			client.setGame(new ClientGame());
			getGame().setBoard(ev.getBeginGame());
			client.log("Event Proc",
					String.format("Game information received. \tPlayer Id: %s", getGame().getPlayer().getId().name()));
			break;
		case GAMEINFO:
			client.setGame(new ClientGame());
			getGame().processGameInfo(ev.getGameInfo());
			client.log("Event Proc",
					String.format("Game information received. \tPlayer Id: %s", getGame().getPlayer().getId().name()));
		case CHATMESSAGE:
			getGame().writeMessage(ev.getChatMessage(), ev.getInstigator());
			break;
		case BANKTRADE:
			getTurn().setTradePhase(true);
			getGame().processBankTrade(ev.getBankTrade(), ev.getInstigator());
			break;
		case PLAYERTRADE:
			getTurn().setTradePhase(true);
			if (getTurn().getPlayerTrade() != null)
			{
				getGame().processPlayerTrade(ev.getPlayerTrade(), ev.getInstigator());
			}
			break;
		case LOBBYUPDATE:
			getGame().processPlayers(ev.getLobbyUpdate(), ev.getInstigator());
			break;
		case CARDSDISCARDED:
			getGame().processDiscard(ev.getCardsDiscarded(), ev.getInstigator());
			break;
		case MONOPOLYRESOLUTION:
			getGame().processMonopoly(ev.getMonopolyResolution(), ev.getInstigator());
			break;
		case RESOURCECHOSEN:
			getGame().processResourceChosen(ev.getResourceChosen(), ev.getInstigator());
			break;
		case RESOURCESTOLEN:
			getGame().processResourcesStolen(ev.getResourceStolen(), ev.getInstigator());
			break;
		case ERROR:
			client.log("Client Error", String.format("Error Message: %s", ev.getError().getDescription()));
			// TODO display error?
			break;
		}

		updateExpectedMoves(ev);
	}

	private void updateExpectedMoves(Event ev)
	{
		if (ev == null
				|| (getGame() == null && !(ev.getTypeCase().equals(Event.TypeCase.BEGINGAME)
						|| ev.getTypeCase().equals(Event.TypeCase.GAMEINFO)))
				|| ev.getTypeCase().equals(Event.TypeCase.TYPE_NOT_SET))
			return;

		// Switch on request type
		switch (ev.getTypeCase())
		{
		case TURNENDED:
			if (getTurn().isInitialPhase() && getGame().getPlayer().getSettlements().size() < 2
					&& getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()))
			{
				if (getGame().getTurns() < Game.NUM_PLAYERS)
				{
					client.log("Server initial phase", String.format("Adding BUILDSETTLEMENT to expected moves for %s",
							ev.getInstigator().getId().name()));
					getExpectedMoves().add(Requests.Request.BodyCase.BUILDSETTLEMENT);
				}
				if (getGame().getTurns() >= Game.NUM_PLAYERS && getGame().getTurns() < Game.NUM_PLAYERS * 2 - 1)
				{
					client.log("Server initial phase", String.format("Adding BUILDSETTLEMENT to expected moves for %s",
							ev.getInstigator().getId().name()));
					getExpectedMoves().add(Requests.Request.BodyCase.BUILDSETTLEMENT);
				}

			}
			else if (getTurn().isInitialPhase()
					&& getGame().getPlayer(getGame().getCurrentPlayer()).getSettlements().size() >= 2
					&& getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()))
			{
				getTurn().setInitialPhase(false);
			}

			if (getGame().getPlayer().getSettlements().size() >= 2
					&& getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()))
			{
				getExpectedMoves().add(Requests.Request.BodyCase.ROLLDICE);
			}
			break;

		// Add expected moves based on recently played dev card
		case DEVCARDPLAYED:
		{
			if (ev.getInstigator().getId() == getGame().getPlayer().getId())
			{
				switch (ev.getDevCardPlayed())
				{
				case KNIGHT:
					getExpectedMoves().add(Requests.Request.BodyCase.MOVEROBBER);
					break;
				case YEAR_OF_PLENTY:
					getExpectedMoves().add(Requests.Request.BodyCase.CHOOSERESOURCE);
					getExpectedMoves().add(Requests.Request.BodyCase.CHOOSERESOURCE);
					break;
				case ROAD_BUILDING:
					getExpectedMoves().add(Requests.Request.BodyCase.BUILDROAD);
					getExpectedMoves().add(Requests.Request.BodyCase.BUILDROAD);
					break;
				case MONOPOLY:
					getExpectedMoves().add(Requests.Request.BodyCase.CHOOSERESOURCE);
					break;
				}
			}
			break;
		}

		case ROLLED:
			int dice = ev.getRolled().getA() + ev.getRolled().getB();
			if (dice == 7)
			{
				if (getGame().getPlayer().getNumResources() > 7)
				{
					getExpectedMoves().add(Requests.Request.BodyCase.DISCARDRESOURCES);
				}
				if (ev.getInstigator().getId().getNumber() == getGame().getPlayer().getId().getNumber())
				{
					getExpectedMoves().add(Requests.Request.BodyCase.MOVEROBBER);
				}
			}
			if (ev.getInstigator().getId() == getGame().getPlayer().getId()
					&& getExpectedMoves().contains(Requests.Request.BodyCase.ROLLDICE))
			{
				getExpectedMoves().remove(Requests.Request.BodyCase.ROLLDICE);
			}
			break;

		// Expect for the player to send a steal request next
		case ROBBERMOVED:
			if (getGame().getPlayer().getColour().equals(getGame().getCurrentPlayer()))
			{
				getExpectedMoves().add(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
			}
			if (ev.getInstigator().getId() == getGame().getPlayer().getId()
					&& getExpectedMoves().contains(Requests.Request.BodyCase.MOVEROBBER))
			{
				getExpectedMoves().remove(Requests.Request.BodyCase.MOVEROBBER);
			}
			break;

		// Remove expected moves if necessary
		case MONOPOLYRESOLUTION:
		case RESOURCECHOSEN:
			if (ev.getInstigator().getId() == getGame().getPlayer().getId()
					&& getExpectedMoves().contains(Requests.Request.BodyCase.CHOOSERESOURCE))
			{
				getExpectedMoves().remove(Requests.Request.BodyCase.CHOOSERESOURCE);
			}
			break;
		case ROADBUILT:
			if (getGame().getPlayer(getGame().getCurrentPlayer()).getRoads().size() == 1
					&& getGame().getPlayer(getGame().getCurrentPlayer()).getId()
							.equals(Board.Player.Id.forNumber(Game.NUM_PLAYERS - 1))
					&& getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()))
			{
				getExpectedMoves().add(Requests.Request.BodyCase.BUILDSETTLEMENT);
			}
			if (ev.getInstigator().getId() == getGame().getPlayer().getId()
					&& getExpectedMoves().contains(Requests.Request.BodyCase.BUILDROAD))
			{
				getExpectedMoves().remove(Requests.Request.BodyCase.BUILDROAD);
			}
			break;
		case SETTLEMENTBUILT:
			if (getGame().getPlayer().getRoads().size() < 2
					&& getGame().getPlayer().getColour().equals(getGame().getCurrentPlayer()))
			{
				client.log("Server initial phase",
						String.format("Adding BUILDROAD to expected moves for %s", ev.getInstigator().getId().name()));
				getTurn().setInitialPhase(true);
				getExpectedMoves().add(Requests.Request.BodyCase.BUILDROAD);
			}
			if (ev.getInstigator().getId() == getGame().getPlayer().getId()
					&& getExpectedMoves().contains(Requests.Request.BodyCase.BUILDSETTLEMENT))
			{
				getExpectedMoves().remove(Requests.Request.BodyCase.BUILDSETTLEMENT);
			}
			break;
		case RESOURCESTOLEN:
			if (ev.getInstigator().getId() == getGame().getPlayer().getId()
					&& getExpectedMoves().contains(Requests.Request.BodyCase.SUBMITTARGETPLAYER))
			{
				getExpectedMoves().remove(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
			}
			break;
		case PLAYERTRADE:
			if (getTurn().getPlayerTrade() != null
					&& getExpectedMoves().contains(Requests.Request.BodyCase.SUBMITTRADERESPONSE))
			{
				getExpectedMoves().remove(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
				getTurn().setPlayerTrade(null);
			}
			else
				getTurn().setPlayerTrade(ev.getPlayerTrade());
			break;
		case CARDSDISCARDED:
			if (ev.getInstigator().getId() == getGame().getPlayer().getId()
					&& getExpectedMoves().contains(Requests.Request.BodyCase.DISCARDRESOURCES))
			{
				getExpectedMoves().remove(Requests.Request.BodyCase.DISCARDRESOURCES);
			}
			break;
		case BEGINGAME:
			getTurn().setInitialPhase(true);
			if (getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()))
			{
				client.log("Client Play",
						String.format("Added BUILDSETTLEMENT move to player %s", ev.getInstigator().getId().name()));
				getExpectedMoves().add(Requests.Request.BodyCase.BUILDSETTLEMENT);
			}
			break;

		// No new expected moves
		default:
			break;
		}
	}

	/**
	 * Process the next message.
	 * 
	 * @throws IOException
	 */
	public void processMessage() throws Exception
	{
		Message msg = conn.getMessageFromServer();

		if (msg == null) return;

		// switch on message type
		switch (msg.getTypeCase())
		{
		// Extract and process event
		case EVENT:
			processEvent(msg.getEvent());
			break;
		}
	}

	private ClientGame getGame()
	{
		return client.getState();
	}

	private TurnState getTurn()
	{
		return client.getTurn();
	}

	private ConcurrentLinkedQueue<Requests.Request.BodyCase> getExpectedMoves()
	{
		return client.getTurn().getExpectedMoves();
	}
}
