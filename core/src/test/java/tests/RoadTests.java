package tests;

import enums.Colour;
import exceptions.*;
import game.build.Road;
import game.build.Settlement;
import game.players.Player;
import game.players.ServerPlayer;
import grid.Edge;
import grid.Node;
import intergroup.board.Board;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

public class RoadTests extends TestHelper
{
	@Before
	public void setUp()
	{
		reset();
	}

	@Test
	public void buildRoadTest()
			throws SettlementExistsException, CannotBuildRoadException, RoadExistsException, BankLimitException
	{
		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);

		// Grant resources and Build road
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, n.getEdges().get(0));
	}

	@Test(expected = CannotBuildRoadException.class)
	public void cannotBuildRoadTest()
			throws CannotAffordException, CannotBuildRoadException, RoadExistsException, BankLimitException
	{
		// Cannot build because there is no settlement
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, n.getEdges().get(0));
	}

	@Test(expected = InvalidCoordinatesException.class)
	public void invalidCoordinatesTest() throws CannotAffordException, CannotBuildRoadException, RoadExistsException,
			InvalidCoordinatesException, BankLimitException
	{
		// Set up request with invalid coordinates
		Board.Edge.Builder e = n.getEdges().get(0).toEdgeProto().toBuilder();
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setY(-30);
		point.setX(-10);
		e.setA(point);

		// Dont worry about granting resources or anything.
		// The checks for invalid coordinates happens first.
		game.setCurrentPlayer(p.getColour());
		game.buildRoad(e.build());
	}

	@Test
	public void buildRoadTest2() throws CannotAffordException, CannotBuildRoadException, RoadExistsException,
			InvalidCoordinatesException, SettlementExistsException, BankLimitException
	{
		// Set up request
		Board.Edge.Builder e = n.getEdges().get(0).toEdgeProto().toBuilder();

		// Grant resources and build settlement so road construction is
		// permitted
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		p.grantResources(Road.getRoadCost(), game.getBank());
		makeSettlement(p, n);
		p.spendResources(Settlement.getSettlementCost(), game.getBank());

		// Dont worry about granting resources or anything.
		// The checks for invalid coordinates happens first.
		game.setCurrentPlayer(p.getColour());
		buildRoad(p, n.getEdges().get(0));
		p.spendResources(Road.getRoadCost(), game.getBank());
		assertEquals(p.getNumResources(), 0);
		assertEquals(p.getRoads().size(), 1);
	}

	@Test
	public void settlementBreaksRoadTest() throws SettlementExistsException, CannotAffordException, BankLimitException,
			CannotBuildRoadException, RoadExistsException
	{
		// Set up player 2
		Player p2 = new ServerPlayer(Colour.RED, "");
		game.addPlayer(p2);

		// Find edges where roads will be built
		Edge e1 = n.getEdges().get(0); // Will be first road
		Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end
																// of first road
		Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0); // This
																									// will
																									// be
																									// second
																									// road
		Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end
																// of second
																// road
		Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0); // Third
																									// road
		Node n3 = e3.getX().equals(n2) ? e3.getY() : e3.getX(); // Opposite end
																// of third road
		Edge e4 = n3.getEdges().get(0).equals(e3) ? n3.getEdges().get(1) : n3.getEdges().get(0); // Fourth
																									// road
		Node n4 = e4.getX().equals(n3) ? e4.getY() : e4.getX(); // Opposite end
																// of fourth
																// road
		Edge e5 = n4.getEdges().get(0).equals(e4) ? n4.getEdges().get(1) : n4.getEdges().get(0); // Fifth
																									// road
		Node n5 = e5.getX().equals(n4) ? e5.getY() : e5.getX(); // Opposite end
																// of fifth road
		Edge e6 = n5.getEdges().get(0).equals(e5) ? n5.getEdges().get(1) : n5.getEdges().get(0); // sixth
																									// road

		// Second settlement node to allow building of roads 3 and 4, as roads
		// must be within two
		// of any settlement
		Node n6 = e6.getX().equals(n5) ? e6.getY() : e6.getX(); // Opposite end
																// of sixth road

		// Need a settlement before you can build a road.
		// For roads 1 and 2
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);

		// Need a settlement before you can build a road.
		// For roads 3 and 4
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n6);

		// Build road 1
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e1);

		// Build second road chained onto the first
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e2);

		// Build third road chained onto the second
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e3);

		// Build foreign settlement
		p2.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p2, n3);

		// Build sixth road next to settlement 2
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e6);

		// Build fifth road chained onto the sixth
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e5);

		// Build fourth road chained onto the fifth.
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e4);

		// Longest road is 3. Two separate road chains.
		// Assert that they haven't been merged together
		assertEquals(3, p.calcRoadLength());
		assertEquals(2, p.getNumOfRoadChains());
	}

	@Test
	public void settlementBreaksRoadTest2()
			throws SettlementExistsException, CannotAffordException, CannotBuildRoadException, RoadExistsException,
			IllegalPlacementException, InvalidCoordinatesException, BankLimitException
	{
		// Set up player 2
		Player p2 = new ServerPlayer(Colour.RED, "");
		game.addPlayer(p2);
		game.setCurrentPlayer(p2.getColour());

		Edge e1 = n.getEdges().get(0); // Will be first road
		Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end
																// of first road
		Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0); // This
																									// will
																									// be
																									// second
																									// road
		Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end
																// of second
																// road
		Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0); // Third
																									// road

		// Need a settlement before you can build a road.
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);

		// Build road 1
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e1);

		// Build second road chained onto the first.
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e2);

		// Build third road chained onto the second.
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e3);

		// Build foreign settlement in between second and third road.
		p2.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p2, n2);

		// Assert previous road chain of length three was broken.
		assertEquals(2, p.calcRoadLength());
		assertEquals(2, p.getNumOfRoadChains());
	}

	@Test(expected = CannotAffordException.class)
	public void cannotAffordRoadTest() throws SettlementExistsException, CannotAffordException,
			CannotBuildRoadException, RoadExistsException, BankLimitException
	{
		// Find edges
		Edge e1 = n.getEdges().get(0);
		Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end
																// of first edge
		Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0);
		Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end
																// of second
																// edge
		Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0);
		Node n3 = e3.getX().equals(n2) ? e3.getY() : e3.getX(); // Opposite end
																// of third edge
		Edge e4 = n3.getEdges().get(0).equals(e3) ? n3.getEdges().get(1) : n3.getEdges().get(0);
		Node n4 = e4.getX().equals(n3) ? e4.getY() : e4.getX(); // Opposite end
																// of fourth
																// edge
		Edge e5 = n4.getEdges().get(0).equals(e4) ? n4.getEdges().get(1) : n4.getEdges().get(0);
		Node n5 = e5.getX().equals(n4) ? e5.getY() : e5.getX(); // Opposite end
																// of fifth edge
		Edge e6 = n5.getEdges().get(0).equals(e5) ? n5.getEdges().get(1) : n5.getEdges().get(0);
		Node n6 = e6.getX().equals(n5) ? e6.getY() : e6.getX(); // Opposite end
																// of sixth edge

		// Make two settlements
		makeSettlement(p, n);
		makeSettlement(p, n6);

		// Make two roads
		buildRoad(p, e1);
		buildRoad(p, e6);

		// Try to make third
		p.buildRoad(e5, game.getBank());
	}

	/**
	 * Builds roads around a single node, and another somewhere else. This test
	 * asserts the player has 4 roads but that the length of its longest is 3.
	 * 
	 * @throws CannotBuildRoadException
	 * @throws RoadExistsException
	 */
	@Test
	public void roadLengthTest()
			throws SettlementExistsException, CannotBuildRoadException, RoadExistsException, BankLimitException
	{
		Node n2 = game.getGrid().nodes.get(new Point(-1, 0));

		// Make two settlements
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n2);

		// Make three roads
		for (int i = 0; i < n.getEdges().size(); i++)
		{
			Edge e = n.getEdges().get(i);
			p.grantResources(Road.getRoadCost(), game.getBank());

			// Build road
			buildRoad(p, e);
		}

		// Make another road
		Edge e = n2.getEdges().get(0);
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, e);

		// Ensure four were built but that this player's longest road count
		// is only 3
		assertEquals(4, p.getRoads().size());
		assertEquals(3, p.calcRoadLength());
	}

}
