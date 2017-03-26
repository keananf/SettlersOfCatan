package client;

import com.badlogic.gdx.Gdx;
import connection.IServerConnection;

import java.util.concurrent.Semaphore;

/**
 * Abstract notion of a client
 * 
 * @author 140001596
 */
public abstract class Client implements Runnable
{
	protected ClientGame state;
	protected EventProcessor eventProcessor;
	protected TurnProcessor turnProcessor;
	protected MoveProcessor moveProcessor;
	protected static final int PORT = 12345;
	private Turn turn;
	private IServerConnection conn;
	private Semaphore stateLock, turnLock;
	private Thread thread;

	public Client()
	{
		setUpConnection();
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run()
	{
		// Loop processing events when needed and sending turns
		while (getState() == null || !getState().isOver())
		{
			try
			{
				acquireLocksAndGetEvents();
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		log("Client Play", "Ending AI client loop");
	}

	/**
	 * Acquires locks and attempts to process an event
	 */
	protected void acquireLocksAndGetEvents() throws Exception
	{
		try
		{
			getStateLock().acquire();
			try
			{
				getTurnLock().acquire();
				try
				{
					eventProcessor.processMessage();
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
				getStateLock().release();
			}
			Thread.sleep(100);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the different components for a RemoteClient
	 */
	protected void setUp(IServerConnection conn)
	{
		this.conn = conn;
		this.stateLock = new Semaphore(1);
		this.turnLock = new Semaphore(1);
		this.turn = new Turn();
		this.turnProcessor = new TurnProcessor(conn, this);
		this.moveProcessor = new MoveProcessor(this);
		this.eventProcessor = new EventProcessor(conn, this);
	}

	/**
	 * Updates the client's turn object
	 * 
	 * @param selectedMove this move and corresponding information
	 */
	public void updateTurn(Turn selectedMove)
	{
		// Reset and set chosen field
		getTurn().resetInfo();
		getTurn().setChosenMove(selectedMove.getChosenMove());

		// Set additional fields
		switch (selectedMove.getChosenMove())
		{
		case SUBMITTRADERESPONSE:
			getTurn().setTradeResponse(selectedMove.getTradeResponse());
			break;
		case CHOOSERESOURCE:
			getTurn().setChosenResource(selectedMove.getChosenResource());
			break;
		case MOVEROBBER:
			getTurn().setChosenHex(selectedMove.getChosenHex());
			break;
		case PLAYDEVCARD:
			getTurn().setChosenCard(selectedMove.getChosenCard());
			break;
		case BUILDROAD:
			getTurn().setChosenEdge(selectedMove.getChosenEdge());
			break;
		case CHATMESSAGE:
			getTurn().setChatMessage(selectedMove.getChatMessage());
			break;
		case DISCARDRESOURCES:
			getTurn().setChosenResources(selectedMove.getChosenResources());
			break;
		case INITIATETRADE:
			getTurn().setPlayerTrade(selectedMove.getPlayerTrade());
			break;
		case SUBMITTARGETPLAYER:
			getTurn().setTarget(selectedMove.getTarget());
		case BUILDSETTLEMENT:
		case BUILDCITY:
			getTurn().setChosenNode(selectedMove.getChosenNode());
			break;

		// Empty request bodies
		case JOINLOBBY:
		case ROLLDICE:
		case ENDTURN:
		case BUYDEVCARD:
		default:
			break;
		}
	}

	/**
	 * Sends the given turn object.
	 *
	 * Method assumed to be called in thread safe manner
	 * 
	 * @param turn the turn to send
	 */
	public void sendTurn(Turn turn)
	{
		// Send to server if it is a valid move
		if (getMoveProcessor().validateMsg(turn))
		{
			updateTurn(turn);
			turnProcessor.sendMove();
		}
		else
		{
			// TODO else display error?
			log("Client Play", String.format("Invalid Request %s for %s", turn.getChosenMove().name(),
					getState().getPlayer().getId().name()));
		}
	}

	/**
	 * Sends the given turn object.
	 *
	 * Method assumed to be called in thread safe manner
	 * 
	 * @param turn the turn to send
	 */
	public void acquireLocksAndSendTurn(Turn turn)
	{
		try
		{
			getStateLock().acquire();
			try
			{
				getTurnLock().acquire();
				try
				{
					sendTurn(turn);
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
				getStateLock().release();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Shuts down a client by terminating the socket and the event processor
	 * thread.
	 */
	public void shutDown()
	{
		conn.shutDown();
		try
		{
			thread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Logs the given message
	 * 
	 * @param tag the tag (for Gdx)
	 * @param msg the message to log
	 */
	public void log(String tag, String msg)
	{
		if (Gdx.app == null)
		{
			System.out.println(tag + ": " + msg);
		}
		else
			Gdx.app.log(tag, msg);
	}

	public ClientGame getState()
	{
		return state;
	}

	public Turn getTurn()
	{
		return turn;
	}

	public MoveProcessor getMoveProcessor()
	{
		return moveProcessor;
	}

	public void setGame(ClientGame game)
	{
		this.state = game;
	}

	public Semaphore getStateLock()
	{
		return stateLock;
	}

	public Semaphore getTurnLock()
	{
		return turnLock;
	}

	/**
	 * Attempts to set up a connection with the server
	 */
	protected abstract void setUpConnection();
}
