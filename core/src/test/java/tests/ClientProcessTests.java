package tests;

import board.Edge;
import board.Hex;
import board.HexGrid;
import board.Node;
import client.ClientGame;
import enums.Colour;
import enums.DevelopmentCardType;
import exceptions.*;
import game.build.City;
import game.build.Settlement;
import game.players.NetworkPlayer;
import game.players.Player;
import org.junit.Before;
import org.junit.Test;
import protocol.BoardProtos;
import protocol.BuildProtos;
import protocol.EnumProtos;
import protocol.EventProtos;

import java.awt.*;

import static org.junit.Assert.*;

public class ClientProcessTests extends ClientTestHelper
{
    @Before
    public void setUp() throws CannotAffordException, InvalidCoordinatesException
    {
        reset();
        clientGame = new ClientGame();
        clientGame.setBoard(game.getBoard().getBoard());
    }

    @Test
    public void processBoardTest() throws CannotAffordException, InvalidCoordinatesException
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
        BuildProtos.PointProto.Builder p1 = BuildProtos.PointProto.newBuilder();
        p1.setX(n.getX());
        p1.setY(n.getY());

        // Process request
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() != null);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() instanceof Settlement);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement().getPlayerColour().equals(Colour.BLUE));
    }


    @Test
    public void cityTest()
    {
        BuildProtos.PointProto.Builder p1 = BuildProtos.PointProto.newBuilder();
        p1.setX(n.getX());
        p1.setY(n.getY());

        // Process request
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.CITY);

        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() != null);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() instanceof City);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement().getPlayerColour().equals(Colour.BLUE));
    }

    @Test
    public void roadTest()
    {
        Edge edge = n.getEdges().get(0);

        // Need a settlement before you can build a road.
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Process request
        processRoadEvent(edge, Colour.BLUE);
        assertTrue(clientGame.getGrid().getEdge(edge.toEdgeProto()).getRoad() != null);
        assertTrue(clientGame.getGrid().getEdge(edge.toEdgeProto()).getRoad().getPlayerColour().equals(Colour.BLUE));
    }

    @Test
    public void settlementBreaksRoadTest()
    {
        Player p  = clientGame.getPlayers().get(Colour.BLUE);
        Player p2 = new NetworkPlayer(Colour.RED);

        // Find edges where roads will be built
        Edge e1 = n.getEdges().get(0); // Will be first road
        Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end of first road
        Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0); // This will be second road
        Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end of second road
        Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0); // Third road
        Node n3 = e3.getX().equals(n2) ? e3.getY() : e3.getX(); // Opposite end of third road
        Edge e4 = n3.getEdges().get(0).equals(e3) ? n3.getEdges().get(1) : n3.getEdges().get(0); // Fourth road
        Node n4 = e4.getX().equals(n3) ? e4.getY() : e4.getX(); // Opposite end of fourth road
        Edge e5 = n4.getEdges().get(0).equals(e4) ? n4.getEdges().get(1) : n4.getEdges().get(0); // Fifth road
        Node n5 = e5.getX().equals(n4) ? e5.getY() : e5.getX(); // Opposite end of fifth road
        Edge e6 = n5.getEdges().get(0).equals(e5) ? n5.getEdges().get(1) : n5.getEdges().get(0); // sixth road

        // Second settlement node to allow building of roads 3 and 4, as roads must be within two
        // of any settlement
        Node n6 = e6.getX().equals(n5) ? e6.getY() : e6.getX(); // Opposite end of sixth road

        // Need a settlement before you can build a road.
        // For roads 1 and 2
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Need a settlement before you can build a road.
        // For roads 3 and 4
        processSettlementEvent(n6, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Build road 1
        processRoadEvent(e1, p.getColour());

        // Build second road chained onto the first
        processRoadEvent(e2, p.getColour());

        // Build third road chained onto the second
        processRoadEvent(e3, p.getColour());

        // Build foreign settlement
        processSettlementEvent(n3, p2.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Build sixth road next to settlement 2
        processRoadEvent(e6, p.getColour());

        // Build fifth road chained onto the sixth
        processRoadEvent(e5, p.getColour());

        // Build fourth road chained onto the fifth.
        processRoadEvent(e4, p.getColour());

        // Longest road is 3. Two separate road chains.
        // Assert that they haven't been merged together
        assertEquals(3, p.calcRoadLength());
        assertEquals(2, p.getNumOfRoadChains());
    }

    @Test
    public void settlementBreaksRoadTest2()
    {
        Player p  = clientGame.getPlayers().get(Colour.BLUE);
        Player p2 = new NetworkPlayer(Colour.RED);

        // Find edges to make roads
        Edge e1 = n.getEdges().get(0); // Will be first road
        Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end of first road
        Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0); // This will be second road
        Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end of second road
        Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0); // Third road

        // Need a settlement before you can build a road.
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Build road 1
        processRoadEvent(e1, p.getColour());

        // Build second road chained onto the first.
        processRoadEvent(e2, p.getColour());

        // Build third road chained onto the second.
        processRoadEvent(e3, p.getColour());

        // Build foreign settlement in between second and third road.
        processSettlementEvent(n2, p2.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Assert previous road chain of length three was broken.
        assertEquals(2, p.calcRoadLength());
        assertEquals(2, p.getNumOfRoadChains());
    }

    /**
     * Builds roads around a single node, and another somewhere else.
     * This test asserts the player has 4 roads but that the length of its
     * longest is 3.
     * @throws CannotBuildRoadException
     * @throws RoadExistsException
     */
    @Test
    public void roadLengthTest() throws SettlementExistsException, CannotBuildRoadException, RoadExistsException
    {
        Player p  = clientGame.getPlayers().get(Colour.BLUE);
        Node n2 = game.getGrid().nodes.get(new Point(-1,0));

        // Make two settlements\
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);
        processSettlementEvent(n2, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Make three roads
        for(int i = 0; i < n.getEdges().size(); i++)
        {
            Edge e = n.getEdges().get(i);
            processRoadEvent(e, p.getColour());
        }

        // Make another road not connected to the first three
        Edge e = n2.getEdges().get(0);
        processRoadEvent(e, p.getColour());

        // Ensure four were built but that this player's longest road count
        // is only 3
        assertEquals(4, p.getRoads().size());
        assertEquals(3, p.calcRoadLength());
    }

    @Test
    public void largestArmyTest()
    {
        Player p  = clientGame.getPlayers().get(Colour.BLUE);
        Player p2 = clientGame.getPlayers().get(Colour.RED);

        // Find edges
        Edge e1 = n.getEdges().get(0);
        Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end of first edge
        Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0);
        Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end of second edge
        Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0);
        Node n3 = e3.getX().equals(n2) ? e3.getY() : e3.getX(); // Opposite end of third edge
        Edge e4 = n3.getEdges().get(0).equals(e3) ? n3.getEdges().get(1) : n3.getEdges().get(0);
        Node n4 = e4.getX().equals(n3) ? e4.getY() : e4.getX(); // Opposite end of fourth edge
        Edge e5 = n4.getEdges().get(0).equals(e4) ? n4.getEdges().get(1) : n4.getEdges().get(0);
        Node n5 = e5.getX().equals(n4) ? e5.getY() : e5.getX(); // Opposite end of fifth edge
        Edge e6 = n5.getEdges().get(0).equals(e5) ? n5.getEdges().get(1) : n5.getEdges().get(0);
        Node n6 = e6.getX().equals(n5) ? e6.getY() : e6.getX(); // Opposite end of sixth edge

        // Need a settlement before you can build a road.
        // For roads 1 and 2
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Need a settlement so that this player can be stolen from
        processSettlementEvent(n6, p2.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Player 1 plays three knights
        for(Hex h : n6.getHexes())
        {
            processPlayKnightEvent(h, p.getColour());
        }

        // Assert largest army
        assertEquals(3, p.getVp());
        assertEquals(1, p2.getVp());

        // Have player 2 play four knights, so largest army is revoked.
        while(p2.getArmySize() < 4)
        {
            Hex h = n.getHexes().get(0);
            processPlayKnightEvent(h, p2.getColour());
        }

        assertEquals(1, p.getVp());
        assertEquals(3, p2.getVp());
    }

    @Test
    public void longestRoadTest()
    {
        Player p  = clientGame.getPlayers().get(Colour.BLUE);

        // Find edges where roads will be built
        Edge e1 = n.getEdges().get(0); // Will be first road
        Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end of first road
        Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0); // This will be second road
        Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end of second road
        Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0); // Third road
        Node n3 = e3.getX().equals(n2) ? e3.getY() : e3.getX(); // Opposite end of third road
        Edge e4 = n3.getEdges().get(0).equals(e3) ? n3.getEdges().get(1) : n3.getEdges().get(0); // Fourth road
        Node n4 = e4.getX().equals(n3) ? e4.getY() : e4.getX(); // Opposite end of fourth road
        Edge e5 = n4.getEdges().get(0).equals(e4) ? n4.getEdges().get(1) : n4.getEdges().get(0); // Fifth road
        Node n5 = e5.getX().equals(n4) ? e5.getY() : e5.getX(); // Opposite end of fifth road
        Edge e6 = n5.getEdges().get(0).equals(e5) ? n5.getEdges().get(1) : n5.getEdges().get(0); // sixth road

        // Second settlement node to allow building of roads 3 and 4, as roads must be within two
        // of any settlement
        Node n6 = e6.getX().equals(n5) ? e6.getY() : e6.getX(); // Opposite end of sixth road

        // Need a settlement before you can build a road.
        // For roads 1 and 2
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Need a settlement before you can build a road.
        // For roads 3 and 4
        processSettlementEvent(n6, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Build road 1
        processRoadEvent(e1, p.getColour());

        // Build second road chained onto the first
        processRoadEvent(e2, p.getColour());

        // Build third road chained onto the second
        processRoadEvent(e3, p.getColour());

        // Build fourth road chained onto the fifth.
        processRoadEvent(e4, p.getColour());

        // Build fifth road chained onto the sixth
        processRoadEvent(e5, p.getColour());

        // Assert longest road
        assertEquals(5, p.calcRoadLength());
        assertEquals(1, p.getNumOfRoadChains());
        assertEquals(4, p.getVp());

        // Build foreign settlement so longest road is revoked.
        processSettlementEvent(n3, Colour.RED, EnumProtos.BuildingTypeProto.SETTLEMENT);
        assertEquals(3, p.calcRoadLength());
        assertEquals(2, p.getNumOfRoadChains());
        assertEquals(2, p.getVp());
    }

    @Test
    public void moveRobberTest() throws InvalidCoordinatesException
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
    public void playedDevCardTest() throws DoesNotOwnException
    {
        // Move and check
        processPlayedDevCard(EnumProtos.DevelopmentCardProto.LIBRARY, p.getColour());
        assertTrue(clientGame.getPlayedDevCards().get(p.getColour()).get(DevelopmentCardType.Library) == 1);
    }

    @Test
    public void boughtDevCardTest() throws CannotAffordException
    {
        // Set up request
        EventProtos.Event.Builder ev = EventProtos.Event.newBuilder();
        ev.setBoughtDevCard(EnumProtos.ColourProto.RED);

        // Move and check
        clientGame.getPlayer().grantResources(DevelopmentCardType.getCardCost());
        clientGame.recordDevCard(ev.getBoughtDevCard());
        assertTrue(clientGame.getBoughtDevCards().get(Colour.RED) == 1);
    }

    @Test
    public void diceAndResourceTest()
    {
        Node n = clientGame.getGrid().getNode(-1, 0);
        int dice = n.getHexes().get(0).getChit();

        // Build Settlement so resources can be granted
        processSettlementEvent(n, p.getColour(), EnumProtos.BuildingTypeProto.SETTLEMENT);

        // Set up request
        EventProtos.DiceRoll.Builder point = EventProtos.DiceRoll.newBuilder();
        point.setDice(dice);

        // Move and check
        assertEquals(0, clientGame.getPlayer().getNumResources());
        clientGame.processDice(point.build().getDice());
        assertEquals(clientGame.getDice(), dice);
        assertEquals(1, clientGame.getPlayer().getNumResources());
    }
}
