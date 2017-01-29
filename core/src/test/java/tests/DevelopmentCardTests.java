package tests;

import board.Edge;
import board.Hex;
import board.Node;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.build.Road;
import game.build.Settlement;
import game.players.NetworkPlayer;
import game.players.Player;
import org.junit.Before;
import org.junit.Test;
import protocol.BoardProtos.EdgeProto;
import protocol.BuildProtos;
import protocol.RequestProtos;
import protocol.RequestProtos.*;
import protocol.ResponseProtos.MoveRobberResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DevelopmentCardTests extends TestHelper
{
	DevelopmentCardType c;
	
	@Before
	public void setUp()
	{
		reset();
		c = DevelopmentCardType.Library;
	}

	@Test(expected = CannotAffordException.class)
	public void cannotBuyDevCardTest() throws CannotAffordException
	{
		p.buyDevelopmentCard(DevelopmentCardType.RoadBuilding);
	}
	
	@Test(expected = DoesNotOwnException.class)
	public void cannotPlayDevCardTest() throws DoesNotOwnException
	{
		// This development card does not exist in the player's hand 
		p.playDevelopmentCard(c);
	}	

	@Test
	public void buyDevelopmentCardTest() throws CannotAffordException
	{
		// Grant resources and buy a card
		assertTrue(p.getDevelopmentCards().size() == 0);
		p.grantResources(DevelopmentCardType.getCardCost());
		buyDevelopmentCard();
		assertTrue(p.getDevelopmentCards().size() > 0);
	}
	
	@Test
	public void playAndRemoveDevelopmentCardTest() throws CannotAffordException, DoesNotOwnException
	{
		// Grant resources and buy a card
		assertTrue(p.getDevelopmentCards().size() == 0);
		p.grantResources(DevelopmentCardType.getCardCost());
		DevelopmentCardType c = buyDevelopmentCard();
		assertTrue(p.getDevelopmentCards().get(c) == 1);
		
		// Play card and test it was removed
		DevelopmentCardType key = (DevelopmentCardType) p.getDevelopmentCards().keySet().toArray()[0];
		p.playDevelopmentCard(key);

		assertTrue(p.getDevelopmentCards().get(c) == 0);
	}


	@Test
	public void largestArmyTest() throws SettlementExistsException, CannotStealException,
			InvalidCoordinatesException, DoesNotOwnException, CannotAffordException
	{
		NetworkPlayer p2 = new NetworkPlayer(Colour.RED);
		game.addPlayer(p2);

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

		// Make settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);

		// Need a settlement so that this player can be stolen from
		p2.grantResources(Settlement.getSettlementCost());
		makeSettlement(p2, n6);

		// Player 1 plays three knights
		for(Hex h : n6.getHexes())
		{
			RequestProtos.PlayKnightCardRequest.Builder req = RequestProtos.PlayKnightCardRequest.newBuilder();
			RequestProtos.MoveRobberRequest.Builder internalReq = RequestProtos.MoveRobberRequest.newBuilder();
			internalReq.setHex(h.toHexProto());
			internalReq.setColourToTakeFrom(Colour.toProto(p2.getColour()));
			req.setRequest(internalReq.build());

			// Grant Card
			p.grantResources(DevelopmentCardType.getCardCost());
			p.buyDevelopmentCard(DevelopmentCardType.Knight);
			game.playKnightCard(req.build(), p.getColour());
		}

		// Assert largest army
		assertEquals(3, p.getVp());
		assertEquals(1, p2.getVp());

		// Have player 2 play four knights, so largest army is revoked.
		while(p2.getArmySize() < 4)
		{
			Hex h = n.getHexes().get(0);RequestProtos.PlayKnightCardRequest.Builder req = RequestProtos.PlayKnightCardRequest.newBuilder();
			RequestProtos.MoveRobberRequest.Builder internalReq = RequestProtos.MoveRobberRequest.newBuilder();
			internalReq.setHex(h.toHexProto());
			internalReq.setColourToTakeFrom(Colour.toProto(p.getColour()));
			req.setRequest(internalReq.build());

			// Grant Card
			p2.grantResources(DevelopmentCardType.getCardCost());
			p2.buyDevelopmentCard(DevelopmentCardType.Knight);
			game.playKnightCard(req.build(), p2.getColour());
		}

		assertEquals(1, p.getVp());
		assertEquals(3, p2.getVp());
	}

	@Test
	public void playMonopolyTest() throws DoesNotOwnException, CannotAffordException
	{
		// Grant Card
		p.grantResources(DevelopmentCardType.getCardCost());
		p.buyDevelopmentCard(DevelopmentCardType.Monopoly);

		// Set-up resources to be taken when playing the development card 
		Player p2 = new NetworkPlayer(Colour.RED);
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		ResourceType r = ResourceType.Brick;
		
		// Give resources to new player and add them to game
		grant.put(r, 2);
		game.addPlayer(p2);
		p2.grantResources(grant);

		// Set up request
		PlayMonopolyCardRequest.Builder request = PlayMonopolyCardRequest.newBuilder();
		request.setResource(ResourceType.toProto(r));

		// Play request and assert resources were transferred
		assertEquals(2, p2.getNumResources());
		assertEquals(0, p.getNumResources());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Monopoly) == 1);

		game.playMonopolyCard(request.build());
		assertEquals(0, p2.getNumResources());
		assertEquals(2, p.getNumResources());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Monopoly) == 0);
	}

	@Test
	public void playKnightNoResourcesTest() throws SettlementExistsException, CannotAffordException,
			IllegalPlacementException, CannotStealException, DoesNotOwnException, InvalidCoordinatesException
	{
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Grant
		p.grantResources(DevelopmentCardType.getCardCost());
		p.buyDevelopmentCard(DevelopmentCardType.Knight);

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		game.addPlayer(p2);
		p2.grantResources(Settlement.getSettlementCost());
		makeSettlement(p2, hex.getNodes().get(0));
		
		// Set up request
		PlayKnightCardRequest.Builder request = PlayKnightCardRequest.newBuilder();
		MoveRobberRequest.Builder internalReq = MoveRobberRequest.newBuilder();
		internalReq.setColourToTakeFrom(Colour.toProto(p2.getColour()));
		internalReq.setHex(hex.toHexProto());
		request.setRequest(internalReq);
				
		// Assert that swap happened, but that no resource was taken
		// as p2 didn't have any
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Knight) == 1);
		game.playKnightCard(request.build(), p.getColour());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Knight) == 0);
		assertTrue(!oldHex.equals(game.getGrid().getHexWithRobber()));
		assertFalse(oldHex.hasRobber());
		assertFalse(hasResources(p2));
	}

	@Test(expected = DoesNotOwnException.class)
	public void cannotPlayKnightTest() throws CannotAffordException, IllegalPlacementException,
			DoesNotOwnException, CannotStealException, InvalidCoordinatesException
	{
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		p2.grantResources(DevelopmentCardType.getCardCost());
		game.addPlayer(p2);

		// Set up request
		PlayKnightCardRequest.Builder request = PlayKnightCardRequest.newBuilder();
		MoveRobberRequest.Builder internalReq = MoveRobberRequest.newBuilder();
		internalReq.setColourToTakeFrom(Colour.toProto(p2.getColour()));
		internalReq.setHex(hex.toHexProto());
		request.setRequest(internalReq);

		// Play move and assert robber was moved
		assertTrue(oldHex.hasRobber());
		assertFalse(hasResources(p));
		assertTrue(hasResources(p2));

		// Cannot play because do not own a card
		game.playKnightCard(request.build(), p.getColour());
	}

	@Test
	public void cannotStealFromSpecifiedPlayerTest() throws CannotAffordException, IllegalPlacementException,
			DoesNotOwnException, InvalidCoordinatesException
	{
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Grant
		p.grantResources(DevelopmentCardType.getCardCost());
		p.buyDevelopmentCard(DevelopmentCardType.Knight);

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		p2.grantResources(DevelopmentCardType.getCardCost());
		game.addPlayer(p2);

		// Set up request

		PlayKnightCardRequest.Builder request = PlayKnightCardRequest.newBuilder();
		MoveRobberRequest.Builder internalReq = MoveRobberRequest.newBuilder();
		internalReq.setColourToTakeFrom(Colour.toProto(p2.getColour()));
		internalReq.setHex(hex.toHexProto());
		request.setRequest(internalReq);
		
		// Play move and assert robber was moved
		assertTrue(oldHex.hasRobber());
		assertFalse(hasResources(p));
		assertTrue(hasResources(p2));
		
		// Robber will move, but cannot take resource because this player does not have a settlement
		// on one of the hex's nodes
		try
		{
			game.playKnightCard(request.build(), p.getColour());
		}
		catch (CannotStealException e)
		{
			// Ensure robber wasn't moved
			assertTrue(oldHex.equals(game.getGrid().getHexWithRobber()));
			assertTrue(game.getGrid().getHexWithRobber().hasRobber());
		}
	}
	
	@Test
	public void playKnightTakeResourceTest() throws SettlementExistsException, CannotAffordException,
			IllegalPlacementException, CannotStealException, InvalidCoordinatesException
	{
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Grant
		p.grantResources(DevelopmentCardType.getCardCost());
		p.buyDevelopmentCard(DevelopmentCardType.Knight);

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		p2.grantResources(DevelopmentCardType.getCardCost());
		game.addPlayer(p2);
		p2.grantResources(Settlement.getSettlementCost());
		makeSettlement(p2, hex.getNodes().get(0));
		
		// Set up request
		MoveRobberRequest.Builder request = MoveRobberRequest.newBuilder();
		request.setColourToTakeFrom(Colour.toProto(p2.getColour()));
		request.setHex(hex.toHexProto());
		
		// Play move and assert robber was moved
		assertTrue(oldHex.hasRobber());
		assertFalse(hasResources(p));
		assertTrue(hasResources(p2));
		
		MoveRobberResponse response = game.moveRobber(request.build(), p.getColour());
		assertTrue(!oldHex.equals(game.getGrid().getHexWithRobber()));
		assertTrue(game.getGrid().getHexWithRobber().hasRobber());
		assertFalse(oldHex.hasRobber());
		assertTrue(hasResources(p));
		assertTrue(p2.getNumResources() < DevelopmentCardType.getCardCost().size());
	}
	
	@Test
	public void playYearOfPlentyTest() throws DoesNotOwnException, CannotAffordException
	{
		// Grant
		p.grantResources(DevelopmentCardType.getCardCost());
		p.buyDevelopmentCard(DevelopmentCardType.YearOfPlenty);

		// Set up request
		PlayYearOfPlentyCardRequest.Builder request = PlayYearOfPlentyCardRequest.newBuilder();
		request.setR1(ResourceType.toProto(ResourceType.Brick));
		request.setR2(ResourceType.toProto(ResourceType.Grain));
		
		// Play request and assert resources were transferred
		assertEquals(0, p.getNumResources());	
		game.playYearOfPlentyCard(request.build());
		assertEquals(2, p.getNumResources());
	}
	
	@Test
	public void playBuildRoadsCardTest() throws SettlementExistsException, CannotBuildRoadException,
			RoadExistsException, CannotAffordException, DoesNotOwnException, InvalidCoordinatesException
	{
		// Set up entities
		Edge e1 = n.getEdges().get(0), e2 = n.getEdges().get(1);
		Node n1 = e1.getX(), n2 = e1.getY(), n3 = e2.getX(), n4 = e2.getY();
		p.grantResources(DevelopmentCardType.getCardCost());
		p.buyDevelopmentCard(DevelopmentCardType.RoadBuilding);

		// Set up moves
		PlayRoadBuildingCardRequest.Builder request = PlayRoadBuildingCardRequest.newBuilder();
		BuildRoadRequest.Builder roadReq = BuildRoadRequest.newBuilder();
		EdgeProto.Builder edge = EdgeProto.newBuilder();
		BuildProtos.PointProto.Builder point = BuildProtos.PointProto.newBuilder();

		// Node 1 of road 1
		point.setX(n1.getX());
		point.setY(n1.getY());
		edge.setP1(point.build());

		// Node 2 of road 1
		point.setX(n2.getX());
		point.setY(n2.getY());
		edge.setP2(point.build());

		// Add edge to BuildRoadRequest, and add first req to overall request
		roadReq.setEdge(edge.build());
		request.setRequest1(roadReq.build());

		// Node 1 of road 2
		point.setX(n3.getX());
		point.setY(n3.getY());
		edge.setP1(point.build());

		// Node 2 of road 2
		point.setX(n4.getX());
		point.setY(n4.getY());
		edge.setP2(point.build());

		// Add edge to BuildRoadRequest, and add second req to overall request
		roadReq.setEdge(edge.build());
		request.setRequest2(roadReq.build());

		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		
		// Grant resources and Build roads
		p.grantResources(Road.getRoadCost());
		p.grantResources(Road.getRoadCost());
		game.playBuildRoadsCard(request.build(), p.getColour());
		
		assertTrue(p.getRoads().size() == 2);
		assertTrue(p.getRoads().get(0).getEdge().equals(e1));
		assertTrue(p.getRoads().get(1).getEdge().equals(e2));
	}
	
	/**
	 * Tests atomicity and end-to-end processing of a multi-part move
	 * @throws CannotBuildRoadException
	 * @throws CannotAffordException
	 */
	@Test
	public void playBuildRoadsCardFailure() throws SettlementExistsException, CannotBuildRoadException,
			CannotAffordException, DoesNotOwnException, InvalidCoordinatesException
	{
		// Set up variables
		Edge e1 = n.getEdges().get(0);
		Node n1 = e1.getX(), n2 = e1.getY();
		int oldResources = 0;
		
		// Set up development card
		p.grantResources(DevelopmentCardType.getCardCost());
		DevelopmentCardType card = DevelopmentCardType.RoadBuilding;
		p.buyDevelopmentCard(card);
		
		// Set up moves. Make move2 a duplicate of move1 to throw an exception
		PlayRoadBuildingCardRequest.Builder request = PlayRoadBuildingCardRequest.newBuilder();
		BuildRoadRequest.Builder roadReq = BuildRoadRequest.newBuilder();
		EdgeProto.Builder edge = EdgeProto.newBuilder();
		BuildProtos.PointProto.Builder point = BuildProtos.PointProto.newBuilder();

		// Node 1 of road 1
		point.setX(n1.getX());
		point.setY(n1.getY());
		edge.setP1(point.build());

		// Node 2 of road 1
		point.setX(n2.getX());
		point.setY(n2.getY());
		edge.setP2(point.build());

		// Add edge to BuildRoadRequest, and add request twice so error is thrown
		roadReq.setEdge(edge.build());
		request.setRequest1(roadReq.build());
		request.setRequest2(roadReq.build());

		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(p, n);
		
		// Grant resources and Build roads
		p.grantResources(Road.getRoadCost());
		p.grantResources(Road.getRoadCost());
		oldResources = p.getNumResources();

		// Catch thrown exception and ensure player does NOT have first road
		Player copy = p.copy();
		try
		{
			game.playBuildRoadsCard(request.build(), p.getColour()); // FAILS
		}
		catch (RoadExistsException e)
		{
			// Simulate rollback which occurs at server level
			p.restoreCopy(copy, null);

			// Ensure player wasn't updated, and that the dev card was not spent
			assertEquals(0, p.getRoads().size());
			assertEquals(oldResources, p.getNumResources());
			assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.RoadBuilding) == 1);
		}

	}
	
	@Test
	public void playLibraryTest() throws DoesNotOwnException, CannotAffordException
	{
		// Grant Card
		p.grantResources(DevelopmentCardType.getCardCost());
		p.buyDevelopmentCard(DevelopmentCardType.Library);

		// Play move and assert vp has increased
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Library) == 1);
		assertEquals(0, p.getVp());
		game.playLibraryCard();
		assertEquals(1, p.getVp());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Library) == 0);
	}
	
	@Test
	public void playUniversityTest() throws DoesNotOwnException, CannotAffordException
	{
		// Grant Card
		p.grantResources(DevelopmentCardType.getCardCost());
		p.buyDevelopmentCard(DevelopmentCardType.University);

		// Play move and assert vp has increased
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.University) == 1);
		assertEquals(0, p.getVp());
		game.playUniversityCard();
		assertEquals(1, p.getVp());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.University) == 0);
	}
}
