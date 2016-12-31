package test.java.catan;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import main.java.board.Edge;
import main.java.board.Hex;
import main.java.board.Node;
import main.java.enums.*;
import main.java.exceptions.*;
import main.java.game.build.DevelopmentCard;
import main.java.game.build.Road;
import main.java.game.build.Settlement;
import main.java.game.moves.*;
import main.java.game.players.*;

import org.junit.*;

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
		Player p2 = new NetworkPlayer(Colour.Red);
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		ResourceType r = ResourceType.Brick;
		
		// Give resources to new player and add them to game
		grant.put(r, 2);
		game.addPlayer(p2);		
		p2.grantResources(grant);

		// Set up move
		PlayMonopolyCardMove move = new PlayMonopolyCardMove();
		move.setPlayerColour(p.getColour());
		move.setResource(r);
		
		// Play move and assert resources were transferred
		assertEquals(2, p2.getNumResources());
		assertEquals(0, p.getNumResources());
		
		game.playMonopolyCard(move);
		assertEquals(0, p2.getNumResources());
		assertEquals(2, p.getNumResources());
	}

	@Test
	public void playKnightNoResourcesTest() throws CannotAffordException, IllegalPlacementException, CannotStealException
	{	
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.Red);
		game.addPlayer(p2);
		p2.grantResources(Settlement.getSettlementCost());
		p2.buildSettlement(hex.getNodes().get(0));
		assertFalse(hasResources(p2));
		
		// Set up move
		MoveRobberMove move = new MoveRobberMove();
		move.setPlayerColour(p.getColour());
		move.setColourToTakeFrom(p2.getColour());
		move.setX(hex.getX());
		move.setY(hex.getY());
				
		// Assert that swap happened, but that no resource was taken
		// as p2 didn't have any
		String response = game.moveRobber(move);
		assertEquals("ok", response);
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
		Player p2 = new NetworkPlayer(Colour.Red);
		p2.grantResources(DevelopmentCard.getCardCost());
		game.addPlayer(p2);
		
		// Set up move
		MoveRobberMove move = new MoveRobberMove();
		move.setPlayerColour(p.getColour());
		move.setColourToTakeFrom(p2.getColour());
		move.setX(hex.getX());
		move.setY(hex.getY());
		
		// Play move and assert robber was moved
		assertTrue(oldHex.hasRobber());
		assertFalse(hasResources(p));
		assertTrue(hasResources(p2));
		
		// Robber will move, but cannot take resource because this player does not have a settlement
		// on one of the hex's nodes
		try
		{
			game.moveRobber(move);
		}
		catch (CannotStealException e)
		{
			// Ensure robber wasn't moved
			assertTrue(oldHex.equals(game.getGrid().getHexWithRobber()));
			assertTrue(game.getGrid().getHexWithRobber().hasRobber());
		}
	}
	
	@Test
	public void playKnightTakeResourceTest() throws CannotAffordException, IllegalPlacementException, CannotStealException
	{	
		Hex oldHex = game.getGrid().getHexWithRobber();

		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.Red);
		p2.grantResources(DevelopmentCard.getCardCost());
		game.addPlayer(p2);
		p2.grantResources(Settlement.getSettlementCost());
		p2.buildSettlement(hex.getNodes().get(0));
		
		// Set up move
		MoveRobberMove move = new MoveRobberMove();
		move.setPlayerColour(p.getColour());
		move.setColourToTakeFrom(p2.getColour());
		move.setX(hex.getX());
		move.setY(hex.getY());
		
		// Play move and assert robber was moved
		assertTrue(oldHex.hasRobber());
		assertFalse(hasResources(p));
		assertTrue(hasResources(p2));
		
		String response = game.moveRobber(move);
		assertEquals("ok", response);
		assertTrue(!oldHex.equals(game.getGrid().getHexWithRobber()));
		assertTrue(game.getGrid().getHexWithRobber().hasRobber());
		assertFalse(oldHex.hasRobber());
		assertTrue(hasResources(p));
		assertTrue(p2.getNumResources() < DevelopmentCard.getCardCost().size());
	}
	
	@Test
	public void playYearOfPlentyTest()
	{
		// Set up move
		PlayYearOfPlentyCardMove move = new PlayYearOfPlentyCardMove();
		move.setPlayerColour(p.getColour());
		move.setResource1(ResourceType.Brick);
		move.setResource2(ResourceType.Ore);
		
		// Play move and assert resources were transferred
		assertEquals(0, p.getNumResources());	
		game.playYearOfPlentyCard(move);
		assertEquals(2, p.getNumResources());
	}
	
	@Test
	public void playBuildRoadsCardTest() throws CannotBuildRoadException, RoadExistsException, CannotAffordException
	{
		// Set up entities
		Edge e1 = n.getEdges().get(0), e2 = n.getEdges().get(1);
		Node n1 = e1.getX(), n2 = e1.getY(), n3 = e2.getX(), n4 = e2.getY();
		
		// Set up moves
		PlayRoadBuildingCardMove move = new PlayRoadBuildingCardMove();
		BuildRoadMove move1 = new BuildRoadMove(), move2 = new BuildRoadMove();
		move1.setX1(n1.getX());
		move1.setY1(n1.getY());
		move1.setX2(n2.getX());
		move1.setY2(n2.getY());
		move1.setPlayerColour(p.getColour());
		move2.setX1(n3.getX());
		move2.setY1(n3.getY());
		move2.setX2(n4.getX());
		move2.setY2(n4.getY());
		move2.setPlayerColour(p.getColour());
		move.setMove1(move1);
		move.setMove2(move2);
		
		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(n);
		
		// Grant resources and Build roads
		p.grantResources(Road.getRoadCost());
		p.grantResources(Road.getRoadCost());
		game.playBuildRoadsCard(move);
		
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
	public void playBuildRoadsCardFailure() throws CannotBuildRoadException, CannotAffordException
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
		PlayRoadBuildingCardMove internalMove = new PlayRoadBuildingCardMove();
		BuildRoadMove move1 = new BuildRoadMove(), move2 = new BuildRoadMove();
		move1.setX1(n1.getX());
		move1.setY1(n1.getY());
		move1.setX2(n2.getX());
		move1.setY2(n2.getY());
		move1.setPlayerColour(p.getColour());
		move2.setX1(n1.getX());
		move2.setY1(n1.getY());
		move2.setX2(n2.getX());
		move2.setY2(n2.getY());
		move2.setPlayerColour(p.getColour());
		internalMove.setMove1(move1);
		internalMove.setMove2(move2);
		
		// Set up wrapper move
		PlayDevelopmentCardMove devCardMove = new PlayDevelopmentCardMove();
		devCardMove.setPlayerColour(p.getColour());
		devCardMove.setCard(card);
		
		// Grant resources and make settlement
		p.grantResources(Settlement.getSettlementCost());
		makeSettlement(n);
		
		// Grant resources and Build roads
		p.grantResources(Road.getRoadCost());
		p.grantResources(Road.getRoadCost());
		oldResources = p.getNumResources();
		game.processDevelopmentCard(devCardMove, internalMove); // FAILS
	
		// Ensure player wasn't updated, and that the dev card was not spent
		assertEquals(0, p.getRoads().size());
		assertEquals(oldResources, p.getNumResources());
		assertTrue(p.getDevelopmentCards().get(DevelopmentCardType.RoadBuilding).size() == 1);
	
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
