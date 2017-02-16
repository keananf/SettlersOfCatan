package tests;

import board.Board;
import grid.*;
import exceptions.*;
import game.build.City;
import game.build.Settlement;
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
		p.buildSettlement(n);
	}

	@Test(expected = CannotAffordException.class)
	public void cannotAffordCityTest() throws SettlementExistsException, CannotAffordException, CannotUpgradeException
	{
		// Test resources
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(hasResources(p));

		// Build settlement
		makeSettlement(p, n);

		p.upgradeSettlement(n);
	}

	@Test(expected = CannotUpgradeException.class)
	public void cannotBuildCityTest() throws CannotAffordException, CannotUpgradeException
	{
		// Cannot build city on node unless a settlement is already there
		p.upgradeSettlement(n);
	}

	@Test
	public void buildSettlementTest() throws SettlementExistsException
	{
		// Test resources
		assertFalse(hasResources(p));
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(hasResources(p));

		// Build settlement
		makeSettlement(p, n);
	}

	@Test(expected = SettlementExistsException.class)
	public void duplicateSettlementTest() throws SettlementExistsException
	{
		// Grant resources for two settlements
		p.grantResources(Settlement.getSettlementCost());
		p.grantResources(Settlement.getSettlementCost());

		// Build settlement, and attempt to build at same node again.
		// Exception will be thrown
		makeSettlement(p, n);
		makeSettlement(p, n);
	}
	
	@Test(expected = IllegalPlacementException.class)
	public void tooCloseToSettlementTest() throws SettlementExistsException, IllegalPlacementException
	{
		// Grant resources and build first settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		
		// Find adjacent node
		Node x = n.getEdges().get(0).getX(), y = n.getEdges().get(0).getY();
		Node n2 = x.equals(n) ? y : x;
		try
		{
			// Grant resources and try to build a settlement
			p.grantResources(Settlement.getSettlementCost());
			p.buildSettlement(n2);
		}
		catch (SettlementExistsException | CannotAffordException e)
		{
			e.printStackTrace();
		}
	}

	@Test(expected = InvalidCoordinatesException.class)
	public void invalidCoordinatesSettlement() throws CannotAffordException, InvalidCoordinatesException,
											IllegalPlacementException, SettlementExistsException
	{
		// Create protobuf representation of building a settlement
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setX(-10);
		point.setY(-30);

		// Grant resources
		p.grantResources(Settlement.getSettlementCost());

		// Try to build
		game.setTurn(p.getColour());
		game.buildSettlement(point.build());
	}


	@Test(expected = InvalidCoordinatesException.class)
	public void invalidCoordinatesCity() throws CannotAffordException, InvalidCoordinatesException,
			IllegalPlacementException, SettlementExistsException, CannotUpgradeException
	{
		// Create protobuf representation of building a settlement
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setX(-10);
		point.setY(-30);

		// Grant resources
		p.grantResources(Settlement.getSettlementCost());

		// Try to build
		game.setTurn(p.getColour());
		game.upgradeSettlement(point.build());
	}


	@Test
	public void buildSettlementTest2() throws CannotAffordException, InvalidCoordinatesException,
			IllegalPlacementException, SettlementExistsException
	{
		// Create protobuf representation of building a settlement
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setX(n.getX());
		point.setY(n.getY());

		// Grant resources
		p.grantResources(Settlement.getSettlementCost());

		// Try to build
		game.setTurn(p.getColour());
		game.buildSettlement(point.build());
	}


	@Test
	public void buildCityTest2() throws CannotAffordException, InvalidCoordinatesException,
			IllegalPlacementException, SettlementExistsException, CannotUpgradeException
	{
		// Grant resources and build settlement so it can be upgraded
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);

		// Create protobuf representation of building a settlement
		Board.Point.Builder point = Board.Point.newBuilder();
		point.setX(n.getX());
		point.setY(n.getY());

		// Grant resources
		p.grantResources(City.getCityCost());

		// Try to build
		game.setTurn(p.getColour());
		game.upgradeSettlement(point.build());
		assertEquals(p.getSettlements().size(), 1);
		assertTrue(p.getSettlements().get(new Point(n.getX(), n.getY())) instanceof City);
	}
}
