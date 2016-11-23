package test.java.catan;

import static org.junit.Assert.*;
import main.java.game.build.*;
import main.java.game.enums.*;
import main.java.game.exceptions.*;
import main.java.board.*;
import main.java.game.*;
import main.java.game.players.*;

import java.awt.Point;

import org.junit.*;

public class GameTests
{
	Game game;
	Player p;
	Node n;
	Hex hex;

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
	public void cannotBuildSettlementTest() throws CannotAffordException
	{
		p.buildSettlement(n);
	}

	@Test(expected = CannotBuildRoadException.class)
	public void cannotBuildRoadTest() throws CannotAffordException, CannotBuildRoadException
	{
		// Grant resources for road
		assertFalse(hasResources(p));
		p.grantResources(Road.getRoadCost());
		assertTrue(hasResources(p));
		
		// Cannot build because there is no settlement
		buildRoad(n.getEdges().get(0));
	}
	
	@Test(expected = CannotAffordException.class)
	public void cannotAffordRoadTest() throws CannotAffordException, CannotBuildRoadException
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

	@Test
	public void buildRoadTest() throws CannotBuildRoadException
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
	public void collectResourcesCityTest()
	{
		game.addPlayer(p);

		// Make settlement
		p.grantResources(Settlement.getSettlementCost());
		Settlement s = makeSettlement(n);

		// collect resources
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 1);

		// Upgrade settlement
		p.grantResources(City.getCityCost());
		City c = makeCity(n);
		assertEquals(c.getNode(), s.getNode());

		// collect resources
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 3);
	}

	@Test 
	public void roadLengthTest() throws CannotBuildRoadException
	{
		Node n2 = game.getGrid().nodes.get(new Point(4,1));
		
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
	
	
	
	////////////////////
	// HELPER METHODS //
	////////////////////
	private Settlement makeSettlement(Node n)
	{
		assertTrue(hasResources(p));
		int oldSize = p.getSettlements().size();
		
		// Build settlement
		try
		{
			p.buildSettlement(n);
		}
		catch (CannotAffordException e)
		{
			e.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getSettlements().size() > oldSize);
		assertFalse(hasResources(p));

		return (Settlement) p.getSettlements().values().toArray()[0];
	}

	private City makeCity(Node n)
	{
		assertTrue(hasResources(p));

		// Build settlement
		try
		{
			p.upgradeSettlement(n);
		}
		catch (CannotAffordException | CannotUpgradeException e)
		{
			e.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getSettlements().size() == 1);

		return (City) p.getSettlements().values().toArray()[0];
	}

	private Road buildRoad(Edge e) throws CannotBuildRoadException
	{
		int oldSize = p.getRoads().size();
		
		assertTrue(hasResources(p));
		try
		{
			p.buildRoad(e);
		}
		catch (CannotAffordException ex)
		{
			ex.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getRoads().size() > oldSize);
		assertFalse(hasResources(p));

		return (Road) p.getRoads().toArray()[0];
	}

	private boolean hasResources(Player p)
	{
		for(ResourceType r : p.getResources().keySet())
		{
			if(p.getResources().get(r) > 0)
				return true;
		}

		return false;
	}


	private void reset()
	{
		game = new Game();
		p = new NetworkPlayer(Colour.Blue);
		n = game.getGrid().nodes.get(new Point(-1, 0));
		hex = n.getHexes().get(0);
	}
}
