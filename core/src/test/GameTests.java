package test;

import static org.junit.Assert.*;
import game.build.*;
import game.enums.*;
import game.exceptions.CannotAffordException;
import game.exceptions.CannotUpgradeException;
import board.*;
import game.*;
import game.players.*;

import java.awt.Point;

import org.junit.*;

public class GameTests
{
	static Game game;
	static Player p;
	static Node n;
	static Hex hex;
	
	@BeforeClass
	public static void setUp()
	{
		reset();
	}
	
	@After
	public void cleanUp()
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
	
	@Test(expected = CannotAffordException.class)
	public void cannotBuildRoadTest() throws CannotAffordException
	{
		p.buildRoad(game.getGrid().edges.get(0));
	}
	
	@Test(expected = CannotAffordException.class)
	public void cannotAffordCityTest() throws CannotAffordException, CannotUpgradeException
	{
		// Test resources
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(hasResources(p));
		
		// Build settlement
		Settlement s = makeSettlement();
		
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
		makeSettlement();
	}
	
	@Test
	public void buildRoadTest()
	{
		// Test resources
		assertFalse(hasResources(p));
		p.grantResources(Road.getRoadCost());
		assertTrue(hasResources(p));
		
		// Build road
		buildRoad();
	}

	@Test
	public void collectResourcesTest()
	{
		p.grantResources(Settlement.getSettlementCost());
		game.addPlayer(p);

		makeSettlement();
		
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
		Settlement s = makeSettlement();

		// collect resources
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 1);
	
		// Upgrade settlement
		p.grantResources(City.getCityCost());
		City c = makeCity();
		assertEquals(c.getNode(), s.getNode());
		
		// collect resources
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 3);
	}
	
	////////////////////
	// HELPER METHODS //
	////////////////////
	
	
	private Settlement makeSettlement()
	{
		assertTrue(hasResources(p));

		// Build settlement
		try
		{
			Node n = game.getGrid().nodes.get(new Point(-1, 0));
			hex = n.getHexes().get(0);
			p.buildSettlement(n);
		}
		catch (CannotAffordException e)
		{
			e.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getSettlements().size() == 1);
		assertFalse(hasResources(p));
		
		return (Settlement) p.getSettlements().values().toArray()[0];
	}
	
	private City makeCity()
	{
		assertTrue(hasResources(p));

		// Build settlement
		try
		{
			Node n = game.getGrid().nodes.get(new Point(-1, 0));
			hex = n.getHexes().get(0);
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
	
	private Road buildRoad()
	{
		assertTrue(hasResources(p));
		try
		{
			Edge e= game.getGrid().edges.get(0);
			p.buildRoad(e);
		}
		catch (CannotAffordException e)
		{
			e.printStackTrace();
		}
		
		// Test it was built correctly and that resources were taken away
		assertTrue(p.getRoads().size() == 1);
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
	
	
	private static void reset()
	{
		game = new Game();
		p = new NetworkPlayer(Colour.Blue);
		n = game.getGrid().nodes.get(new Point(-1, 0));
		hex = n.getHexes().get(0);
	}
}
