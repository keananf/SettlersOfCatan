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
 * Created by 140002949 on 19/03/17.
 */
public class EasyAI extends AICore
{
    public EasyAI(AIClient client)
    {
        super(client);
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

	@Override
	public int rankEndTurn() 
	{
		return 0;
	}
}
