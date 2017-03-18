package AI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import grid.BoardElement;
import grid.Node;
import grid.Edge;
import client.ClientGame;
import enums.Difficulty;
import enums.ResourceType;
import game.players.AIPlayer;

public abstract class AICore implements IAI
{
	Difficulty difficulty;
	ClientGame game;
	AIPlayer player;
	Map<ResourceType, Integer> pips;
	AIClient client;
	
	public AICore(Difficulty difficulty, AIClient client)
	{
		this.client = client;
		this.difficulty = difficulty;
		this.game = client.getState();
		this.player = (AIPlayer) game.getPlayer();
	}
	
	public ArrayList<MoveEntry> getMoves()
	{
		ArrayList<MoveEntry> moves = new ArrayList<MoveEntry>();
		
		//TODO: add empty move
		
		if(client.getMoveProcessor.checkBuyDevCard())
		{
			//create MoveEntry for buying a dev card
			//add MoveEntry to list
		}
		
		//TODO: check if a player can play a development card
		
		ArrayList<BoardElement> elements = client.getMoveProcessor().getBuildingPossibilities();
		
		for(BoardElement e: elements)
		{
			if(e instanceof Node)
			{
				if(player.canBuildCity((Node) e))
				{
					//create MoveEntry for building city
					//add MoveEntry to list
				}
				else if(player.canBuildSettlement((Node) e))
				{
					//create MoveEntry for buildiung a settlement
					//add MoveEntry to list
				}
			}
			else if(e instanceof Edge)
			{
				if(player.canBuildRoad((Edge) e))
				{
					//create MoveEntry for building a road
					//add MoveEntry to list
				}
			}
		}
		
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
	
	public Node getSettlementNode()
	{
		Node buildNode = null;
		
		//get all BoardElements
		ArrayList<BoardElement> elements = client.getMoveProcessor().getBuildingPossibilities();
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		//take out the nodes
		for(BoardElement e: elements)
		{
			if(e instanceof Node)
			{
				nodes.add((Node) e);
			}
		}
		
		if(nodes.size() > 0)
		{
			Random ran = new Random();
			
			buildNode = nodes.get(ran.nextInt(nodes.size()));
		}
		
		
		
		return buildNode;
	}
	
	public Edge getRoadEdge(Node node)
	{
		Edge roadEdge = null;
		
		ArrayList<Edge> edges = (ArrayList<Edge>) node.getEdges();
		
		ArrayList<Edge> validEdges = new ArrayList<Edge>();
		
		for(Edge e: edges)
		{
			if(e.getRoad() == null)
			{
				validEdges.add(e);
			}
		}
		
		if(validEdges.size() > 0)
		{
			Random ran = new Random();
			roadEdge = validEdges.get(ran.nextInt(validEdges.size()));
		}
				
		return roadEdge;
	}
	
}
