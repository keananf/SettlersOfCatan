package tests;

import exceptions.*;
import game.build.City;
import game.build.Settlement;
import grid.Edge;
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
	public void cannotBuildSettlementTest()
			throws CannotAffordException, IllegalPlacementException, SettlementExistsException
	{
		// Find edges
		Edge e1 = n.getEdges().get(0);
		Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end of first edge
		Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0);
		Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end of second edge
		Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0);
		Node n3 = e3.getX().equals(n2) ? e3.getY() : e3.getX(); // Opposite end of third edge
		Edge e4 = n3.getEdges().get(0).equals(e3) ? n3.getEdges().get(1) : n3.getEdges().get(0);
		Node n4 = e4.getX().equals(n3) ? e4.getY() : e4.getX(); // Opposite end of fourth edge
		Edge e5 = n4.getEdges().get(0).equals(e4) ? n4.getEdges().get(1) : n4.getEdges().get(0);
		Node n5 = e5.getX().equals(n4) ? e5.getY() : e5.getX(); // Opposite end of fifth edge
		Edge e6 = n5.getEdges().get(0).equals(e5) ? n5.getEdges().get(1) : n5.getEdges().get(0);
		Node n6 = e6.getX().equals(n5) ? e6.getY() : e6.getX(); // Opposite end of sixth edge

		// Make two settlements
		makeSettlement(p, n);
		makeSettlement(p, n6);

		// make third without resources
		p.buildSettlement(n3, game.getBank());
	}

	@Test(expected = CannotAffordException.class)
	public void cannotAffordCityTest()
			throws SettlementExistsException, CannotAffordException, CannotUpgradeException, BankLimitException
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
	public void tooCloseToSettlementTest()
			throws SettlementExistsException, IllegalPlacementException, BankLimitException
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
		game.setCurrentPlayer(p.getColour());
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
		game.setCurrentPlayer(p.getColour());
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
		game.setCurrentPlayer(p.getColour());
		game.buildSettlement(point.build());
	}

	@Test
	public void buildCityTest2() throws CannotAffordException, InvalidCoordinatesException, IllegalPlacementException,
			SettlementExistsException, CannotUpgradeException, BankLimitException
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
		game.setCurrentPlayer(p.getColour());
		game.upgradeSettlement(point.build());
		assertEquals(p.getSettlements().size(), 1);
		assertTrue(p.getSettlements().get(new Point(n.getX(), n.getY())) instanceof City);
	}
}
