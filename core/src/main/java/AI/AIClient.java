package AI;

import catan.SettlersOfCatan;
import client.Client;
import enums.Difficulty;

public abstract class AIClient extends Client
{
	protected AICore ai;

	public AIClient(Difficulty difficulty, SettlersOfCatan game)
	{
		super(game);
		assignAI(difficulty);
	}

	public AIClient()
	{
		super();
		ai = new VeryEasyAI(this);
	}

	public AIClient(Difficulty difficulty)
	{
		super();
		assignAI(difficulty);
	}

	public AIClient(SettlersOfCatan game)
	{
		super(game);
		ai = new VeryEasyAI(this);
	}

	private void assignAI(Difficulty difficulty)
	{
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

	@Override
	public void run()
	{
		log("Client Play", "Starting AI client loop");
		active = true;

		// Loop processing events when needed and sending turns
		while (active && (getState() == null || !getState().isOver()))
		{
			try
			{
				// Attempt to make a move and send a turn
				acquireLocksAndPerformMove();
				Thread.sleep(100);

				acquireLocksAndGetEvents();
				Thread.sleep(100);
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
