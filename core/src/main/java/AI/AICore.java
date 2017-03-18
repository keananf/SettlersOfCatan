package AI;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

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
	
	public ArrayList<MoveEntry> getMoves()
	{
		ArrayList<MoveEntry> moves = new ArrayList<MoveEntry>();
		
		//TODO: wait for pull request
		
		return moves;
	}
	
	public ArrayList<MoveEntry> rankMoves(ArrayList<MoveEntry> moves)
	{
		ArrayList<MoveEntry> ranked = moves;
		
		Random ran = new Random();
		
		int rank;
		
		for(MoveEntry move: ranked)
		{
			rank = ran.nextInt(ranked.size());
			
			move.setRank(rank);
		}
		
		return ranked;
	}
	
	public MoveEntry selectMove(ArrayList<MoveEntry> moves)
	{
		MoveEntry selectedMove = null;
		int maxRank = -1;
		ArrayList<MoveEntry> optimalMoves = new ArrayList<MoveEntry>();
		
		for(MoveEntry entry : moves)
		{
			if(entry.getRank() > maxRank)
			{
				maxRank = entry.getRank();
				optimalMoves.clear();
				optimalMoves.add(entry);
			}
			else if(entry.getRank() == maxRank)
			{
				optimalMoves.add(entry);
			}
		}
		
		if(optimalMoves.size() > 1)
		{
			Random r = new Random();
			selectedMove = optimalMoves.get(r.nextInt(optimalMoves.size() - 1));		
		}
	
		
		return selectedMove;
		
	}
	
	public void updateGameState(ClientGame game)
	{
		this.game = game;
	}
	
}
