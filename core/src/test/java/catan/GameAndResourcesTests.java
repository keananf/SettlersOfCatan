package catan;

import static org.junit.Assert.*;
import java.util.*;
import comm.messages.ResourceCount;
import comm.messages.TradeMessage;
import enums.*;
import exceptions.IllegalTradeException;
import exceptions.SettlementExistsException;
import game.build.*;
import game.players.NetworkPlayer;
import game.players.Player;

import org.junit.*;

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
	
	@Test
	public void loseResourcesTest() // If you have over 7 resources, and a 7 is rolled
	{		
		// Grant the player over 7 resources
		p.grantResources(Settlement.getSettlementCost());
		p.grantResources(Settlement.getSettlementCost());
		assertTrue(p.getNumResources() >= 7);

		// A '7' is rolled, so this player must lose it's excess resources
		game.allocateResources(7);
		assertTrue(p.getNumResources() == 7);
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
	
	@Test
	public void illegalTradeTest() throws IllegalTradeException
	{
		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.Red);				
		game.addPlayer(p2);		

		// Set up trade move and offer and request
		ResourceCount offer = new ResourceCount(), request = new ResourceCount();
		TradeMessage msg = new TradeMessage();
		offer.setBrick(1);
		request.setWool(1);
		msg.setOffer(offer);
		msg.setRequest(request);
		msg.setPlayerColour(p.getColour());
		msg.setRecipient(p2.getColour());
		msg.setStatus(TradeStatus.Accepted);
		
		// Neither player has resources, so this will fail.
		// Exception thrown and caught in processMove
		String response = game.processMove(msg, MoveType.TradeMove);
		
		// assert failed
		assertNotEquals("ok", response);
		assertTrue(p.getNumResources() == 0 && p2.getNumResources() == 0);
	}
	
	
	@Test
	public void emptyTradeTest() throws IllegalTradeException
	{
		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.Red);		
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(ResourceType.Brick, 1);
		p2.grantResources(grant);
		game.addPlayer(p2);	

		// Set up trade move and request
		ResourceCount request = new ResourceCount();
		TradeMessage msg = new TradeMessage();
		request.setBrick(1);
		msg.setOffer(new ResourceCount()); // Empty offer
		msg.setRequest(request);
		msg.setPlayerColour(p.getColour());
		msg.setRecipient(p2.getColour());
		msg.setStatus(TradeStatus.Accepted);

		assertTrue(p.getNumResources() == 0 && p2.getNumResources() == 1);
		
		// Neither player has resources, so this will fail.
		// Exception thrown and caught in processMove
		String response = game.processMove(msg, MoveType.TradeMove);
		
		// assert success
		assertEquals("ok", response);
		assertTrue(p.getNumResources() == 1 && p2.getNumResources() == 0);
	}
}
