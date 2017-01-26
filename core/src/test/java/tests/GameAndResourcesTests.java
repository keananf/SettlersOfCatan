package tests;

import exceptions.CannotStealException;
import exceptions.InvalidCoordinatesException;
import exceptions.SettlementExistsException;
import game.build.City;
import game.build.Settlement;
import org.junit.Before;
import org.junit.Test;
import protocol.BoardProtos;
import protocol.BuildProtos;
import protocol.EnumProtos;
import protocol.RequestProtos;

import static org.junit.Assert.*;

public class GameAndResourcesTests extends TestHelper
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
		assertTrue(game.getGrid().ports.size() == 9); // number of ports
	}
	
	@Test
	public void collectResourcesTest() throws SettlementExistsException
	{
		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		assertFalse(hasResources(p));

		// collect resources
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 1);
	}

	@Test
	public void collectResourcesWithRobberTest() throws SettlementExistsException
	{		
		// Make a settlement and toggle the robber on its hex
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		hex.toggleRobber();

		// try to collect resources
		game.allocateResources(hex.getChit());
		assertFalse(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 0);
	}

	@Test(expected = InvalidCoordinatesException.class)
	public void moveRobberInvalidCoordinate() throws InvalidCoordinatesException, CannotStealException
	{
		// Make a move robber request with invalid hex coordinates
		RequestProtos.MoveRobberRequest.Builder req = RequestProtos.MoveRobberRequest.newBuilder();
		BoardProtos.HexProto.Builder hex = this.hex.toHexProto().toBuilder();
		BuildProtos.PointProto.Builder point = BuildProtos.PointProto.newBuilder();
		point.setX(-10);
		point.setY(-15);
		hex.setP(point.build());
		req.setHex(hex.build());
		req.setColourToTakeFrom(EnumProtos.ColourProto.RED);

		game.moveRobber(req.build(), p.getColour());
	}
	
	@Test
	public void loseResourcesTest() // If you have over 7 resources, and a 7 is rolled
	{		
		// Grant the player over 7 resources
		p.grantResources(Settlement.getSettlementCost());
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(p.getNumResources() >= 7);

		// A '7' is rolled, so this player must lose it's excess resources
		game.allocateResources(7);
		assertTrue(p.getNumResources() == 7);
	}
	
	@Test
	public void collectResourcesCityTest() throws SettlementExistsException
	{
		// Grant resources for and make settlement
		p.grantResources(Settlement.getSettlementCost());
		Settlement s = makeSettlement(p, n);

		// Grant resources for and upgrade settlement
		p.grantResources(City.getCityCost());
		City c = makeCity(n);
		assertEquals(c.getNode(), s.getNode()); // assert the upgrade happened

		// collect 2 of this resource
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 2);
	}
}

