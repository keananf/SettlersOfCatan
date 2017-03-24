package AI;

import client.Client;
import enums.Difficulty;

public abstract class AIClient extends Client
{
	protected AICore ai;

	public AIClient(Difficulty difficulty)
	{
		super();
		switch (difficulty)
		{
		case EASY:
			ai = new EasyAI(this);
			break;

		case VERYEASY:
		default:
			ai = new VeryEasyAI(this);
			break;
		}
	}

	public AIClient()
	{
		super();
		ai = new VeryEasyAI(this);
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

				// Attempt to make a move and send a turn
				acquireLocksAndPerformMove();
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
}
