package tests;

import board.Edge;
import board.Node;
import enums.Colour;
import exceptions.*;
import game.build.Road;
import game.build.Settlement;
import game.players.NetworkPlayer;
import game.players.Player;
import org.junit.Before;
import org.junit.Test;
import protocol.BoardProtos;
import protocol.BuildProtos.PointProto;
import protocol.RequestProtos;
import protocol.RequestProtos.BuildSettlementRequest;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RoadTests extends TestHelper
{
	@Before
	public void setUp()
	{
		reset();
	}

	@Test
	public void buildRoadTest() throws SettlementExistsException, CannotBuildRoadException, RoadExistsException
	{
		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		
		// Grant resources and Build road
		p.grantResources(Road.getRoadCost());
		buildRoad(n.getEdges().get(0));
	}
	
	@Test(expected = CannotBuildRoadException.class)
	public void cannotBuildRoadTest() throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{		
		// Cannot build because there is no settlement
		p.grantResources(Road.getRoadCost());
		buildRoad(n.getEdges().get(0));
	}

	@Test(expected = InvalidCoordinatesException.class)
	public void invalidCoordinatesTest() throws CannotAffordException, CannotBuildRoadException,
			RoadExistsException, InvalidCoordinatesException
	{
		// Set up request with invalid coordinates
		RequestProtos.BuildRoadRequest.Builder req = RequestProtos.BuildRoadRequest.newBuilder();
		BoardProtos.EdgeProto.Builder e = n.getEdges().get(0).toEdgeProto().toBuilder();
		PointProto.Builder point = PointProto.newBuilder();
		point.setY(-30);
		point.setX(-10);
		e.setP1(point);
		req.setEdge(e.build());

		// Dont worry about granting resources or anything.
		// The checks for invalid coordinates happens first.
		game.buildRoad(req.build(), p.getColour());
	}

	@Test
	public void buildRoadTest2() throws CannotAffordException, CannotBuildRoadException,
			RoadExistsException, InvalidCoordinatesException, SettlementExistsException
	{
		// Set up request
		RequestProtos.BuildRoadRequest.Builder req = RequestProtos.BuildRoadRequest.newBuilder();
		req.setEdge(n.getEdges().get(0).toEdgeProto());

		// Grant resources and build settlement so road construction is permitted
		p.grantResources(Settlement.getSettlementCost());
		p.grantResources(Road.getRoadCost());
		makeSettlement(p, n);

		// Dont worry about granting resources or anything.
		// The checks for invalid coordinates happens first.
		game.buildRoad(req.build(), p.getColour());
		assertEquals(p.getNumResources(), 0);
		assertEquals(p.getRoads().size(), 1);
	}

	@Test
	public void settlementBreaksRoadTest() throws SettlementExistsException, CannotAffordException, CannotBuildRoadException, RoadExistsException
	{	
		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		game.addPlayer(p2);


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
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		
		// Need a settlement before you can build a road.
		// For roads 3 and 4
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n6);
		
		// Build road 1
		p.grantResources(Road.getRoadCost());
		buildRoad(e1);
		
		// Build second road chained onto the first
		p.grantResources(Road.getRoadCost());
		buildRoad(e2);
		
		// Build third road chained onto the second
		p.grantResources(Road.getRoadCost());
		buildRoad(e3);
		
		// Build foreign settlement
		p2.grantResources(Settlement.getSettlementCost());
		makeSettlement(p2, n3);
		
		// Build sixth road next to settlement 2
		p.grantResources(Road.getRoadCost());
		buildRoad(e6);
		
		// Build fifth road chained onto the sixth
		p.grantResources(Road.getRoadCost());
		buildRoad(e5);
		
		// Build fourth road chained onto the fifth.
		p.grantResources(Road.getRoadCost());
		buildRoad(e4);
		
		// Longest road is 3. Two separate road chains.
		// Assert that they haven't been merged together
		assertEquals(3, p.calcRoadLength());
		assertEquals(2, p.getNumOfRoadChains());	
	}
	
	@Test
	public void settlementBreaksRoadTest2() throws SettlementExistsException, CannotAffordException,
			CannotBuildRoadException, RoadExistsException, IllegalPlacementException, InvalidCoordinatesException
	{
		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		game.addPlayer(p2);
		game.setCurrentPlayer(p2.getColour());
		
		Edge e1 = n.getEdges().get(0); // Will be first road
		Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end of first road
		Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0); // This will be second road
		Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end of second road
		Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0); // Third road 

		// Need a settlement before you can build a road.
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		
		// Build road 1
		p.grantResources(Road.getRoadCost());
		buildRoad(e1);
		
		// Build second road chained onto the first.
		p.grantResources(Road.getRoadCost());
		buildRoad(e2);
		
		// Build third road chained onto the second. 
		p.grantResources(Road.getRoadCost());
		buildRoad(e3);
	
		// Build foreign settlement in between second and third road.
		p2.grantResources(Settlement.getSettlementCost());
		BuildSettlementRequest.Builder req = BuildSettlementRequest.newBuilder();
		PointProto.Builder point = PointProto.newBuilder();
		point.setX(n2.getX());
		point.setY(n2.getY());
		req.setPoint(point.build());
		game.buildSettlement(req.build(), p2.getColour());

		// Assert previous road chain of length three was broken.
		assertEquals(2, p.calcRoadLength());
		assertEquals(2, p.getNumOfRoadChains());
	}
	
	@Test(expected = CannotAffordException.class)
	public void cannotAffordRoadTest() throws SettlementExistsException, CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		// Need a settlement before you can build a road.
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);

		// Try to build a road with no resources
		assertFalse(hasResources(p));
		p.buildRoad(n.getEdges().get(0));
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
		Node n2 = game.getGrid().nodes.get(new Point(-1,0));
		
		// Make two settlements
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n2);
		
		// Make three roads
		for(int i = 0; i < n.getEdges().size(); i++)
		{
			Edge e = n.getEdges().get(i);
			p.grantResources(Road.getRoadCost());
			
			// Build road
			buildRoad(e);
		}
		
		// Make another road
		Edge e = n2.getEdges().get(0);
		p.grantResources(Road.getRoadCost());
		buildRoad(e);
		
		// Ensure four were built but that this player's longest road count
		// is only 3
		assertEquals(4, p.getRoads().size());
		assertEquals(3, p.calcRoadLength());
	}

}
