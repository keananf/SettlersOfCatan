package AI;

import client.Client;
import enums.Difficulty;

public abstract class AIClient extends Client
{
	protected AICore AI;

	public AIClient(Difficulty difficulty)
	{
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
	}

	public AIClient()
	{
		AI = new RandomAI(this);
	}

	@Override
	public void shutDown()
	{
		super.shutDown();
	}
}
