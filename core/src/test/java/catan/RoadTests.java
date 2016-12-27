package test.java.catan;

import static org.junit.Assert.*;

import java.awt.Point;

import main.java.board.Edge;
import main.java.board.Node;
import main.java.exceptions.*;
import main.java.game.build.*;

import org.junit.*;

public class RoadTests extends TestHelper
{
	@Before
	public void setUp()
	{
		reset();
	}

	@Test
	public void buildRoadTest() throws CannotBuildRoadException, RoadExistsException
	{
		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(n);
		
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
	
	@Test(expected = CannotAffordException.class)
	public void cannotAffordRoadTest() throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		// Need a settlement before you can build a road.
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(n);

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
