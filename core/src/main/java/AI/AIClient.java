package AI;

import catan.SettlersOfCatan;
import client.Client;
import enums.Difficulty;
import intergroup.Events;

public abstract class AIClient extends Client
{
	private AICore ai;
	private static final String[] names = {"Cheswick", "Alice", "Bob", "Evelyn", "James", "Gary", "Frank", "Helen", "Iris", "Kevin"};

	AIClient(Difficulty difficulty, String userName, SettlersOfCatan game)
	{
		super(game, userName);
		assignAI(difficulty);
	}

	AIClient()
	{
		super(names[(int)Math.floor(Math.random() * 10)]);
		assignAI(Difficulty.VERYEASY);
	}

	AIClient(Difficulty difficulty)
	{
		super(names[(int)Math.floor(Math.random() * 10)]);
		assignAI(difficulty);
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
						|| ev.getTypeCase().equals(Events.Event.TypeCase.ERROR)
						|| ev.getTypeCase().equals(Events.Event.TypeCase.ALLRESOURCES))
				{
					if (!ev.getTypeCase().equals(Events.Event.TypeCase.CARDSDISCARDED)
							|| ev.getInstigator().getId().getNumber() == getPlayer().getId().getNumber())
					{
						log("Client Proc", "Resuming");
						ai.resume();
					}
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
	boolean acquireLocksAndPerformMove()
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
