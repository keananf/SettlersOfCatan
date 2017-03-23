package AI;

import client.Client;
import enums.Difficulty;

public abstract class AIClient extends Client
{
	protected AICore ai;

	public AIClient(Difficulty difficulty)
	{
		super();
		switch(difficulty)
		{
			case EASY:
				ai = new EasyAI(this);
				break;

			case VERYEASY:
			default:
				ai = new RandomAI(this);
				break;
		}
	}

	public AIClient()
	{
		super();
		ai = new RandomAI(this);
	}

	@Override
	public void run()
	{
		// Loop processing events when needed and sending turns
		while(getState() == null || !getState().isOver())
		{
			try
			{
				acquireLocksAndGetEvents();

				if(getState() != null)
				log("Client Play", String.format("Client expected moves %s %s", getState().getPlayer().getId().name(), getTurn().getExpectedMoves().toString()));

				// Sleep while it is NOT your turn and while you do not have expected moves
				Thread.sleep(100);

				// Attempt to make a move and send a turn
				acquireLocksAndPerformMove();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				shutDown();
			}
		}
		log("Client Play", "Ending AI client loop");
	}

	/**
	 * Acquires locks and attempts to move
	 */
	protected void acquireLocksAndPerformMove() throws Exception
	{
		try
		{
			getStateLock().acquire();
			try
			{
				getTurnLock().acquire();
				try
				{
					ai.performMove();
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
				getStateLock().release();
			}
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
