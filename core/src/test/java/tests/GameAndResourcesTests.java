package tests;

import static org.junit.Assert.*;
import java.util.*;
import enums.*;
import exceptions.IllegalTradeException;
import exceptions.SettlementExistsException;
import game.build.*;
import game.players.NetworkPlayer;
import game.players.Player;

import org.junit.*;
import protocol.ResourceProtos.*;
import protocol.TradeProtos.*;

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
		Player p2 = new NetworkPlayer(Colour.RED);
		game.addPlayer(p2);		

		// Set up playerTrade move and offer and request
		ResourceCount.Builder resource = ResourceCount.newBuilder();
		PlayerTradeProto.Builder playerTrade = PlayerTradeProto.newBuilder();

		// Set offer and request
		resource.setBrick(1);
		playerTrade.setOffer(resource.build());
		resource.setWool(1);
		playerTrade.setRequest(resource);

		playerTrade.setOfferer(Colour.toProto(p.getColour()));
		playerTrade.setRecipient(Colour.toProto(p2.getColour()));

		// Neither player has resources, so this will fail.
		// Exception thrown and caught in processMove
		game.processPlayerTrade(playerTrade.build(), p.getColour());
		
		// assert failed
		assertTrue(p.getNumResources() == 0 && p2.getNumResources() == 0);
	}
	
	
	@Test
	public void emptyTradeTest() throws IllegalTradeException
	{
		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(ResourceType.Brick, 1);
		p2.grantResources(grant);
		game.addPlayer(p2);


		// Set up playerTrade move and offer and request
		ResourceCount.Builder resource = ResourceCount.newBuilder();
		PlayerTradeProto.Builder playerTrade = PlayerTradeProto.newBuilder();

		// Set up empty offer and request
		playerTrade.setOffer(resource.build());
		resource.setBrick(1);
		playerTrade.setRequest(resource.build());

		playerTrade.setOfferer(Colour.toProto(p.getColour()));
		playerTrade.setRecipient(Colour.toProto(p2.getColour()));

		assertTrue(p.getNumResources() == 0 && p2.getNumResources() == 1);
		
		// Neither player has resources, so this will fail.
		// Exception thrown and caught in processMove
		game.processPlayerTrade(playerTrade.build(), p.getColour());
		
		// assert success
		assertTrue(p.getNumResources() == 1 && p2.getNumResources() == 0);
	}
}
