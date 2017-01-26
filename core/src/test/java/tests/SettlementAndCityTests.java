package tests;

import board.Node;
import exceptions.*;
import game.build.Settlement;
import org.junit.Before;
import org.junit.Test;
import protocol.BuildProtos;
import protocol.RequestProtos.BuildSettlementRequest;
import protocol.RequestProtos.UpgradeSettlementRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		BuildSettlementRequest.Builder req = BuildSettlementRequest.newBuilder();
		BuildProtos.PointProto.Builder point = BuildProtos.PointProto.newBuilder();
		point.setX(-10);
		point.setY(-30);
		req.setPoint(point.build());

		// Grant resources
		p.grantResources(Settlement.getSettlementCost());

		// Try to build
		game.buildSettlement(req.build(), p.getColour());
	}


	@Test(expected = InvalidCoordinatesException.class)
	public void invalidCoordinatesCity() throws CannotAffordException, InvalidCoordinatesException,
			IllegalPlacementException, SettlementExistsException, CannotUpgradeException
	{
		// Create protobuf representation of building a settlement
		UpgradeSettlementRequest.Builder req = UpgradeSettlementRequest.newBuilder();
		BuildProtos.PointProto.Builder point = BuildProtos.PointProto.newBuilder();
		point.setX(-10);
		point.setY(-30);
		req.setPoint(point.build());

		// Grant resources
		p.grantResources(Settlement.getSettlementCost());

		// Try to build
		game.upgradeSettlement(req.build(), p.getColour());
	}
}
