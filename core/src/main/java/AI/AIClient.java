package AI;

import catan.SettlersOfCatan;
import client.Client;
import enums.Difficulty;
import intergroup.Events;

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
		assignAI(Difficulty.VERYEASY);
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
		active = true;
		Thread aiThread = new Thread(ai);
		aiThread.start();

		// Loop processing events when needed and sending turns
		while (active && (getState() == null || !getState().isOver()))
		{
			try
			{
				Events.Event ev = acquireLocksAndGetEvents();
				if (ai.getExpectedEvents().contains(ev.getTypeCase())
						|| ev.getTypeCase().equals(Events.Event.TypeCase.ERROR))
				{
					log("Client Proc", "Resuming");
					ai.resume();
				}

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
	protected boolean acquireLocksAndPerformMove() throws Exception
	{
		boolean val = false;
		try
		{
			getStateLock().acquire();
			try
			{
				getTurnLock().acquire();
				try
				{
					val = ai.performMove();
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

		return val;
	}
}
