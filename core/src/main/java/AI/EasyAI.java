package AI;

import client.Turn;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.Game;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.Player;
import grid.Edge;
import grid.Hex;
import grid.Node;
import grid.Port;
import intergroup.board.Board;
import intergroup.trade.Trade;

import java.util.*;

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
        RankHex rh = new RankHex(chosenHex, getState());
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
                for (Colour c : getState().getPlayers().keySet()) {
                    if ((p = getState().getPlayer(c)).getHasLongestRoad()) {

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

                for (Port port : getState().getGrid().getPortsAsList())
                {
                    if (port.hasSettlement())
                    {

                        Road road = port.getRoad();
                        if(road != null)
                        {
                            Colour col = road.getPlayerColour();
                            if (col == getPlayer().getColour())
                            {
                                ranking++;//more efficient trading available when taking block of resource cards
                                break;
                            }
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

    @Override
    public int rankBuyDevCard()
    {
        int rank = 4;

        //check if player can build or set a road if so-> decrease rank by 2
        if(getPlayer().canAfford(Settlement.getSettlementCost())
                || getPlayer().canAfford(Road.getRoadCost())
                || getPlayer().canAfford(City.getCityCost())){
            rank =-2;
        }
        return rank - getPlayer().getNumDevCards();
        // the less cards you have the higher the priority
    }

    @Override
    public int rankNewRoad(Edge chosenEdge)
    {
        Node n1 = chosenEdge.getX();
        Node n2 = chosenEdge.getY();

        int rank = 3 ;

        if(chosenEdge.hasSettlement()) {// if next settlement has to be 2 roads away
            Node toAnalyse = (n1.getBuilding()!=null && n1.getBuilding().getPlayerColour() == getPlayer().getColour()) ? n2 : n1;

            for (Edge e: toAnalyse.getEdges()) {
                if(!e.hasSettlement()){
                    rank++;

                    Node nxtToAnalyse = (e.getX().getBuilding() == null && !e.getX().equals(toAnalyse) ) ? e.getX() : e.getY();
                    if(nxtToAnalyse.getBuilding() == null){
                        rank ++ ;
                    }
                    else if(nxtToAnalyse.getBuilding() != null)
                    {
                        if(nxtToAnalyse.getBuilding().getPlayerColour() != getPlayer().getColour())
                        {
                            rank =- 2;
                        }
                        else{
                            rank++;
                        }



                    }

                }
            }

        }else{// if settlement can be build at the end of this road
            Node toAnalyse = (!n1.isNearRoad(getPlayer().getColour())) ? n1 :n2 ;
            RankNode rn = new RankNode(toAnalyse, getPlayer());
            rn.rank(false);
            rank += (rn.getRanking()/2);
        }

        return rank;
    }



    @Override
    public int rankDiscard(Turn turn)
    {
        int amount = getPlayer().getNumResources();
        int diff = amount / 2;
        Map<ResourceType, Integer> discard = new HashMap<ResourceType, Integer>();
        Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
        resources.putAll(getPlayer().getResources());


        while(diff > 0)
        {
            ResourceType r = ResourceType.Ore;

            if(resources.containsKey(r) && resources.get(r) > 0)
            {
                resources.put(r, resources.get(r) - 1);
                discard.put(r, 1 + (discard.containsKey(r) ? discard.get(r) : 0));
            }
            else
            {
                if(resources.get(ResourceType.Grain) > resources.get(ResourceType.Wool))
                {
                    r = ResourceType.Grain;
                    if(resources.containsKey(r) && resources.get(r) > 0)
                    {
                        resources.put(r, resources.get(r) - 1);
                        discard.put(r, 1 + (discard.containsKey(r) ? discard.get(r) : 0));
                    }
                }
                else if(resources.containsKey(ResourceType.Wool) && resources.get(ResourceType.Wool) > 0)
                {
                    r = ResourceType.Wool;
                    resources.put(r, resources.get(r) - 1);
                    discard.put(r, 1 + (discard.containsKey(r) ? discard.get(r) : 0));
                }
                else if(resources.get(ResourceType.Lumber) > resources.get(ResourceType.Brick))
                {
                    r = ResourceType.Lumber;
                    if(resources.containsKey(r) && resources.get(r) > 0)
                    {
                        resources.put(r, resources.get(r) - 1);
                        discard.put(r, 1 + (discard.containsKey(r) ? discard.get(r) : 0));
                    }
                }
                else
                {
                    r = ResourceType.Brick;
                    if(resources.containsKey(r) && resources.get(r) > 0)
                    {
                        resources.put(r, resources.get(r) - 1);
                        discard.put(r, 1 + (discard.containsKey(r) ? discard.get(r) : 0));
                    }
                }


            }
            turn.setChosenResources(discard);
            diff--;
        }

        return 6;
    }





    @Override
    public int rankTradeResponse(Trade.Response tradeResponse, Trade.WithPlayer trade)
    {
        int ranking = -1;
        Map<ResourceType,Integer> given = getState().processResources(trade.getOffering());
        Map<ResourceType,Integer>  taking = getState().processResources(trade.getWanting());

        for(ResourceType rt : getPlayer().getResources().keySet()){
            if(getPlayer().getResources().get(rt) <=1 ){
                if(given.containsKey(rt)){
                    ranking ++;
                }
            }
            else if(getPlayer().getResources().get(rt)>=4){
                if(taking.containsKey(rt)){
                    ranking ++;
                }
            }
        }

        return (tradeResponse == Trade.Response.REJECT) ? 0 : ranking;

    }

    // remains 0 as this is the EasyAI
    public int rankEndTurn()
    {
        return 0;
    }

    @Override
    public int rankTargetPlayer(Colour target)
    {
        int rank = 5;

        HashMap<Colour, Integer> totalRank = new HashMap<Colour, Integer>();

        Game game = client.getState();

        HashMap<Colour, Player> players = (HashMap<Colour, Player>) game.getPlayers();

        Set<Colour> colours = players.keySet();

        for(Colour c : colours)
        {
            Player p = players.get(c);

            HashMap<ResourceType, Integer> resources = (HashMap<ResourceType, Integer>) p.getResources();

            Set<ResourceType> types = resources.keySet();

            for(ResourceType t: types)
            {
                if(!totalRank.keySet().contains(c))
                {
                    totalRank.put(c, 0);
                }
                totalRank.put(c, totalRank.get(c) + resources.get(t));
            }

            totalRank.put(c, totalRank.get(c) + p.getVp());
        }

        ArrayList<Integer> ranks = new ArrayList<Integer>();

        for(Colour c: colours)
        {
            ranks.add(totalRank.get(c));
        }

        int targetRank = totalRank.get(target);

        for(int i: ranks)
        {
            if(i > targetRank)
            {
                rank--;
            }
        }

        return rank;
    }

    @Override
    public int rankInitiateTrade(Turn turn)
    {
        // Ensures the AI only trades once per turn, so as not to spam
        // the server
        if (getTurn().hasTraded()) return -1;

        Map<ResourceType, Integer> tradeReq = new HashMap<>();
        Map<ResourceType, Integer> resources = new HashMap<>();
        resources.putAll(getPlayer().getResources());
        List<ResourceType> want = getDesiredResources(resources);
        ResourceType maxResource = findMax(resources);
        ResourceType leastResource;
        Trade.WithPlayer.Builder trade = Trade.WithPlayer.newBuilder();

        // Set up the resources the player is wanting
        if (want.size() == 0)
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
    /**
     * Finds a player to trade with based on what the user wants
     *
     * @param wanting the resources the user wants
     * @return the player who should have some of this resource
     */
    private Board.Player findOther(ResourceType wanting)
    {
        // Arbitrary starting colour
        Colour c = Colour.BLUE.equals(getPlayer().getColour()) ? Colour.RED : Colour.BLUE;

        // Loop through players
        for (Player p : getState().getPlayers().values())
        {
            boolean val = false;

            // Skip this player
            if (p.equals(getPlayer())) continue;

            // Loop through players settlements
            for (Building b : p.getSettlements().values())
            {
                // Check each hex this building is adjacent to
                for (Hex h : b.getNode().getHexes())
                {
                    if (h.getResource().equals(wanting))
                    {
                        c = p.getColour();
                        val = true;
                        break;
                    }
                }

                if (val) break;
            }

            if (val) break;
        }

        return Board.Player.newBuilder().setId(getState().getPlayer(c).getId()).build();
    }

    /**
     * Ascertains which resource types the player does NOT have
     *
     * @param resources the player's resources
     * @return a list of resource types that the player does NOT have
     */
    private List<ResourceType> getDesiredResources(Map<ResourceType, Integer> resources)
    {
        List<ResourceType> want = new ArrayList<>();

        // Ask for resources this player does NOT have
        for (ResourceType type : ResourceType.values())
        {
            // If the player has none of this resource
            if (!resources.containsKey(type) || resources.get(type) == 0)
            {
                want.add(type);
            }
        }
        return want;
    }

    /**
     * Finds the resource that the player owns the most of
     *
     * @param resources the player's resources
     * @return the max resource
     */
    private ResourceType findMax(Map<ResourceType, Integer> resources)
    {
        int max = 0;
        ResourceType r = null;

        // Loop through player resources to find most common
        for (ResourceType type : resources.keySet())
        {
            if (resources.get(type) > max)
            {
                max = resources.get(type);
                r = type;
            }
        }

        return r;
    }

    /**
     * Finds the resource that the player owns the least of
     *
     * @param resources the player's resources
     * @return the least resource
     */
    private ResourceType findLeast(Map<ResourceType, Integer> resources)
    {
        int min = 100;
        ResourceType r = null;

        // Loop through player resources to find least common
        for (ResourceType type : resources.keySet())
        {
            if (resources.get(type) < min)
            {
                min = resources.get(type);
                r = type;
            }
        }

        return r;
    }

}
