package tests;

import board.Hex;
import board.HexGrid;
import board.Node;
import client.ClientGame;
import enums.Colour;
import game.build.City;
import game.build.Settlement;
import org.junit.Before;
import org.junit.Test;
import protocol.BoardProtos;
import protocol.BuildProtos;
import protocol.EnumProtos;
import protocol.EventProtos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ClientProcessTests extends TestHelper
{
    ClientGame clientGame;

    @Before
    public void setUp()
    {
        reset();
        clientGame = new ClientGame();
    }

    @Test
    public void processBoardTest()
    {
        // Retrieve board and its protobuf representation
        HexGrid actualBoard = game.getGrid();
        BoardProtos.BoardProto board = game.getBoard().getBoard();

        // Simulate processing of protobuf
        HexGrid processedBoard = clientGame.setBoard(board);

        // Assert all nodes were serialised and deserialised
        for(Node n1 : actualBoard.getNodesAsList())
        {
            assertTrue(processedBoard.getNodesAsList().contains(n1));
        }

        // Assert all hexes were serialised and deserialised
        for(Hex h1 : actualBoard.getHexesAsList())
        {
            assertTrue(processedBoard.getHexesAsList().contains(h1));
        }
    }

    @Test
    public void settlementTest()
    {
        // Set up request
        BuildProtos.BuildingProto.Builder req = BuildProtos.BuildingProto.newBuilder();
        BoardProtos.EdgeProto e = n.getEdges().get(0).toEdgeProto();
        BuildProtos.PointProto p1 = e.getP1();
        req.setP(p1);
        req.setPlayerId(EnumProtos.ColourProto.BLUE);
        req.setType(EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Process request
        clientGame.processNewBuilding(req.build());
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() != null);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() instanceof Settlement);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement().getPlayerColour().equals(Colour.BLUE));
    }


    @Test
    public void cityTest()
    {
        // Set up request
        BuildProtos.BuildingProto.Builder req = BuildProtos.BuildingProto.newBuilder();
        BoardProtos.EdgeProto e = n.getEdges().get(0).toEdgeProto();
        BuildProtos.PointProto p1 = e.getP1();
        req.setP(p1);
        req.setPlayerId(EnumProtos.ColourProto.BLUE);
        req.setType(EnumProtos.BuildingTypeProto.CITY);

        // Process request
        clientGame.processNewBuilding(req.build());
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() != null);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() instanceof City);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement().getPlayerColour().equals(Colour.BLUE));
    }

    @Test
    public void roadTest()
    {
        // Set up request
        BuildProtos.RoadProto.Builder req = BuildProtos.RoadProto.newBuilder();
        BoardProtos.EdgeProto e = n.getEdges().get(0).toEdgeProto();
        BuildProtos.PointProto p1 = e.getP1(), p2 = e.getP2();
        req.setP1(p1);
        req.setP2(p2);
        req.setPlayerId(EnumProtos.ColourProto.BLUE);

        // Process request
        clientGame.processRoad(req.build());
        assertTrue(clientGame.getGrid().getEdge(req.build()).getRoad() != null);
        assertTrue(clientGame.getGrid().getEdge(req.build()).getRoad().getPlayerColour().equals(Colour.BLUE));
    }

    @Test
    public void moveRobberTest()
    {
        Hex h = game.getGrid().getHexWithRobber();

        // Set up request
        BuildProtos.PointProto.Builder point = BuildProtos.PointProto.newBuilder();
        point.setX(hex.getX());
        point.setY(hex.getY());

        // Move and check
        clientGame.moveRobber(point.build());
        assertNotEquals(h, clientGame.getGrid().getHexWithRobber());
    }

    @Test
    public void boughtDevCard()
    {
        // Set up request
        EventProtos.Event.Builder ev = EventProtos.Event.newBuilder();
        ev.setBoughtDevCard(EnumProtos.ColourProto.RED);

        // Move and check
        clientGame.recordDevCard(ev.getBoughtDevCard());
        assertTrue(clientGame.getDevCardsBought().get(Colour.RED) == 1);
    }

    @Test
    public void diceTest()
    {
        // Set up request
        EventProtos.DiceRoll.Builder point = EventProtos.DiceRoll.newBuilder();
        point.setDice(5);

        // Move and check
        clientGame.setDice(point.build().getDice());
        assertEquals(clientGame.getDice(), 5);
    }
}
