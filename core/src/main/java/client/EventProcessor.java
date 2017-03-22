package client;

import connection.IServerConnection;
import intergroup.Events.Event;
import intergroup.Messages.Message;
import intergroup.Requests;
import intergroup.board.Board;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Class which continuously listens for updates from the server Created by
 * 140001596
 */
public class EventProcessor implements Runnable
{
	private final Client client;
	private IServerConnection conn;
	private boolean robberMoved;

	public EventProcessor(IServerConnection conn, Client client)
	{
		this.conn = conn;
		this.client = client;
	}

	@Override
	public void run()
	{
		client.log("Event Proc","Starting event processor");

		// Continuously wait for new messages from the server
		while (getGame() == null || !getGame().isOver())
		{
			// Break
			if(conn == null) break;

			try
			{
				processMessage();
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
				// Error. Invalid event.
				// TODO request state from server? Or fail?
				conn = null;
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
		client.log("Event Proc", String.format("Processing event %s", ev.getTypeCase().name()));

        // Switch on type of event
        switch(ev.getTypeCase())
		{
			case GAMEWON:
				getGame().setGameOver();
				break;
			case TURNENDED:
				getGame().setCurrentPlayer(getTurn().isInitialPhase());
				getTurn().reset();
				break;
			case CITYBUILT:
				getGame().processNewCity(ev.getCityBuilt(),ev.getInstigator());
				break;
			case SETTLEMENTBUILT:
				getGame().processNewSettlement(ev.getSettlementBuilt(),ev.getInstigator());
				break;
			case ROADBUILT:
				getGame().processRoad(ev.getRoadBuilt(),ev.getInstigator());
				break;
			case ROLLED:
				int roll = ev.getRolled().getA()+ev.getRolled().getB();
				getTurn().setTurnStarted(true);
				getTurn().setRoll(roll);
				getGame().processDice(roll, ev.getRolled().getResourceAllocationList());
				break;
			case ROBBERMOVED:
				getGame().moveRobber(ev.getRobberMoved());
				break;
			case DEVCARDBOUGHT:
				getGame().recordDevCard(ev.getDevCardBought(),ev.getInstigator());
				break;
			case DEVCARDPLAYED:
				getGame().processPlayedDevCard(ev.getDevCardPlayed(),ev.getInstigator());
				break;
			case BEGINGAME:
				client.setGame(new ClientGame());
				getGame().setBoard(ev.getBeginGame());
				client.log("Event Proc", String.format("Game information received. \tPlayer Id: %s", getGame().getPlayer().getId().name()));
				break;
			case GAMEINFO:
				client.setGame(new ClientGame());
				getGame().processGameInfo(ev.getGameInfo());
				client.log("Event Proc", String.format("Game information received. \tPlayer Id: %s", getGame().getPlayer().getId().name()));
			case CHATMESSAGE:
				getGame().writeMessage(ev.getChatMessage(), ev.getInstigator());
				break;
			case BANKTRADE:
				getTurn().setTradePhase(true);
				getGame().processBankTrade(ev.getBankTrade(), ev.getInstigator());
				break;
			case PLAYERTRADE:
				getTurn().setTradePhase(true);
				if(getTurn().getPlayerTrade() != null)
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
		if(getGame() == null || ev == null || ev.getTypeCase().equals(Event.TypeCase.TYPE_NOT_SET)) return;

		// Add roll dice if necessary
		if(getGame().getPlayer() != null && getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()) &&
				ev.getTypeCase().equals(Event.TypeCase.TURNENDED))
		{
			getExpectedMoves().add(Requests.Request.BodyCase.ROLLDICE);
		}

		// If not this player's turn,
		if((!ev.hasInstigator() && !ev.getTypeCase().equals(Event.TypeCase.BEGINGAME)) || (ev.getInstigator().getId() != getGame().getPlayer().getId() &&
				!(ev.getTypeCase().equals(Event.TypeCase.ROLLED) || ev.getTypeCase().equals(Event.TypeCase.PLAYERTRADE))))
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
				break;
			}

			case ROLLED:
				int dice = ev.getRolled().getA() + ev.getRolled().getB();
				if(dice == 7)
				{
					if(getGame().getPlayer().getNumResources() > 7)
					{
						getExpectedMoves().add(Requests.Request.BodyCase.DISCARDRESOURCES);
					}
					if(ev.getInstigator().getId() == getGame().getPlayer().getId())
					{
						getExpectedMoves().add(Requests.Request.BodyCase.MOVEROBBER);
					}
				}
				break;

			// Expect for the player to send a steal request next
			case ROBBERMOVED:
				getExpectedMoves().add(Requests.Request.BodyCase.CHOOSERESOURCE);
				robberMoved = true;
				if(getExpectedMoves().contains(Requests.Request.BodyCase.MOVEROBBER))
				{
					getExpectedMoves().remove(Requests.Request.BodyCase.MOVEROBBER);
				}
				break;

			// Remove expected moves if necessary
			case MONOPOLYRESOLUTION:
			case RESOURCECHOSEN:
				if(robberMoved)
				{
					getExpectedMoves().add(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
					robberMoved = false;
				}
				if(getExpectedMoves().contains(Requests.Request.BodyCase.CHOOSERESOURCE))
				{
					getExpectedMoves().remove(Requests.Request.BodyCase.CHOOSERESOURCE);
				}
				break;
			case ROADBUILT:
				if(getTurn().isInitialPhase() && getGame().getPlayer().getSettlements().size() < 2 &&
						ev.getInstigator().getId().equals(Board.Player.Id.forNumber(getGame().getPlayers().size() - 1)))
				{
					getExpectedMoves().add(Requests.Request.BodyCase.BUILDSETTLEMENT);
				}
				else if(ev.getInstigator().getId().equals(Board.Player.Id.forNumber(getGame().getPlayers().size() - 1)))
				{
					getTurn().setInitialPhase(false);
				}
				if(getExpectedMoves().contains(Requests.Request.BodyCase.BUILDROAD))
				{
					getExpectedMoves().remove(Requests.Request.BodyCase.BUILDROAD);
				}
				break;
			case SETTLEMENTBUILT:
				if(getGame().getPlayer().getRoads().size() < 2)
				{
					client.log("", String.format("Adding BUILDROAD to expected moves for player %s", ev.getInstigator().getId().name()));
					getTurn().setInitialPhase(true);
					getExpectedMoves().add(Requests.Request.BodyCase.BUILDROAD);
				}
				if(getExpectedMoves().contains(Requests.Request.BodyCase.BUILDSETTLEMENT))
				{
					getExpectedMoves().remove(Requests.Request.BodyCase.BUILDSETTLEMENT);
				}
				break;
			case RESOURCESTOLEN:
				if(getExpectedMoves().contains(Requests.Request.BodyCase.SUBMITTARGETPLAYER))
				{
					getExpectedMoves().remove(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
				}
				break;
			case PLAYERTRADE:
				if(getTurn().getPlayerTrade() != null && getExpectedMoves().contains(Requests.Request.BodyCase.SUBMITTRADERESPONSE))
				{
					getExpectedMoves().remove(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
					getTurn().setPlayerTrade(null);
				}
				else getTurn().setPlayerTrade(ev.getPlayerTrade());
				break;
			case CARDSDISCARDED:
				if(getExpectedMoves().contains(Requests.Request.BodyCase.DISCARDRESOURCES))
				{
					getExpectedMoves().remove(Requests.Request.BodyCase.DISCARDRESOURCES);
				}
				break;
			case BEGINGAME:
				getTurn().setInitialPhase(true);
				if(getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()))
				{
					client.log("Client Play", String.format("Added BUILDSETTLEMENT move to player %s", ev.getInstigator().getId().name()));
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
	private void processMessage() throws Exception
	{
		Message msg = conn.getMessageFromServer();

		if(msg == null) return;

		// switch on message type
		switch (msg.getTypeCase())
		{
			// Extract and process event
			case EVENT:
				try
				{
					getGameLock().acquire();
					try
					{
						getTurnLock().acquire();
						try
						{
							processEvent(msg.getEvent());
						}
						finally
						{
							getTurnLock().release();
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					finally
					{
						getGameLock().release();
					}

				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				break;
		}
	}

	private ClientGame getGame()
	{
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
