package tests;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.build.Road;
import game.build.Settlement;
import game.players.ServerPlayer;
import game.players.Player;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.Messages;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import server.ReceivedMessage;
import server.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DevelopmentCardTests extends TestHelper
{
	DevelopmentCardType c;
	private Server server;

	@Before
	public void setUp()
	{
		reset();
		c = DevelopmentCardType.Library;
		server = new Server();
		server.setGame(game);
	}

	@Test(expected = CannotAffordException.class)
	public void cannotBuyDevCardTest() throws CannotAffordException, BankLimitException
	{
		p.buyDevelopmentCard(game.getBank());
	}

	@Test(expected = DoesNotOwnException.class)
	public void cannotPlayDevCardTest() throws DoesNotOwnException, CannotPlayException
	{
		// This development card does not exist in the player's hand
		p.playDevelopmentCard(c);
	}

	@Test
	public void buyDevelopmentCardTest() throws CannotAffordException, BankLimitException
	{
		// Grant resources and buy a card
		assertTrue(p.getDevelopmentCards().size() == 0);
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		buyDevelopmentCard();
		assertTrue(p.getDevelopmentCards().size() > 0);
	}

	@Test
	public void playAndRemoveDevelopmentCardTest()
			throws CannotAffordException, DoesNotOwnException, BankLimitException, CannotPlayException
	{
		// Grant resources and buy a card
		assertTrue(p.getDevelopmentCards().size() == 0);
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		DevelopmentCardType c = buyDevelopmentCard();
		assertTrue(p.getDevelopmentCards().get(c) == 1);

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Play card and test it was removed
		DevelopmentCardType key = (DevelopmentCardType) p.getDevelopmentCards().keySet().toArray()[0];
		p.playDevelopmentCard(key);

		assertTrue(p.getDevelopmentCards().get(c) == 0);
	}

	@Test
	public void largestArmyTest() throws SettlementExistsException, CannotStealException, InvalidCoordinatesException,
			DoesNotOwnException, CannotAffordException, IOException, BankLimitException
	{
		ServerPlayer p2 = new ServerPlayer(Colour.RED, "");
		p2.setId(Board.Player.Id.PLAYER_2);
		game.addPlayer(p2);

		// Find edges
		Edge e1 = n.getEdges().get(0);
		Node n1 = e1.getX().equals(n) ? e1.getY() : e1.getX(); // Opposite end
																// of first edge
		Edge e2 = n1.getEdges().get(0).equals(e1) ? n1.getEdges().get(1) : n1.getEdges().get(0);
		Node n2 = e2.getX().equals(n1) ? e2.getY() : e2.getX(); // Opposite end
																// of second
																// edge
		Edge e3 = n2.getEdges().get(0).equals(e2) ? n2.getEdges().get(1) : n2.getEdges().get(0);
		Node n3 = e3.getX().equals(n2) ? e3.getY() : e3.getX(); // Opposite end
																// of third edge
		Edge e4 = n3.getEdges().get(0).equals(e3) ? n3.getEdges().get(1) : n3.getEdges().get(0);
		Node n4 = e4.getX().equals(n3) ? e4.getY() : e4.getX(); // Opposite end
																// of fourth
																// edge
		Edge e5 = n4.getEdges().get(0).equals(e4) ? n4.getEdges().get(1) : n4.getEdges().get(0);
		Node n5 = e5.getX().equals(n4) ? e5.getY() : e5.getX(); // Opposite end
																// of fifth edge
		Edge e6 = n5.getEdges().get(0).equals(e5) ? n5.getEdges().get(1) : n5.getEdges().get(0);
		Node n6 = e6.getX().equals(n5) ? e6.getY() : e6.getX(); // Opposite end
																// of sixth edge

		// Make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p, n);
		p.spendResources(Settlement.getSettlementCost(), game.getBank());

		// Need a settlement so that this player can be stolen from
		p2.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p2, n6);
		p2.spendResources(Settlement.getSettlementCost(), game.getBank());
		game.setCurrentPlayer(p.getColour());

		// Player 1 plays three knights
		for (Hex h : n6.getHexes())
		{
			Requests.Request.Builder req = Requests.Request.newBuilder();

			// Set up knight card request, grant the card, play the card
			req.setPlayDevCard(Board.PlayableDevCard.KNIGHT);
			p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
			p.buyDevelopmentCard(DevelopmentCardType.Knight, game.getBank());

			// Reset recent dev card for player, so they can play this turn
			game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

			assertEquals(0, server.getExpectedMoves(p.getColour()).size());
			server.addMessageToProcess(
					new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
			server.processMessage();
			assertEquals(1, server.getExpectedMoves(p.getColour()).size());

			// Now set up move robber request
			req.clearPlayDevCard();
			req.setMoveRobber(h.toHexProto().getLocation());
			server.addMessageToProcess(
					new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
			server.processMessage();
			assertEquals(1, server.getExpectedMoves(p.getColour()).size());
			assertTrue(
					server.getExpectedMoves(p.getColour()).get(0).equals(Requests.Request.BodyCase.SUBMITTARGETPLAYER));

			// Set player to take resource from
			req.clearMoveRobber();
			req.setSubmitTargetPlayer(Board.Player.newBuilder().setId(game.getPlayer(p2.getColour()).getId()).build());
			server.addMessageToProcess(
					new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
			server.processMessage();
			assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		}

		// Assert largest army
		assertEquals(3, p.getVp());
		assertEquals(1, p2.getVp());
		game.setCurrentPlayer(p2.getColour());

		// Have player 2 play four knights, so largest army is revoked.
		while (p2.getArmySize() < 4)
		{
			Hex h = n.getHexes().get(0);
			Requests.Request.Builder req = Requests.Request.newBuilder();

			// Set up knight card request, grant the card, play the card
			req.setPlayDevCard(Board.PlayableDevCard.KNIGHT);
			p2.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
			p2.buyDevelopmentCard(DevelopmentCardType.Knight, game.getBank());

			// Reset recent dev card for player, so they can play this turn
			game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

			assertEquals(0, server.getExpectedMoves(p2.getColour()).size());
			server.addMessageToProcess(
					new ReceivedMessage(p2.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
			server.processMessage();
			assertEquals(1, server.getExpectedMoves(p2.getColour()).size());

			// Now set up move robber request
			req.clearPlayDevCard();
			req.setMoveRobber(h.toHexProto().getLocation());
			server.addMessageToProcess(
					new ReceivedMessage(p2.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
			server.processMessage();
			assertEquals(1, server.getExpectedMoves(p2.getColour()).size());
			assertTrue(server.getExpectedMoves(p2.getColour()).get(0)
					.equals(Requests.Request.BodyCase.SUBMITTARGETPLAYER));

			// Set player to take resource from
			req.clearMoveRobber();
			req.setSubmitTargetPlayer(Board.Player.newBuilder().setId(game.getPlayer(p.getColour()).getId()).build());
			assertFalse(hasResources(p));
			server.addMessageToProcess(
					new ReceivedMessage(p2.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
			server.processMessage();
			assertEquals(0, server.getExpectedMoves(p2.getColour()).size());
		}

		assertEquals(1, p.getVp());
		assertEquals(3, p2.getVp());
	}

	@Test
	public void playMonopolyTest() throws DoesNotOwnException, CannotAffordException, IOException, BankLimitException
	{
		// Grant Card
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.Monopoly, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Set up resources grant
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		ResourceType r = ResourceType.Brick;
		grant.put(r, 2);

		// Set-up resources to be taken when playing the development card
		Player p2 = new ServerPlayer(Colour.RED, ""), p3 = new ServerPlayer(Colour.ORANGE, "");
		p2.grantResources(grant, game.getBank());
		p2.setId(Board.Player.Id.PLAYER_2);
		p3.grantResources(grant, game.getBank());
		p3.setId(Board.Player.Id.PLAYER_3);
		game.addPlayer(p2);
		game.addPlayer(p3);

		// Set up knight card request, play the card
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.MONOPOLY);
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Monopoly) == 1);
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Monopoly) == 0);

		// Set up choose resource request
		req.clearPlayDevCard().setChooseResource(ResourceType.toProto(r));

		// Play request and assert resources were transferred
		assertEquals(2, p2.getNumResources());
		assertEquals(2, p3.getNumResources());
		assertEquals(0, p.getNumResources());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(0, p2.getNumResources());
		assertEquals(0, p3.getNumResources());
		assertEquals(4, p.getNumResources());
	}

	@Test
	public void playKnightInvalidResourceTest() throws Exception
	{
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Grant
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.Knight, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Set up player 2
		Player p2 = new ServerPlayer(Colour.RED, "");
		p2.setId(Board.Player.Id.PLAYER_2);
		game.addPlayer(p2);
		p2.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p2, hex.getNodes().get(0));
		game.setCurrentPlayer(p.getColour());

		// Set up knight card request, play the card
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.KNIGHT);
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Knight) == 1);
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Knight) == 0);

		// Now set up move robber request
		req.clearPlayDevCard();
		req.setMoveRobber(hex.toHexProto().getLocation());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertTrue(server.getExpectedMoves(p.getColour()).get(0).equals(Requests.Request.BodyCase.SUBMITTARGETPLAYER));

		// Set player to take resource from
		req.clearMoveRobber();
		req.setSubmitTargetPlayer(Board.Player.newBuilder().setId(game.getPlayer(p2.getColour()).getId()).build());
		assertFalse(hasResources(p));
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());

		// Assert that swap happened, but that no resource was taken
		// as p2 didn't have any
		assertTrue(!oldHex.equals(game.getGrid().getHexWithRobber()));
		assertFalse(oldHex.hasRobber());
	}

	@Test
	public void cannotPlayKnightTest() throws Exception
	{
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Set up player 2
		Player p2 = new ServerPlayer(Colour.RED, "");
		p2.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		game.addPlayer(p2);

		// Set up request, don't grant card
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.KNIGHT);
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());

		// Play move and assert robber wasn't moved
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertTrue(oldHex.hasRobber());
		assertFalse(hasResources(p));
		assertTrue(hasResources(p2));
	}

	@Test
	public void cannotPlayCardSameTurnTest() throws Exception
	{
		// Grant
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.RoadBuilding, game.getBank());

		// Set up request
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.ROAD_BUILDING);
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());

		// Try to play move. CannotPlayException will be caught and nothing will
		// happen
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();

		// This value would be two if the card was successfully played
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
	}

	@Test
	public void playBeforeDiceRollTest() throws Exception
	{
		// Grant
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.RoadBuilding, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Set up request, don't grant card
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.ROAD_BUILDING);

		// Add dice roll as expected move
		server.getExpectedMoves(p.getColour()).add(Requests.Request.BodyCase.ROLLDICE);
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());

		// Try to play move. CannotPlayException will be caught and nothing will
		// happen
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();

		// Card was successfully played
		assertEquals(3, server.getExpectedMoves(p.getColour()).size());
	}

	@Test
	public void cannotStealFromSpecifiedPlayerTest() throws Exception
	{
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Grant
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.Knight, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Set up player 2
		Player p2 = new ServerPlayer(Colour.RED, "");
		p2.setId(Board.Player.Id.PLAYER_2);
		p2.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		game.addPlayer(p2);

		// Set up request
		Requests.Request.Builder req = Requests.Request.newBuilder();

		// Set up knight card request, grant the card, play the card
		req.setPlayDevCard(Board.PlayableDevCard.KNIGHT);
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.Knight, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());

		// Now set up move robber request, ensure robber was moved
		req.clearPlayDevCard();
		req.setMoveRobber(hex.toHexProto().getLocation());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertFalse(oldHex.hasRobber());
		assertFalse(oldHex.equals(game.getGrid().getHexWithRobber()));

		// Set player to take colour from
		req.clearMoveRobber();
		req.setSubmitTargetPlayer(Board.Player.newBuilder().setId(game.getPlayer(p2.getColour()).getId()).build());

		// Play move and assert resources weren't stolen, and that a move is not
		// still expected
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		assertFalse(hasResources(p));
		assertTrue(hasResources(p2));

	}

	@Test
	public void playKnightTakeResourceTest() throws Exception
	{
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Grant
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.Knight, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Set up player 2, make settlement, grant resources so one can be taken
		Player p2 = new ServerPlayer(Colour.RED, "");
		p2.setId(Board.Player.Id.PLAYER_2);
		game.addPlayer(p2);
		p2.grantResources(Settlement.getSettlementCost(), game.getBank());
		p2.grantResources(Settlement.getSettlementCost(), game.getBank());
		makeSettlement(p2, hex.getNodes().get(0));
		game.setCurrentPlayer(p.getColour());

		// Set up knight card request, grant the card, play the card
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.KNIGHT);
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());

		// Now set up move robber request
		req.clearPlayDevCard();
		req.setMoveRobber(hex.toHexProto().getLocation());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertTrue(server.getExpectedMoves(p.getColour()).get(0).equals(Requests.Request.BodyCase.SUBMITTARGETPLAYER));

		// Set player to take resource from
		req.clearMoveRobber();
		req.setSubmitTargetPlayer(Board.Player.newBuilder().setId(game.getPlayer(p2.getColour()).getId()).build());
		assertFalse(hasResources(p));
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());

		assertFalse(oldHex.equals(game.getGrid().getHexWithRobber()));
		assertTrue(game.getGrid().getHexWithRobber().hasRobber());
		assertFalse(oldHex.hasRobber());
		assertTrue(hasResources(p));
	}

	@Test
	public void playYearOfPlentyTest() throws Exception
	{
		// Grant
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.YearOfPlenty, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Set up YOP card request, play the card
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.YEAR_OF_PLENTY);
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(2, server.getExpectedMoves(p.getColour()).size());

		// Now set up choose resource request, and request it
		req.clearPlayDevCard();
		req.setChooseResource(Resource.Kind.BRICK);
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertTrue(1 == p.getResources().get(ResourceType.Brick));

		// Choose same resource again
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		assertTrue(2 == p.getResources().get(ResourceType.Brick));
	}

	@Test
	public void playBuildRoadsCardTest() throws Exception
	{
		// Set up entities
		Edge e1 = n.getEdges().get(0), e2 = n.getEdges().get(1);
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		p.buyDevelopmentCard(DevelopmentCardType.RoadBuilding, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		p.grantResources(Road.getRoadCost(), game.getBank());
		p.grantResources(Road.getRoadCost(), game.getBank());
		makeSettlement(p, n);

		// Set up Road building card request, play the card
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.ROAD_BUILDING);
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(2, server.getExpectedMoves(p.getColour()).size());

		// Now request to build a road
		req.clearPlayDevCard();
		req.setBuildRoad(e1.toEdgeProto());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertEquals(1, p.getRoads().size());

		// Build road again
		req.setBuildRoad(e2.toEdgeProto());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		assertEquals(2, p.getRoads().size());

		// Assert roads were built on appropriate edges
		assertTrue(p.getRoads().size() == 2);
		assertTrue(p.getRoads().get(0).getEdge().equals(e1));
		assertTrue(p.getRoads().get(1).getEdge().equals(e2));
	}

	/**
	 * Tests atomicity and end-to-end processing of a multi-part move
	 * 
	 * @throws CannotBuildRoadException
	 * @throws CannotAffordException
	 */
	@Test
	public void playBuildRoadsCardFailure() throws Exception
	{
		// Set up variables
		Edge e1 = n.getEdges().get(0);
		int oldResources = 0;

		// Set up development card
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());
		DevelopmentCardType card = DevelopmentCardType.RoadBuilding;
		p.buyDevelopmentCard(card, game.getBank());

		// Reset recent dev card for player, so they can play this turn
		game.getPlayer(game.getCurrentPlayer()).clearRecentDevCards();

		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost(), game.getBank());
		p.grantResources(Road.getRoadCost(), game.getBank());
		p.grantResources(Road.getRoadCost(), game.getBank());
		makeSettlement(p, n);

		// Set up Road building card request, play the card
		Requests.Request.Builder req = Requests.Request.newBuilder();
		req.setPlayDevCard(Board.PlayableDevCard.ROAD_BUILDING);
		assertEquals(0, server.getExpectedMoves(p.getColour()).size());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(2, server.getExpectedMoves(p.getColour()).size());

		// Now request to build a road
		req.clearPlayDevCard();
		req.setBuildRoad(e1.toEdgeProto());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertEquals(1, p.getRoads().size());
		assertTrue(p.getRoads().get(0).getEdge().equals(e1));

		// ATTEMPT to build road in same location
		req.setBuildRoad(e1.toEdgeProto());
		server.addMessageToProcess(
				new ReceivedMessage(p.getColour(), Messages.Message.newBuilder().setRequest(req).build()));
		server.processMessage();

		// Assert that server is STILL expecting a new road request, & only one
		// road was built
		assertEquals(1, server.getExpectedMoves(p.getColour()).size());
		assertTrue(server.getExpectedMoves(p.getColour()).get(0).equals(Requests.Request.BodyCase.BUILDROAD));
		assertEquals(1, p.getRoads().size());
	}

	@Test
	public void playLibraryTest() throws DoesNotOwnException, CannotAffordException, BankLimitException
	{
		// Grant Card
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());

		// Play move and assert vp has increased
		assertFalse(p.getDevelopmentCards().containsKey(DevelopmentCardType.Library));
		assertEquals(0, p.getVp());
		p.buyDevelopmentCard(DevelopmentCardType.Library, game.getBank());
		assertEquals(0, p.getVp());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.Library) == 1);
	}

	@Test
	public void playUniversityTest() throws DoesNotOwnException, CannotAffordException, BankLimitException
	{
		// Grant Card
		p.grantResources(DevelopmentCardType.getCardCost(), game.getBank());

		// Play move and assert vp has increased
		assertFalse(p.getDevelopmentCards().containsKey(DevelopmentCardType.University));
		assertEquals(0, p.getVp());
		p.buyDevelopmentCard(DevelopmentCardType.University, game.getBank());
		assertEquals(0, p.getVp());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.University) == 1);
	}
}