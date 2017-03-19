package AI;

import client.ClientGame;
import enums.Difficulty;
import game.players.AIPlayer;

public class VeryEasyAI extends AICore {

	public VeryEasyAI(Difficulty difficulty, ClientGame game, AIPlayer player) 
	{
		super(difficulty, game, player);
	}

}
