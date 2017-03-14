package tests;

import intergroup.board.Board;
import enums.Colour;
import enums.DevelopmentCardType;
import exceptions.*;
import game.build.Building;
import game.build.City;
import game.build.Settlement;
import game.players.LocalPlayer;
import game.players.Player;
import grid.Edge;
import grid.Hex;
import grid.HexGrid;
import grid.Node;
import intergroup.lobby.Lobby;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

public class ClientProcessTests extends ClientTestHelper
{
    @Test
    public void processBoardTest() throws CannotAffordException, InvalidCoordinatesException
    {
        // Retrieve board and its protobuf representation
        HexGrid actualBoard = game.getGrid();
        Lobby.GameSetup board = game.getGameSettings(clientPlayer.getColour());

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
        Board.Point p1 = n.toProto();

        // Process request
        processSettlementEvent(n, p.getColour());
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() != null);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() instanceof Settlement);
        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement().getPlayerColour().equals(clientPlayer.getColour()));
    }


    @Test
    public void cityTest()
    {
        Board.Point p1 = n.toProto();

        // Process request
        processCityEvent(n, clientPlayer.getColour());
        Building settlement = clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement();

        assertTrue(clientGame.getGrid().getNode(p1.getX(), p1.getY()).getSettlement() != null);
        assertTrue(settlement instanceof City);
        assertTrue(settlement.getPlayerColour().equals(clientPlayer.getColour()));
    }

    @Test
    public void roadTest()
    {
        Edge edge = n.getEdges().get(0);
        Board.Point p1 = edge.toEdgeProto().getA(), p2 = edge.toEdgeProto().getB();

        // Need a settlement before you can build a road.
        processSettlementEvent(n, clientPlayer.getColour());

        // Process request
        processRoadEvent(edge, clientPlayer.getColour());
        assertTrue(clientGame.getGrid().getEdge(p1, p2).getRoad() != null);
        assertTrue(clientGame.getGrid().getEdge(p1, p2).getRoad().getPlayerColour().equals(clientPlayer.getColour()));
    }

    @Test
    public void settlementBreaksRoadTest()
    {
        Player p  = clientPlayer;
        Player p2 = new LocalPlayer(Colour.RED, "");
        p2.setId(Board.Player.Id.PLAYER_2);
        clientGame.addPlayer(p2);

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
        processSettlementEvent(n, p.getColour());

        // Need a settlement before you can build a road.
        // For roads 3 and 4
        processSettlementEvent(n6, p.getColour());

        // Build road 1
        processRoadEvent(e1, p.getColour());

        // Build second road chained onto the first
        processRoadEvent(e2, p.getColour());

        // Build third road chained onto the second
        processRoadEvent(e3, p.getColour());

        // Build foreign settlement
        processSettlementEvent(n3, p2.getColour());

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
        Player p  = clientPlayer;
        Player p2 = new LocalPlayer(Colour.RED, "");
        p2.setId(Board.Player.Id.PLAYER_2);
        clientGame.addPlayer(p2);

        // Find edges to make roads
        Edge e1 = n.getEdges().get(0); // Will be first road
        Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end of first road
        Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0); // This will be second road
        Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end of second road
        Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0); // Third road

        // Need a settlement before you can build a road.
        processSettlementEvent(n, p.getColour());

        // Build road 1
        processRoadEvent(e1, p.getColour());

        // Build second road chained onto the first.
        processRoadEvent(e2, p.getColour());

        // Build third road chained onto the second.
        processRoadEvent(e3, p.getColour());

        // Build foreign settlement in between second and third road.
        processSettlementEvent(n2, p2.getColour());

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
        Player p  = clientPlayer;
        Node n2 = game.getGrid().nodes.get(new Point(-1,0));

        // Make two settlements\
        processSettlementEvent(n, p.getColour());
        processSettlementEvent(n2, p.getColour());

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
        Player p  = clientPlayer;
        Player p2 = new LocalPlayer(Colour.RED, "");
        p2.setId(Board.Player.Id.PLAYER_2);
        clientGame.addPlayer(p2);

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
        processSettlementEvent(n, p.getColour());

        // Need a settlement so that this player can be stolen from
        processSettlementEvent(n6, p2.getColour());

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
        Player p  = clientPlayer;
        Player p2 = new LocalPlayer(Colour.RED, "");
        p2.setId(Board.Player.Id.PLAYER_2);
        clientGame.addPlayer(p2);

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
        processSettlementEvent(n, p.getColour());

        // Need a settlement before you can build a road.
        // For roads 3 and 4
        processSettlementEvent(n6, p.getColour());

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
        processSettlementEvent(n3, p2.getColour());
        assertEquals(3, p.calcRoadLength());
        assertEquals(2, p.getNumOfRoadChains());
        assertEquals(2, p.getVp());
    }

    @Test
    public void moveRobberTest() throws InvalidCoordinatesException
    {
        Hex h = game.getGrid().getHexWithRobber();

        // Set up request
        Board.Point point = h.toHexProto().getLocation();

        // Move and check
        clientGame.moveRobber(point);
        assertNotEquals(h, clientGame.getGrid().getHexWithRobber());
    }

    @Test
    public void playedDevCardTest() throws DoesNotOwnException
    {
        // Move and check
        processPlayedDevCard(Board.DevCard.newBuilder().setPlayableDevCard(Board.PlayableDevCard.YEAR_OF_PLENTY).build(), clientPlayer.getColour());
        assertTrue(clientGame.getPlayedDevCards().get(p.getColour()).get(DevelopmentCardType.YearOfPlenty) == 1);
    }

    @Test
    public void boughtDevCardTest() throws CannotAffordException
    {
        // Set up request
        processBoughtDevCard(Board.DevCard.newBuilder().setPlayableDevCard(Board.PlayableDevCard.MONOPOLY).build(), clientPlayer.getColour());
        assertTrue(clientGame.getBoughtDevCards().get(clientPlayer.getColour()) == 1);
    }

    @Test
    public void diceAndResourceTest()
    {
        Node n = clientGame.getGrid().getNode(-1, 0);
        int dice = n.getHexes().get(0).getChit();

        // Build Settlement so resources can be granted
        processSettlementEvent(n, p.getColour());

        // Move and check
        assertEquals(0, clientGame.getPlayer().getNumResources());
        clientGame.processDice(dice);
        assertEquals(clientGame.getDice(), dice);
        assertEquals(1, clientGame.getPlayer().getNumResources());
    }
}
