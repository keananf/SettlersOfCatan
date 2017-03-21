package AI;

import client.Turn;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.trade.Trade;

/**
 * This class assigns every move a rank of 0, so that the AICore treats
 * all moves as being equally valid. When it comes to 'selectMove,'
 * a random element of the list will be chosen.
 */
public class RandomAI extends AICore
{
	public RandomAI(AIClient aiClient)
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
		return 0;
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
