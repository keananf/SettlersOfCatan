package tests;

import exceptions.*;
import game.build.City;
import game.build.Settlement;
import grid.Node;
import intergroup.board.Board;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

public class SettlementAndCityTests extends TestHelper
{
	@Before
	public void setUp()
	{
		reset();
	}


	@Test(expected = CannotAffordException.class)
	public void cannotBuildSettlementTest() throws CannotAffordException, IllegalPlacementException, SettlementExistsException
	{
		p.buildSettlement(n, game.getBank());
	}

	@Test(expected = CannotAffordException.class)
	public void cannotAffordCityTest() throws SettlementExistsException, CannotAffordException, CannotUpgradeException, BankLimitException
	{
		// Test resources
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		assertTrue(hasResources(p));

		// Build settlement
		makeSettlement(p, n);

		p.upgradeSettlement(n, game.getBank());
	}

	@Test(expected = CannotUpgradeException.class)
	public void cannotBuildCityTest() throws CannotAffordException, CannotUpgradeException
	{
		// Cannot build city on node unless a settlement is already there
		p.upgradeSettlement(n, game.getBank());
	}

	@Test
	public void buildSettlementTest() throws SettlementExistsException, BankLimitException
	{
		// Test resources
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		assertTrue(hasResources(p));

		// Build settlement
		makeSettlement(p, n);
	}

	@Test(expected = SettlementExistsException.class)
	public void duplicateSettlementTest() throws SettlementExistsException, BankLimitException
	{
		// Grant resources for two settlements
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		p.grantResources(Settlement.getSettlementCost(), game.getBank());

		// Build settlement, and attempt to build at same node again.
		// Exception will be thrown
		makeSettlement(p, n);
		makeSettlement(p, n);
	}
	
	@Test(expected = IllegalPlacementException.class)
	public void tooCloseToSettlementTest() throws SettlementExistsException, IllegalPlacementException, BankLimitException
	{
		// Grant resources and build first settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);
		
		// Find adjacent node
		Node x = n.getEdges().get(0).getX(), y = n.getEdges().get(0).getY();
		Node n2 = x.equals(n) ? y : x;
		try
		{
			// Grant resources and try to build a settlement
			p.grantResources(Settlement.getSettlementCost(), game.getBank());
			p.buildSettlement(n2, game.getBank());
		}
		catch (SettlementExistsException | CannotAffordException e)
		{
			e.printStackTrace();
		}
	}

	@Test(expected = InvalidCoordinatesException.class)
	public void invalidCoordinatesSettlement() throws CannotAffordException, InvalidCoordinatesException,
			IllegalPlacementException, SettlementExistsException, BankLimitException
	{
		// Create protobuf representation of building a settlement
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setX(-10);
		point.setY(-30);

		// Grant resources
		p.grantResources(Settlement.getSettlementCost(), game.getBank());

		// Try to build
		game.setTurn(p.getColour());
		game.buildSettlement(point.build());
	}


	@Test(expected = InvalidCoordinatesException.class)
	public void invalidCoordinatesCity() throws CannotAffordException, InvalidCoordinatesException,
			IllegalPlacementException, SettlementExistsException, CannotUpgradeException, BankLimitException
	{
		// Create protobuf representation of building a settlement
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setX(-10);
		point.setY(-30);

		// Grant resources
		p.grantResources(Settlement.getSettlementCost(), game.getBank());

		// Try to build
		game.setTurn(p.getColour());
		game.upgradeSettlement(point.build());
	}


	@Test
	public void buildSettlementTest2() throws CannotAffordException, InvalidCoordinatesException,
			IllegalPlacementException, SettlementExistsException, BankLimitException
	{
		// Create protobuf representation of building a settlement
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setX(n.getX());
		point.setY(n.getY());

		// Grant resources
		p.grantResources(Settlement.getSettlementCost(), game.getBank());

		// Try to build
		game.setTurn(p.getColour());
		game.buildSettlement(point.build());
	}


	@Test
	public void buildCityTest2() throws CannotAffordException, InvalidCoordinatesException,
			IllegalPlacementException, SettlementExistsException, CannotUpgradeException, BankLimitException
	{
		// Grant resources and build settlement so it can be upgraded
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);

		// Create protobuf representation of building a settlement
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setX(n.getX());
		point.setY(n.getY());

		// Grant resources
		p.grantResources(City.getCityCost(), game.getBank());

		// Try to build
		game.setTurn(p.getColour());
		game.upgradeSettlement(point.build());
		assertEquals(p.getSettlements().size(), 1);
		assertTrue(p.getSettlements().get(new Point(n.getX(), n.getY())) instanceof City);
	}
}
