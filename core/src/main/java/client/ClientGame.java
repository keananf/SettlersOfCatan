package client;

import board.*;
import enums.Colour;
import enums.ResourceType;
import game.GameState;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import protocol.BoardProtos.*;
import protocol.BuildProtos;
import protocol.BuildProtos.BuildingProto;
import protocol.BuildProtos.PointProto;
import protocol.EnumProtos.BuildingTypeProto;

import java.util.ArrayList;
import java.util.List;

/**
 * A game with additional methods for processing protobufs for the client
 * Created by 140001596
 */
public class ClientGame extends GameState
{
    private boolean gameOver;
    private int dice;

    public ClientGame() {}

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
     * Updates the dice roll to the one received from the server
     * @param dice the new dice roll
     */
    public void setDice(int dice)
    {
        this.dice = dice;
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
        Colour col = Colour.fromProto(newRoad.getPlayerId());

        // Make new road object
        Road r = new Road(newEdge, col);
        newEdge.setRoad(r);

        return r;
    }


    public Building processNewBuilding(BuildingProto building)
    {
        // Extract information
        BuildingTypeProto type = building.getType();
        PointProto p = building.getP();
        Node node = grid.getNode(p.getX(), p.getY());
        Building b = null;

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

        return b;
    }

    public int getDice()
    {
        return dice;
    }
}
