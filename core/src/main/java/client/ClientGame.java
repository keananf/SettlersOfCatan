package client;

import board.*;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.GameState;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.LocalPlayer;
import game.players.NetworkPlayer;
import game.players.Player;
import protocol.BoardProtos.*;
import protocol.BuildProtos;
import protocol.BuildProtos.BuildingProto;
import protocol.BuildProtos.PointProto;
import protocol.EnumProtos;
import protocol.EnumProtos.BuildingTypeProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A game with additional methods for processing protobufs for the client
 * Created by 140001596
 */
public class ClientGame extends GameState
{
    private boolean gameOver;
    private int dice;
    private Map<Colour, Integer> boughtDevCards;
    private Map<Colour, HashMap<DevelopmentCardType, Integer>> playedDevCards;
    private Player thisPlayer;

    public ClientGame()
    {
        players = new HashMap<Colour, Player>();
        boughtDevCards = new HashMap<Colour, Integer>();
        playedDevCards = new HashMap<Colour, HashMap<DevelopmentCardType, Integer>>();

        // Instantiate the playedDevCards maps
        for(Colour c : Colour.values())
        {
            playedDevCards.put(c, new HashMap<DevelopmentCardType, Integer>());

            for(DevelopmentCardType d : DevelopmentCardType.values())
            {
                playedDevCards.get(c).put(d, 0);
            }

            // Instantiate players as well
            if(!c.equals(Colour.BLUE))
                players.put(c, new NetworkPlayer(c));
        }

        // Set up this player
        thisPlayer = new LocalPlayer(Colour.BLUE); // TODO colour will be allocated from server
        playerWithLongestRoad = thisPlayer.getColour();
        currentPlayer = thisPlayer.getColour();
        players.put(thisPlayer.getColour(), thisPlayer);
    }

    /**
     * @return a representation of the board that is compatible with protofbufs
     */
    public HexGrid setBoard(BoardProto board)
    {
        HexGrid grid = new HexGrid();
        List<Node> nodes = processNodes(board.getNodesList());
        List<Hex> hexes = processHexes(board.getHexesList());
        List<Port> ports = processPorts(board.getPortsList());
        List<Edge> edges = processEdges(board.getEdgesList());

        // Overwrite current grid
        grid.setNodesAndHexes(nodes, hexes);
        grid.setEdgesAndPorts(edges, ports);

        return grid;
    }

    /**
     * Retrieve the hex objects referred to by the proto
     * @param protos the hex protos
     */
    private List<Hex> processHexes(List<HexProto> protos)
    {
        List<Hex> hexes = new ArrayList<Hex>();

        // Add nodes
        for(HexProto proto : protos)
        {
            hexes.add(Hex.fromProto(proto));
        }

        return hexes;
    }

    /**
     * Retrieve the node objects referred to by the proto
     * @param protos the node protos
     */
    private List<Node> processNodes(List<NodeProto> protos)
    {
        List<Node> nodes = new ArrayList<Node>();

        // Add nodes
        for(NodeProto proto : protos)
        {
            PointProto p1 = proto.getP();
            Node node = new Node(p1.getX(), p1.getY());

            // If the node has a building
            if(proto.hasBuilding())
            {
                node.setSettlement(processNewBuilding(proto.getBuilding()));
            }

            nodes.add(node);
        }

        return nodes;
    }

    /**
     * Retrieve the port objects referred to by the proto
     * @param protos the port protos
     */
    private List<Port> processPorts(List<PortProto> protos)
    {
        List<Port> ports = new ArrayList<Port>();

        // Add ports
        for(PortProto e : protos)
        {
            PointProto p1 = e.getP1(), p2 = e.getP2();
            Port port = new Port(grid.getNode(p1.getX(), p1.getY()), grid.getNode(p2.getX(), p2.getY()));
            port.setExchangeAmount(e.getExchangeAmount());
            port.setReturnAmount(e.getReturnAmount());
            port.setExchangeType(ResourceType.fromProto(e.getExchangeResource()));
            port.setReturnType(ResourceType.fromProto(e.getReturnResource()));

            ports.add(port);
        }

        return ports;
    }

    /**
     * Retrieve the edge objects referred to by the proto
     * @param protos the edge protos
     */
    private List<Edge> processEdges(List<EdgeProto> protos)
    {
        List<Edge> edges = new ArrayList<Edge>();

        // Add edges
        for(EdgeProto e : protos)
        {
            PointProto p1 = e.getP1(), p2 = e.getP2();
            Edge newEdge = new Edge(grid.getNode(p1.getX(), p1.getY()), grid.getNode(p2.getX(), p2.getY()));
            edges.add(newEdge);
        }

        return edges;
    }

    /**
     * @return if the game has ended or not
     */
    public boolean isOver()
    {
        return gameOver;
    }

    /**
     * Once called, the listener thread will terminate the connection to the server
     */
    public void setGameOver()
    {
        gameOver = true;
    }

    /**
     * Updates the dice roll and allocate resources
     * @param dice the new dice roll
     */
    public void processDice(int dice)
    {
        this.dice = dice;
        Map<ResourceType, Integer> grant = getNewResources(dice, thisPlayer.getColour());

        if(dice != 7)
            thisPlayer.grantResources(grant);
    }

    /**
     * Swap the robber to the given point received from the server
     * @param robberMove the robber's new position
     */
    public void moveRobber(PointProto robberMove)
    {
         grid.swapRobbers(grid.getHex(robberMove.getX(), robberMove.getY()));
    }

    /**
     * Adds the new road received from the server to the board
     * @param newRoad the new road to add
     */
    public Road processRoad(BuildProtos.RoadProto newRoad)
    {
        // Extract information and find edge
        Edge newEdge = grid.getEdge(newRoad);

        // Make new road object
        Road r = new Road(newEdge, currentPlayer);
        newEdge.setRoad(r);

        ((LocalPlayer)players.get(currentPlayer)).addRoad(newEdge);
        checkLongestRoad(false);

        return r;
    }

    /**
     * Processes a new bulding, and adds it to the board
     * @param building the new building
     * @return the new building
     */
    public Building processNewBuilding(BuildingProto building)
    {
        // Extract information
        BuildingTypeProto type = building.getType();
        PointProto p = building.getP();
        Node node = grid.getNode(p.getX(), p.getY());
        Building b = null;

        // If invalid coordinates
        if(node == null || (node.getSettlement() != null &&
                ((node.getSettlement() instanceof Settlement && type.equals(BuildingTypeProto.SETTLEMENT))
                || (node.getSettlement() instanceof City && type.equals(BuildingTypeProto.CITY)))))
        {
            return b;
        }

        // Create and add the building
        switch(type)
        {
            case CITY:
                City c = new City(node, Colour.fromProto(building.getPlayerId()));
                node.setSettlement(c);
                b = c;
                break;
            case SETTLEMENT:
                Settlement s = new Settlement(node, Colour.fromProto(building.getPlayerId()));
                node.setSettlement(s);
                b = s;
                break;
        }
        checkIfRoadBroken(node);

        // Updates settlement and score
        players.get(currentPlayer).addSettlement(b);
        return b;
    }

    /**
     * Records the played dev card for the given player
     * @param type the played card
     */
    public void processPlayedDevCard(EnumProtos.DevelopmentCardProto type)
    {
        DevelopmentCardType card = DevelopmentCardType.fromProto(type);
        Map<DevelopmentCardType, Integer> playedCards = playedDevCards.get(currentPlayer);

        // Record card being played
        int existing = playedCards.get(card);
        playedCards.put(card, existing + 1);

        // Update largest army
        if(card.equals(DevelopmentCardType.Knight))
        {
            existing = boughtDevCards.containsKey(currentPlayer) ? boughtDevCards.get(currentPlayer) : 0;
            boughtDevCards.put(currentPlayer, existing - 1);
            players.get(currentPlayer).addKnight();
            checkLargestArmy();
        }
    }

    /**
     * Records that the given player bought a dev card
     * @param boughtDevCard
     */
    public void recordDevCard(EnumProtos.ColourProto boughtDevCard)
    {
        Colour c = Colour.fromProto(boughtDevCard);
        int existing = boughtDevCards.containsKey(c) ? boughtDevCards.get(c) : 0;
        boughtDevCards.put(c, existing + 1);
    }

    /**
     * Return the current dice roll
     * @return
     */
    public int getDice()
    {
        return dice;
    }

    /**
     * Return the total amounts of dev cards owned by each player
     * @return
     */
    public Map<Colour, Integer> getBoughtDevCards()
    {
        return boughtDevCards;
    }

    /**
     * @return the map of played dev cards
     */
    public Map<Colour,HashMap<DevelopmentCardType,Integer>> getPlayedDevCards()
    {
        return playedDevCards;
    }

    /**
     * @return this player
     */
    public Player getPlayer()
    {
        return thisPlayer;
    }
}
