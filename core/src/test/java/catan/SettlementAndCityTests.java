package test.java.catan;

import static org.junit.Assert.*;
import main.java.board.Node;
import main.java.exceptions.*;
import main.java.game.build.Settlement;

import org.junit.*;

public class SettlementAndCityTests extends TestHelper
{
	@Before
	public void setUp()
	{
		reset();
	}


	@Test(expected = CannotAffordException.class)
	public void cannotBuildSettlementTest() throws CannotAffordException, IllegalPlacementException
	{
		p.buildSettlement(n);
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
			p.buildSettlement(n2);
		}
		catch (CannotAffordException e)
		{
			e.printStackTrace();
		}
	}
	
}
