package catan;

import static org.junit.Assert.*;
import enums.*;
import exceptions.*;
import game.build.*;
import board.*;
import game.*;
import game.players.*;

import java.awt.Point;

import org.junit.*;

public class GameTests extends TestHelper
{
	@Before
	public void setUp()
	{
		reset();
	}

	@Test
	public void makeGameTest()
	{
		assertTrue(game.getGrid().grid.values().size() == 19); // number of hexes
		assertTrue(game.getGrid().nodes.values().size() == 54); // number of nodes
		assertTrue(game.getGrid().ports.size() == 9);
	}

	@Test(expected = CannotAffordException.class)
	public void cannotBuildSettlementTest() throws CannotAffordException, IllegalPlacementException
	{
		p.buildSettlement(n);
	}
	
	@Test(expected = CannotBuildRoadException.class)
	public void cannotBuildRoadTest() throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		// Grant resources for road
		assertFalse(hasResources(p));
		p.grantResources(Road.getRoadCost());
		assertTrue(hasResources(p));
		
		// Cannot build because there is no settlement
		buildRoad(n.getEdges().get(0));
	}
	
	@Test(expected = CannotAffordException.class)
	public void cannotAffordRoadTest() throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		// Have to make a settlement before you can build a road
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(hasResources(p));
		Settlement s = makeSettlement(n);

		// Try to build a road with no resources
		assertFalse(hasResources(p));
		p.buildRoad(n.getEdges().get(0));
	}

	@Test(expected = CannotAffordException.class)
	public void cannotAffordCityTest() throws CannotAffordException, CannotUpgradeException
	{
		// Test resources
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(hasResources(p));

		// Build settlement
		Settlement s = makeSettlement(n);

		p.upgradeSettlement(n);
	}

	@Test(expected = CannotUpgradeException.class)
	public void cannotBuildCityTest() throws CannotAffordException, CannotUpgradeException
	{
		// Cannot build city on node unless a settlement is already there
		p.upgradeSettlement(n);
	}

	@Test
	public void buildSettlementTest()
	{
		// Test resources
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(hasResources(p));

		// Build settlement
		makeSettlement(n);
	}
	
	@Test(expected = IllegalPlacementException.class)
	public void buildNearSettlementTest() throws IllegalPlacementException
	{
		// Grant resources and build first settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(n);
		
		// Find adjacent node
		Node x = n.getEdges().get(0).getX(), y = n.getEdges().get(0).getY();
		Node n2 = x.equals(n) ? y : x;
		try
		{
			// Grant resources and try to build a settlement
			p.grantResources(Settlement.getSettlementCost());
			p.buildSettlement(n);
		}
		catch (CannotAffordException e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void buildRoadTest() throws CannotBuildRoadException, RoadExistsException
	{
		// Test resources grant
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(hasResources(p));

		// make settlement so that we can build a road
		makeSettlement(n);
		
		// Test resources grant
		assertFalse(hasResources(p));
		p.grantResources(Road.getRoadCost());
		assertTrue(hasResources(p));
		
		// Build road
		buildRoad(n.getEdges().get(0));
	}
	
	@Test
	public void collectResourcesTest()
	{
		p.grantResources(Settlement.getSettlementCost());
		game.addPlayer(p);

		makeSettlement(n);

		// collect resources
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 1);
	}

	@Test
	public void collectResourcesWithRobberTest()
	{
		p.grantResources(Settlement.getSettlementCost());
		
		// Make a settlement and toggle the robber on its hex
		makeSettlement(n);
		hex.toggleRobber();

		// try to collect resources
		game.allocateResources(hex.getChit());
		assertFalse(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 0);
	}
	
	@Test
	public void collectResourcesCityTest()
	{
		// Grant resources for and make settlement
		p.grantResources(Settlement.getSettlementCost());
		Settlement s = makeSettlement(n);

		// Grant resources for and upgrade settlement
		p.grantResources(City.getCityCost());
		City c = makeCity(n);
		assertEquals(c.getNode(), s.getNode()); // assert the upgrade happened

		// collect 2 of this resource
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 2);
	}

	/**
	 * Builds roads around a single node, and another somewhere else.
	 * This test asserts the player has 4 roads but that the length of its 
	 * longest is 3.
	 * @throws CannotBuildRoadException
	 * @throws RoadExistsException 
	 */
	@Test 
	public void roadLengthTest() throws CannotBuildRoadException, RoadExistsException
	{
		Node n2 = game.getGrid().nodes.get(new Point(-1,0));
		
		// Make two settlements
		p.grantResources(Settlement.getSettlementCost());
		Settlement s = makeSettlement(n);
		p.grantResources(Settlement.getSettlementCost());
		Settlement s2 = makeSettlement(n2);
		
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
		
		// Ensure three were built and that this player's longest road count
		// was incremented
		assertEquals(4, p.getRoads().size());
		assertEquals(3, p.calcRoadLength());
	}

}
