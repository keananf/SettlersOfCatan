package AI;

import client.Client;
import enums.AIDifficulty;

public abstract class AIClient extends Client
{
	protected AICore AI;

	public AIClient(AIDifficulty difficulty)
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
	}

	public AIClient()
	{
		setUpConnection();
		AI = new RandomAI(this);
	}
}
