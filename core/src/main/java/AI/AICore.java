package AI;

import java.util.Map;

import client.ClientGame;
import enums.Difficulty;
import enums.ResourceType;
import game.players.AIPlayer;

public abstract class AICore 
{
	Difficulty difficulty;
	ClientGame game;
	AIPlayer player;
	Map<ResourceType, Integer> pips;
	
	public AICore(Difficulty difficulty, ClientGame game, AIPlayer player)
	{
		this.difficulty = difficulty;
		this.game = game;
		this.player = player;
	}
	
	
}
