package client;

import connection.IServerConnection;
import intergroup.Events.Event;
import intergroup.Messages.Message;
import intergroup.Requests;
import server.Logger;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class which continuously listens for updates from the server Created by
 * 140001596
 */
public class EventProcessor implements Runnable
{
	private final TurnInProgress turn;
	private final Client client;
	private ClientGame game;
	private final IServerConnection conn;
	private Logger logger;
	private ConcurrentLinkedQueue<Requests.Request.BodyCase> expectedMoves;
	private boolean robberMoved;

	public EventProcessor(IServerConnection conn, Client client)
	{
		this.turn = client.getTurn();
		expectedMoves = turn.getExpectedMoves();
		this.conn = conn;
		logger = new Logger();
		this.client = client;
	}

	@Override
	public void run()
	{
		System.out.println("Starting event processor");

		// Continuously wait for new messages from the server
		while (game == null || !game.isOver())
		{
			try
			{
				processMessage();
			}
			catch (IOException e)
			{
				// Fatal error
				break;
			}
			catch (Exception e)
			{
				// Error. Invalid event.
				// TODO request state from server? Or fail?
				e.printStackTrace();
			}

		}
	}

    /**
     * Processes the event received from the server and updates the game state
     * @param ev the event
     */
    private void processEvent(Event ev) throws Exception
    {
		updateExpectedMoves(ev);
		System.out.println("Processing event");

        // Switch on type of event
        switch(ev.getTypeCase())
		{
			case GAMEWON:
				game.setGameOver();
				break;
			case TURNENDED:
				turn.setTradePhase(false);
				game.setCurrentPlayer(game.getNextPlayer());
				break;
			case CITYBUILT:
				game.processNewCity(ev.getCityBuilt(),ev.getInstigator());
				break;
			case SETTLEMENTBUILT:
				game.processNewSettlement(ev.getSettlementBuilt(),ev.getInstigator());
				break;
			case ROADBUILT:
				game.processRoad(ev.getRoadBuilt(),ev.getInstigator());
				break;
			case ROLLED:
				game.processDice(ev.getRolled().getA()+ev.getRolled().getB(), ev.getRolled().getResourceAllocationList());
				break;
			case ROBBERMOVED:
				game.moveRobber(ev.getRobberMoved());
				break;
			case DEVCARDBOUGHT:
				game.recordDevCard(ev.getDevCardBought(),ev.getInstigator());
				break;
			case DEVCARDPLAYED:
				game.processPlayedDevCard(ev.getDevCardPlayed(),ev.getInstigator());
				break;
			case BEGINGAME:
				game = new ClientGame();
				game.setBoard(ev.getBeginGame());
				break;
			case CHATMESSAGE:
				game.writeMessage(ev.getChatMessage(), ev.getInstigator());
				break;
			case BANKTRADE:
				turn.setTradePhase(true);
				game.processBankTrade(ev.getBankTrade(), ev.getInstigator());
				break;
			case PLAYERTRADE:
				turn.setTradePhase(true);
				if(turn.getPlayerTrade() != null)
				{
					game.processPlayerTrade(ev.getPlayerTrade(), ev.getInstigator());
				}
				break;
			case LOBBYUPDATE:
				game.processPlayers(ev.getLobbyUpdate(), ev.getInstigator());
				break;
			case CARDSDISCARDED:
				game.processDiscard(ev.getCardsDiscarded(), ev.getInstigator());
				break;
			case MONOPOLYRESOLUTION:
				game.processMonopoly(ev.getMonopolyResolution(), ev.getInstigator());
				break;
			case RESOURCECHOSEN:
				game.processResourceChosen(ev.getResourceChosen(), ev.getInstigator());
				break;
			case RESOURCESTOLEN:
				game.processResourcesStolen(ev.getResourceStolen(), ev.getInstigator());
				break;
			case ERROR:
				// TODO display error?
				break;
		}
    }

	private void updateExpectedMoves(Event ev)
	{
		// Add roll dice if necessary
		if(game.getCurrentPlayer().equals(game.getPlayer().getColour()) && ev.getTypeCase().equals(Event.TypeCase.TURNENDED))
		{
			expectedMoves.add(Requests.Request.BodyCase.ROLLDICE);
		}

		// If not this player's turn,
		if(!ev.hasInstigator() || (ev.getInstigator().getId() != game.getPlayer().getId() && !ev.getTypeCase().equals(Event.TypeCase.ROLLED)))
		{
			return;
		}

		// Switch on request type
		switch(ev.getTypeCase())
		{
			// Add expected moves based on recently played dev card
			case DEVCARDPLAYED:
			{
				switch(ev.getDevCardPlayed())
				{
					case KNIGHT:
						expectedMoves.add(Requests.Request.BodyCase.MOVEROBBER);
						break;
					case YEAR_OF_PLENTY:
						expectedMoves.add(Requests.Request.BodyCase.CHOOSERESOURCE);
						expectedMoves.add(Requests.Request.BodyCase.CHOOSERESOURCE);
						break;
					case ROAD_BUILDING:
						expectedMoves.add(Requests.Request.BodyCase.BUILDROAD);
						expectedMoves.add(Requests.Request.BodyCase.BUILDROAD);
						break;
					case MONOPOLY:
						expectedMoves.add(Requests.Request.BodyCase.CHOOSERESOURCE);
						break;
				}
				break;
			}

			case ROLLED:
				int dice = ev.getRolled().getA() + ev.getRolled().getB();
				if(dice == 7)
				{
					expectedMoves.add(Requests.Request.BodyCase.DISCARDRESOURCES);
					if(ev.getInstigator().getId() == game.getPlayer().getId())
					{
						expectedMoves.add(Requests.Request.BodyCase.MOVEROBBER);
					}
				}
				break;

			// Expect for the player to send a steal request next
			case ROBBERMOVED:
				expectedMoves.add(Requests.Request.BodyCase.CHOOSERESOURCE);
				robberMoved = true;
				if(expectedMoves.contains(Requests.Request.BodyCase.MOVEROBBER))
				{
					expectedMoves.remove(Requests.Request.BodyCase.MOVEROBBER);
				}
				break;

			// Remove expected moves if necessary
			case MONOPOLYRESOLUTION:
			case RESOURCECHOSEN:
				if(robberMoved)
				{
					expectedMoves.add(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
					robberMoved = false;
				}
				if(expectedMoves.contains(Requests.Request.BodyCase.CHOOSERESOURCE))
				{
					expectedMoves.remove(Requests.Request.BodyCase.CHOOSERESOURCE);
				}
				break;
			case ROADBUILT:
				if(expectedMoves.contains(Requests.Request.BodyCase.BUILDROAD))
				{
					expectedMoves.remove(Requests.Request.BodyCase.BUILDROAD);
				}
				break;
			case RESOURCESTOLEN:
				if(expectedMoves.contains(Requests.Request.BodyCase.SUBMITTARGETPLAYER))
				{
					expectedMoves.remove(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
				}
				break;
			case PLAYERTRADE:
				if(turn.getPlayerTrade() != null && expectedMoves.contains(Requests.Request.BodyCase.SUBMITTRADERESPONSE))
				{
					expectedMoves.remove(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
					turn.setPlayerTrade(null);
				}
				else turn.setPlayerTrade(ev.getPlayerTrade());
				break;
			case CARDSDISCARDED:
				if(expectedMoves.contains(Requests.Request.BodyCase.DISCARDRESOURCES))
				{
					expectedMoves.remove(Requests.Request.BodyCase.DISCARDRESOURCES);
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
	private void processMessage() throws Exception
	{
		System.out.println("Waiting");
		Message msg = conn.getMessageFromServer();
		System.out.println("Processing");
		logger.logReceivedMessage(msg);

		// switch on message type
		switch (msg.getTypeCase())
		{
			// Extract and process event
			case EVENT:
				processEvent(msg.getEvent());
				break;
		}
	}
}
