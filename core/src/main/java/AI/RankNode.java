package AI;

/**
 * Created by 140002949 on 22/03/17.
 */

import enums.ResourceType;
import game.players.Player;
import grid.Hex;
import grid.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class RankNode
{
	Player player;

	Node node;
	int ranking = 0;
	ResourceType[] stdResourceList = new ResourceType[] { ResourceType.Brick, ResourceType.Lumber, ResourceType.Grain,
			ResourceType.Wool, ResourceType.Ore };
	ArrayList<ResourceType> resourceQueue = new ArrayList<>(Arrays.asList(stdResourceList));// change
																							// to
																							// queue
																							// if
																							// wanted

	LinkedList<Hex> surroundingHexes = new LinkedList<Hex>();

	public RankNode(Node node, Player player)
	{
		this.player = player;
		this.node = node;
		surroundingHexes = (LinkedList<Hex>) node.getHexes();
	}

	public void rank(boolean preRound)
	{
		if (!preRound)
		{
			ArrayList<ResourceType> temp = new ArrayList<>();

			int max = 2;
			int min = 1;

			for (ResourceType key : player.getResources().keySet())
			{
				if (player.getResources().get(key) <= min)
				{
					temp.add(0, key);
					min = player.getResources().get(key);
				}
				else if (player.getResources().get(key) >= max)
				{
					temp.add(key);
					max = player.getResources().get(key);
				}
				else
				{
					temp.add(1, key);
				}
			}
			resourceQueue = temp;
		}
		addHexesRank();
		addResourceRank();
		addLikelihoodRank();
	}

	public void addHexesRank()
	{
		ranking += surroundingHexes.size();
	}

	public void deprioritiseResource(ResourceType r)
	{
		resourceQueue.remove(r);// remove it from the front
		resourceQueue.add(r); // add it to the back
	}

	public void addResourceRank()
	{
		// priority of resource, with a focus on building settlements and roads

		for (Hex hex : surroundingHexes)
		{
			ResourceType rt = hex.getResource();

			if (rt == ResourceType.Generic)
			{
				ranking--;
			}
			else if (resourceQueue.size() >= 1 && rt == resourceQueue.get(0))
			{
				ranking += 4;
			}
			else if (resourceQueue.size() >= 2 && rt == resourceQueue.get(1))
			{
				ranking += 3;
			}
			else if (resourceQueue.size() >= 3 && rt == resourceQueue.get(2))
			{
				ranking += 2;
			}
			else if (resourceQueue.size() >= 4 && rt == resourceQueue.get(3))
			{
				ranking += 1;
			}
		}
	}

	public void addLikelihoodRank()
	{
		for (Hex hex : surroundingHexes)
		{
			int likelihood = Math.abs(hex.getChit() - 7); // difference between
															// diceRoll on hex
															// and 7
			int rank = 7 - likelihood; // the lower the difference, the higher
										// the rank
			ranking += rank;
		}
	}

	public int getRanking()
	{
		return ranking;
	}

	public Node getNodeOnBoard(ArrayList<Node> nodes)
	{
		for (Node n : nodes)
		{
			if (this.node.equals(n)) { return n; }
		}
		return null;
	}

}
