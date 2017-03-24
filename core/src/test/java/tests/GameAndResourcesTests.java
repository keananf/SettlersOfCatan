package tests;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.ServerPlayer;
import game.players.Player;
import grid.Hex;
import intergroup.board.Board;
import intergroup.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

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
		assertTrue(game.getGrid().grid.values().size() == 19); // number of
																// hexes
		assertTrue(game.getGrid().nodes.values().size() == 54); // number of
																// nodes
		assertTrue(game.getGrid().ports.size() == 9); // number of ports
	}

	@Test
	public void collectResourcesTest() throws SettlementExistsException, BankLimitException, CannotAffordException
	{
		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);
		p.spendResources(Settlement.getSettlementCost(), game.getBank());

		// collect resources
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 1);
	}

	@Test
	public void collectResourcesWithRobberTest()
			throws SettlementExistsException, BankLimitException, CannotAffordException
	{
		// Make a settlement and toggle the robber on its hex
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);
		p.spendResources(Settlement.getSettlementCost(), game.getBank());
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

		game.setCurrentPlayer(p.getColour());
		game.moveRobber(point.build());
	}

	@Test
	public void moveRobberTest() throws InvalidCoordinatesException, CannotStealException, SettlementExistsException,
			BankLimitException, CannotAffordException
	{
		// Make a second player
		Player p2 = new ServerPlayer(Colour.RED, "");
		p2.setId(Board.Player.Id.PLAYER_2);
		game.addPlayer(p2);

		// Give player 2 resources to take
		p2.grantResources(Road.getRoadCost(), game.getBank());

		// Grant player 2 a settlement so that player 1 will be allowed to take
		// from them
		p2.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p2, n);
		p2.spendResources(Settlement.getSettlementCost(), game.getBank());

		// Make a move robber request
		Board.Point.Builder point = Board.Point.newBuilder().setX(hex.getX()).setY(hex.getY());

		// Test values before move
		assertEquals(p2.getNumResources(), 2);
		assertEquals(p.getNumResources(), 0);
		Hex h = game.getGrid().getHexWithRobber();

		// Move and check
		game.setCurrentPlayer(p.getColour());
		game.moveRobber(point.build());
		game.takeResource(p2.getId());
		assertNotEquals(h, game.getGrid().getHexWithRobber());
		assertEquals(p.getNumResources(), 1);
		assertEquals(p2.getNumResources(), 1);
	}

	@Test(expected = InvalidDiscardRequest.class)
	public void invalidDiscardTest() throws InvalidDiscardRequest, CannotAffordException, BankLimitException
	{
		// Grant player more than 7 resources
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		assertEquals(12, p.getNumResources());

		// Construct discard request. Discarding one card is NOT enough
		Resource.Counts.Builder discard = Resource.Counts.newBuilder();
		discard.setLumber(1);

		game.processDiscard(discard.build(), p.getColour());
	}

	@Test(expected = CannotAffordException.class)
	public void invalidDiscardTest2() throws InvalidDiscardRequest, CannotAffordException, BankLimitException
	{
		// Grant player more than 7 resources
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		assertEquals(8, p.getNumResources());

		// Construct discard request. Don't have enough
		Resource.Counts.Builder discard = Resource.Counts.newBuilder();
		discard.setLumber(3);

		game.processDiscard(discard.build(), p.getColour());
	}

	@Test
	public void discardTest() throws InvalidDiscardRequest, CannotAffordException, BankLimitException
	{
		// Grant player more than 7 resources
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		assertEquals(8, p.getNumResources());

		// Construct discard request
		Resource.Counts.Builder discard = Resource.Counts.newBuilder();
		discard.setLumber(1);

		game.processDiscard(discard.build(), p.getColour());
	}

	@Test
	public void collectResourcesCityTest() throws SettlementExistsException, BankLimitException, CannotAffordException
	{
		// Grant resources for and make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		Settlement s = makeSettlement(p, n);
		p.spendResources(Settlement.getSettlementCost(), game.getBank());

		// Grant resources for and upgrade settlement
		p.grantResources(City.getCityCost(), game.getBank());
		City c = makeCity(p, n);
		assertEquals(c.getNode(), s.getNode()); // assert the upgrade happened

		// collect 2 of this resource
		game.allocateResources(hex.getChit());
		assertTrue(hasResources(p));
		assertTrue(p.getResources().get(hex.getResource()) == 2);
	}

	@Test
	public void settlementLimitTest() throws SettlementExistsException, IllegalPlacementException,
			InvalidCoordinatesException, CannotAffordException, CannotUpgradeException, BankLimitException
	{
		int settlementsLimit = game.getBank().getAvailableSettlements();
		int cityLimit = game.getBank().getAvailableCities();
		int resourceLimit = game.getBank().getNumAvailableResources();

		// Grant resources for and make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		assertTrue(game.getBank().getNumAvailableResources() == resourceLimit - 4);
		Settlement s = makeSettlement(p, n);
		p.spendResources(Settlement.getSettlementCost(), game.getBank());
		assertTrue(game.getBank().getNumAvailableResources() == resourceLimit);
		assertTrue(game.getBank().getAvailableSettlements() == settlementsLimit - 1);
		assertTrue(game.getBank().getAvailableCities() == cityLimit);

		// Upgrade settlement. Check resources
		p.grantResources(City.getCityCost(), game.getBank());
		assertTrue(game.getBank().getNumAvailableResources() == resourceLimit - 5);
		City c = makeCity(p, n);
		assertTrue(game.getBank().getNumAvailableResources() == resourceLimit);
		assertTrue(game.getBank().getAvailableSettlements() == settlementsLimit);
		assertTrue(game.getBank().getAvailableCities() == cityLimit - 1);
	}

	@Test
	public void roadLimitTest()
			throws CannotBuildRoadException, RoadExistsException, SettlementExistsException, BankLimitException
	{
		int roadLimit = game.getBank().getAvailableRoads();
		int settlementsLimit = game.getBank().getAvailableSettlements();

		// Grant resources for and make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		Settlement s = makeSettlement(p, n);
		assertTrue(game.getBank().getAvailableSettlements() == settlementsLimit - 1);

		// Grant resources for and make road
		p.grantResources(Road.getRoadCost(), game.getBank());
		buildRoad(p, n.getEdges().get(0));
		assertTrue(game.getBank().getAvailableRoads() == roadLimit - 1);
	}

	@Test(expected = BankLimitException.class)
	public void devCardLimitTest() throws BankLimitException, CannotAffordException
	{
		game.setCurrentPlayer(p.getColour());

		// Grant player all dev cards
		int amount = game.getBank().getNumAvailableDevCards();
		for (int i = 0; i < amount; i++)
		{
			p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
			game.buyDevelopmentCard();
		}

		int num = 0;
		// Check number of dev cards
		for (DevelopmentCardType d : p.getDevelopmentCards().keySet())
		{
			num += p.getDevelopmentCards().get(d);
		}

		// Assert player has all dev cards
		assertEquals(amount, num);

		// Try to buy one more to cause exception
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		game.buyDevelopmentCard();
	}

	@Test
	public void resourceLimitTest() throws BankLimitException, CannotAffordException, SettlementExistsException
	{
		int amount = 19;

		// Make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);

		// Get chit and simulate a dice roll 19 times.
		// Grants all of this resource.
		int chit = n.getHexes().get(0).getChit();
		ResourceType r = n.getHexes().get(0).getResource();
		for (int i = 0; i < amount; i++)
		{
			game.allocateResources(chit);
		}

		// Assert player has all of this resource
		assertTrue(p.getResources().get(r) == amount);
		assertTrue(0 == game.getBank().getAvailableResources().get(r));

		// Attempt to grant again, and check that the resulting map is empty
		Map<Colour, Map<ResourceType, Integer>> map = game.allocateResources(chit);
		assertTrue(map.get(p.getColour()).size() == 0);
		assertTrue(map.size() == 1);
	}
}
