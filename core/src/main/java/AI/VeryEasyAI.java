package AI;

import client.Turn;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.trade.Trade;

import java.util.HashMap;
import java.util.Map;

/**
 * This class assigns every move a rank of 0, so that the AICore treats all
 * moves as being equally valid. When it comes to 'selectMove,' a random element
 * of the list will be chosen.
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
		return 0;
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
		return 0;
	}

	@Override
	public int rankInitiateTrade(Turn turn)
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

	@Override
	public int rankTradeResponse(Trade.Response tradeResponse, Trade.WithPlayer trade)
	{
		return 0;
	}
}
