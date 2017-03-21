package AI;

import client.Turn;

import java.util.List;
import java.util.Random;

public class RandomAI extends AICore
{
	private Random rand;

	public RandomAI(AIClient aiClient)
	{
		super(aiClient);
		rand = new Random();
	}

	@Override
	public int rankMove(Turn turn)
	{
		return 0;
	}

	@Override
	public Turn selectMove(List<Turn> optimalMoves)
	{
		return optimalMoves.get(rand.nextInt(optimalMoves.size()));
	}
}
