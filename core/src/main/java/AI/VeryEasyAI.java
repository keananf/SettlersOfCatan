package AI;

import client.Turn;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.build.Building;
import game.players.Player;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.board.Board;
import intergroup.trade.Trade;

import java.util.*;

/**
 * This class assigns nearly every move a rank of 0, so that the AICore treats all
 * moves as being equally valid. When it comes to 'selectMove,' a random element
 * of the list will be chosen.
 * Methods that DON't return 0 are so that the AI is able to show that it is capable
 * of moves of all types.
 */
public class VeryEasyAI extends AICore
{
	public VeryEasyAI(AIClient aiClient)
	{
		super(aiClient);
	}

	@Override
	public int rankBuyDevCard()
	{
		return 1;
	}

	@Override
	public int rankNewRoad(Edge chosenEdge)
	{
		return 0;
	}

	@Override
	public int rankNewSettlement(Node chosenNode)
	{
		return 0;
	}

	@Override
	public int rankNewCity(Node chosenNode)
	{
		return 0;
	}

	@Override
	public int rankNewRobberLocation(Hex chosenHex)
	{
		return 0;
	}

	@Override
	public int rankPlayDevCard(DevelopmentCardType chosenCard)
	{
		return 2;
	}

	@Override
	public int rankInitiateTrade(Turn turn)
	{
		Map<ResourceType, Integer> tradeReq = new HashMap<ResourceType, Integer>();
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		resources.putAll(getPlayer().getResources());
		List<ResourceType> want = getDesiredResources(resources);
		ResourceType maxResource = findMax(resources);
		ResourceType leastResource = null;
		Trade.WithPlayer.Builder trade = Trade.WithPlayer.newBuilder();

		// Set up the resources the player is wanting
		if(want.size() == 0)
		{
			// Request one of the resource that the player has the least of
			leastResource = findLeast(resources);
			tradeReq.put(leastResource, 1);
			trade.setWanting(getState().processResources(tradeReq));
		}
		else
		{
			// Simply choose a resource at random and add 1.
			leastResource = want.get(new Random().nextInt(want.size()));
			tradeReq.put(leastResource, 1);
			trade.setWanting(getState().processResources(tradeReq));
		}

		// Set up the resources that the player is offering
		tradeReq.clear();
		tradeReq.put(maxResource, 1);
		trade.setOffering(getState().processResources(tradeReq));

		// Find a player to trade with
		Board.Player other = findOther(leastResource);
		trade.setOther(other);

		// Update Turn:
		turn.setPlayerTrade(trade.build());
		return 0;
	}

	@Override
	public int rankTradeResponse(Trade.Response tradeResponse, Trade.WithPlayer trade)
	{
		return 0;
	}

	@Override
	public int rankDiscard(Turn turn)
	{
		int amount = getPlayer().getNumResources(), diff = amount / 2;
		Map<ResourceType, Integer> discard = new HashMap<ResourceType, Integer>();
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		resources.putAll(getPlayer().getResources());

		// Randomly assign resources to discard
		while (diff > 0)
		{
			ResourceType r = ResourceType.random();
			if (resources.containsKey(r) && resources.get(r) > 0)
			{
				resources.put(r, resources.get(r) - 1);
				discard.put(r, 1 + (discard.containsKey(r) ? discard.get(r) : 0));
				diff--;
			}
		}

		// Update Turn:
		turn.setChosenResources(discard);

		// Arbitrarily high, as this needs to be done immediately if necessary
		return 100000;
	}

	@Override
	public int rankTargetPlayer(Colour target)
	{
		return 0;
	}

	@Override
	public int rankChosenResource(ResourceType chosenResource)
	{
		return 0;
	}

	/**
	 * Finds a player to trade with based on what the user wants
	 * @param wanting the resources the user wants
	 * @return the player who should have some of this resource
	 */
	private Board.Player findOther(ResourceType wanting)
	{
		// Arbitrary starting colour
		Colour c = Colour.BLUE.equals(getPlayer().getColour()) ? Colour.RED : Colour.BLUE;

		// Loop through players
		for(Player p : getState().getPlayers().values())
		{
			boolean val = false;

			// Skip this player
			if(p.equals(getPlayer())) continue;

			// Loop through players settlements
			for(Building b : p.getSettlements().values())
			{
				// Check each hex this building is adjacent to
				for(Hex h : b.getNode().getHexes())
				{
					if(h.getResource().equals(wanting))
					{
						c = p.getColour();
						val = true;
						break;
					}
				}

				if(val) break;
			}

			if(val) break;
		}

		return Board.Player.newBuilder().setId(getState().getPlayer(c).getId()).build();
	}

	/**
	 * Ascertains which resource types the player does NOT have
	 * @param resources the player's resources
	 * @return a list of resource types that the player does NOT have
	 */
	private List<ResourceType> getDesiredResources(Map<ResourceType, Integer> resources)
	{
		List<ResourceType> want = new ArrayList<ResourceType>();

		// Ask for resources this player does NOT have
		for(ResourceType type : ResourceType.values())
		{
			// If the player has none of this resource
			if(!resources.containsKey(type) || resources.get(type) == 0)
			{
				want.add(type);
			}
		}
		return want;
	}

	/**
	 * Finds the resource that the player owns the most of
	 * @param resources the player's resources
	 * @return the max resource
	 */
	private ResourceType findMax(Map<ResourceType, Integer> resources)
	{
		int max = 0;
		ResourceType r = null;

		// Loop through player resources to find most common
		for(ResourceType type : resources.keySet())
		{
			if(resources.get(type) > max)
			{
				max = resources.get(type);
				r = type;
			}
		}

		return r;
	}

	/**
	 * Finds the resource that the player owns the least of
	 * @param resources the player's resources
	 * @return the least resource
	 */
	private ResourceType findLeast(Map<ResourceType, Integer> resources)
	{
		int min = 100;
		ResourceType r = null;

		// Loop through player resources to find least common
		for(ResourceType type : resources.keySet())
		{
			if(resources.get(type) < min)
			{
				min = resources.get(type);
				r = type;
			}
		}

		return r;
	}
	
	@Override
	public int rankEndTurn()
	{
		return 0;
	}
}
