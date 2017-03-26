package AI;

import client.Turn;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.players.Player;
import grid.Edge;
import grid.Hex;
import grid.Node;
import grid.Port;
import intergroup.trade.Trade;

import java.util.Map;

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
    public int rankNewSettlement(Node chosenNode) {
        RankNode rankNode = new RankNode(chosenNode, super.getPlayer());
        rankNode.rank(false);
        return rankNode.getRanking();

    }

    @Override
    public int rankInitialSettlement(Node chosenNode) {
        RankNode rankNode = new RankNode(chosenNode, super.getPlayer());
        rankNode.rank(true);
        return rankNode.getRanking();
    }

    @Override
    public int rankNewCity(Node chosenNode) {
        RankNode rankNode = new RankNode(chosenNode, super.getPlayer());
        rankNode.rank(false);
        return rankNode.getRanking();
    }

    @Override
    public int rankNewRobberLocation(Hex chosenHex) {
        RankHex rh = new RankHex(chosenHex, getGame());
        rh.rank();
        return rh.getRanking();
    }

    @Override
    public int rankPlayDevCard(DevelopmentCardType chosenCard) {
        int ranking = 0;
        switch (chosenCard) {
            case Knight:
                ranking++; // point for the opportunities available when moving rubber
                break;

            case RoadBuilding:

                Player p = null;

                playerLoop:
                for (Colour c : getGame().getPlayers().keySet()) {
                    if ((p = getGame().getPlayer(c)).hasLongestRoad()) {

                        //if building 2 roads gains you the longest road
                        if (p.getNumOfRoadChains() - 1 == getPlayer().calcRoadLength()) {
                            ranking += 2;
                            //1 for extending road and gaining longest road & 1 for the potential access to resources
                            break playerLoop;
                        }

                    }
                }
                break;

            case YearOfPlenty:
                int differentResources = 0;
                int diffResForCities = 0;
                Map<ResourceType, Integer> map = getPlayer().getResources();

                for (ResourceType rt : map.keySet()) {
                    if (map.get(rt) != 0 && rt != ResourceType.Ore) { // checking to see if I am lacking 2 or less resources to build a road or a settlement
                        differentResources++;
                    } else if (map.get(rt) != 0 && (rt == ResourceType.Ore || rt == ResourceType.Grain)) {
                        diffResForCities++;
                    }
                }

                if (differentResources >= 2 || diffResForCities >= 1) {
                    ranking++;
                }
                break;

            case Monopoly:

                for (Port port : getGame().getGrid().getPortsAsList()) {
                    if (port.hasSettlement()) {

                        Colour col = port.getRoad().getPlayerColour();
                        if (col == getPlayer().getColour()) {
                            ranking++;//more efficient trading available when taking block of resource cards
                            break;
                        }
                    }
                }
                ranking++; // for general trading ability with resource bank
                break;
        }

        return ++ranking;
    }

    @Override
    public int rankChosenResource(ResourceType chosenResource) {
        Map<ResourceType,Integer> map = getPlayer().getResources();

        int rank = 5;
        int least = map.get(chosenResource);

        for (ResourceType rt: map.keySet()){
            if(map.get(rt) < least){
                least = map.get(rt);
                rank--;
            }
        }
        return rank; // if many resources are more scarce than chosenResource --> low rank
        // else--> high rank
    }

//TODO:ASAP
    @Override
    public int rankBuyDevCard() {
        int rank = 4;
        return rank - getPlayer().getNumDevCards();
    }

//TODO:ASAP
    @Override
    public int rankNewRoad(Edge chosenEdge)
    {
        return 0;
    }



//LOWER PRIORITY, TRADE ETC
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
    public int rankInitiateTrade(Turn turn)
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
