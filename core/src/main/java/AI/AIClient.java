package AI;

import client.Client;
import enums.Difficulty;

public abstract class AIClient extends Client
{
	private Thread thread;
	protected AICore AI;

	public AIClient(Difficulty difficulty)
	{
		setUpConnection();
		switch(difficulty)
		{
			case EASY:
				AI = new EasyAI(this);
				break;

			case VERYEASY:
			default:
				AI = new RandomAI(this);
				break;
		}
		thread = new Thread(AI);
		thread.start();
	}

	public AIClient()
	{
		setUpConnection();
		AI = new RandomAI(this);
		thread = new Thread(AI);
		thread.start();
	}

	@Override
	public void shutDown()
	{
		super.shutDown();
		try
		{
			thread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
