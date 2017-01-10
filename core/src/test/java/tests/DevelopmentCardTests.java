package tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import board.Edge;
import board.Hex;
import board.Node;
import enums.*;
import exceptions.*;
import game.build.DevelopmentCard;
import game.build.Road;
import game.build.Settlement;
import game.players.*;

import org.junit.*;
import protocol.BoardProtos.*;
import protocol.BuildProtos;
import protocol.RequestProtos.*;
import protocol.ResponseProtos.*;

public class DevelopmentCardTests extends TestHelper
{
	DevelopmentCard c;
	
	@Before
	public void setUp()
	{
		reset();
		c = new DevelopmentCard();
		c.setColour(p.getColour());
		c.setType(DevelopmentCardType.Library);
	}

	@Test(expected = CannotAffordException.class)
	public void cannotBuyDevCardTest() throws CannotAffordException
	{
		p.buyDevelopmentCard();
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
		p.grantResources(DevelopmentCard.getCardCost());
		buyDevelopmentCard();
		assertTrue(p.getDevelopmentCards().size() > 0);
	}
	
	@Test
	public void playAndRemoveDevelopmentCardTest() throws CannotAffordException, DoesNotOwnException
	{
		// Grant resources and buy a card
		assertTrue(p.getDevelopmentCards().size() == 0);
		p.grantResources(DevelopmentCard.getCardCost());
		buyDevelopmentCard();
		assertTrue(p.getDevelopmentCards().size() > 0);
		
		// Play card and test it was removed
		DevelopmentCardType key = (DevelopmentCardType) p.getDevelopmentCards().keySet().toArray()[0];
		p.playDevelopmentCard(p.getDevelopmentCards().get(key).get(0));
	}
	
	@Test
	public void playMonopolyTest()
	{
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
		
		game.playMonopolyCard(request.build());
		assertEquals(0, p2.getNumResources());
		assertEquals(2, p.getNumResources());
	}

	@Test
	public void playKnightNoResourcesTest() throws SettlementExistsException, CannotAffordException, IllegalPlacementException, CannotStealException
	{	
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		game.addPlayer(p2);
		p2.grantResources(Settlement.getSettlementCost());
		makeSettlement(p2, hex.getNodes().get(0));
		
		// Set up request
		MoveRobberRequest.Builder request = MoveRobberRequest.newBuilder();
		request.setColourToTakeFrom(Colour.toProto(p2.getColour()));
		request.setHex(hex.toHexProto());
				
		// Assert that swap happened, but that no resource was taken
		// as p2 didn't have any
		MoveRobberResponse response = game.moveRobber(request.build(), p.getColour());
		assertTrue(!oldHex.equals(game.getGrid().getHexWithRobber()));
		assertTrue(game.getGrid().getHexWithRobber().hasRobber());
		assertFalse(oldHex.hasRobber());
		assertFalse(hasResources(p2));
	}
	
	@Test
	public void cannotPlayKnightTest() throws CannotAffordException, IllegalPlacementException
	{	
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		p2.grantResources(DevelopmentCard.getCardCost());
		game.addPlayer(p2);

		// Set up request
		MoveRobberRequest.Builder request = MoveRobberRequest.newBuilder();
		request.setColourToTakeFrom(Colour.toProto(p2.getColour()));
		request.setHex(hex.toHexProto());
		
		// Play move and assert robber was moved
		assertTrue(oldHex.hasRobber());
		assertFalse(hasResources(p));
		assertTrue(hasResources(p2));
		
		// Robber will move, but cannot take resource because this player does not have a settlement
		// on one of the hex's nodes
		try
		{
			game.moveRobber(request.build(), p.getColour());
		}
		catch (CannotStealException e)
		{
			// Ensure robber wasn't moved
			assertTrue(oldHex.equals(game.getGrid().getHexWithRobber()));
			assertTrue(game.getGrid().getHexWithRobber().hasRobber());
		}
	}
	
	@Test
	public void playKnightTakeResourceTest() throws SettlementExistsException, CannotAffordException, IllegalPlacementException, CannotStealException
	{	
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		p2.grantResources(DevelopmentCard.getCardCost());
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
		assertTrue(p2.getNumResources() < DevelopmentCard.getCardCost().size());
	}
	
	@Test
	public void playYearOfPlentyTest()
	{
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
	public void playBuildRoadsCardTest() throws SettlementExistsException, CannotBuildRoadException, RoadExistsException, CannotAffordException
	{
		// Set up entities
		Edge e1 = n.getEdges().get(0), e2 = n.getEdges().get(1);
		Node n1 = e1.getX(), n2 = e1.getY(), n3 = e2.getX(), n4 = e2.getY();
		
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
	public void playBuildRoadsCardFailure() throws SettlementExistsException, CannotBuildRoadException, CannotAffordException
	{
		// Set up variables
		Edge e1 = n.getEdges().get(0);
		Node n1 = e1.getX(), n2 = e1.getY();
		int oldResources = 0;
		
		// Set up development card
		p.grantResources(DevelopmentCard.getCardCost());
		DevelopmentCard card = new DevelopmentCard();
		card.setType(DevelopmentCardType.RoadBuilding);
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
			assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.RoadBuilding).size() == 1);
		}

	}
	
	@Test
	public void playLibraryTest()
	{
		// Play move and assert vp has increased
		assertEquals(0, p.getVp());
		game.playLibraryCard();
		assertEquals(1, p.getVp());
	}
	
	@Test
	public void playUniversityTest()
	{
		// Play move and assert vp has increased
		assertEquals(0, p.getVp());
		game.playUniversityCard();
		assertEquals(1, p.getVp());
	}
}
