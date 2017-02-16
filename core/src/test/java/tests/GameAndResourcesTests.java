package tests;

import board.Board;
import grid.*;
import enums.Colour;
import exceptions.*;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.NetworkPlayer;
import game.players.Player;
import org.junit.Before;
import org.junit.Test;
import resource.Resource;

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
		Board.Point.Builder point = Board.Point.newBuilder().setX(-10).setY(-15);

		game.setTurn(p.getColour());
		game.moveRobber(point.build());
	}

	@Test
	public void moveRobberTest() throws InvalidCoordinatesException, CannotStealException, SettlementExistsException
	{
		// Make a second player
		Player p2 = new NetworkPlayer(Colour.RED, "");
		p2.setId(Board.Player.Id.PLAYER_2);
		game.addPlayer(p2);

		// Give player 2 resources to take
		p2.grantResources(Road.getRoadCost());

		// Grant player 2 a settlement so that player 1 will be allowed to take from them
		p2.grantResources(Settlement.getSettlementCost());
		makeSettlement(p2, n);

		// Make a move robber request
		Board.Point.Builder point = Board.Point.newBuilder().setX(hex.getX()).setY(hex.getY());

		// Test values before move
		assertEquals(p2.getNumResources(), 2);
		assertEquals(p.getNumResources(), 0);
		Hex h = game.getGrid().getHexWithRobber();

		// Move and check
		game.setTurn(p.getColour());
		game.moveRobber(point.build());
		game.takeResource(p2.getId());
		assertNotEquals(h, game.getGrid().getHexWithRobber());
		assertEquals(p.getNumResources(), 1);
		assertEquals(p2.getNumResources(), 1);
	}

	@Test(expected = InvalidDiscardRequest.class)
	public void invalidDiscardTest() throws InvalidDiscardRequest, CannotAffordException
	{
		// Grant player more than 7 resources
		p.grantResources(Settlement.getSettlementCost());
		p.grantResources(Settlement.getSettlementCost());
		p.grantResources(Settlement.getSettlementCost());
		assertEquals(12, p.getNumResources());

		// Construct discard request. Discarding one card is NOT enough
		Resource.Counts.Builder discard = Resource.Counts.newBuilder();
		discard.setLumber(1);

		game.processDiscard(discard.build(), p.getColour());
	}

	@Test(expected = CannotAffordException.class)
	public void invalidDiscardTest2() throws InvalidDiscardRequest, CannotAffordException
	{
		// Grant player more than 7 resources
		p.grantResources(Settlement.getSettlementCost());
		p.grantResources(Settlement.getSettlementCost());
		assertEquals(8, p.getNumResources());

		// Construct discard request. Don't have enough
		Resource.Counts.Builder discard = Resource.Counts.newBuilder();
		discard.setLumber(3);

		game.processDiscard(discard.build(), p.getColour());
	}

	@Test
	public void discardTest() throws InvalidDiscardRequest, CannotAffordException
	{
		// Grant player more than 7 resources
		p.grantResources(Settlement.getSettlementCost());
		p.grantResources(Settlement.getSettlementCost());
		assertEquals(8, p.getNumResources());

		// Construct discard request
		Resource.Counts.Builder discard = Resource.Counts.newBuilder();
		discard.setLumber(1);

		game.processDiscard(discard.build(), p.getColour());
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

