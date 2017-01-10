package tests;

import static org.junit.Assert.*;
import java.util.*;

import board.Port;
import enums.*;
import exceptions.CannotAffordException;
import exceptions.IllegalPortTradeException;
import exceptions.IllegalTradeException;
import exceptions.SettlementExistsException;
import game.build.*;
import game.players.NetworkPlayer;
import game.players.Player;

import org.junit.*;
import protocol.RequestProtos;
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
	public void illegalTradeTest()
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
	public void playerTradeTest() throws IllegalTradeException
	{
		// Set up player 2
		Player p2 = new NetworkPlayer(Colour.RED);
		game.addPlayer(p2);

		// set up resources
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(ResourceType.Brick, 1);
		p2.grantResources(grant);
		grant.put(ResourceType.Brick, 0);
		grant.put(ResourceType.Grain, 1);
		p.grantResources(grant);


		// Set up playerTrade move and offer and request
		ResourceCount.Builder offer = ResourceCount.newBuilder(), request = ResourceCount.newBuilder();
		PlayerTradeProto.Builder playerTrade = PlayerTradeProto.newBuilder();

		// Set up empty offer and request
		offer.setGrain(1);
		playerTrade.setOffer(offer.build());
		request.setBrick(1);
		playerTrade.setRequest(request.build());

		playerTrade.setOfferer(Colour.toProto(p.getColour()));
		playerTrade.setRecipient(Colour.toProto(p2.getColour()));

		// assert resources are set up
		assertTrue(1 == p.getResources().get(ResourceType.Grain) && 1 == p.getNumResources());
		assertTrue(0 == p.getResources().get(ResourceType.Brick));
		assertTrue(1 == p2.getResources().get(ResourceType.Brick) && 1 == p2.getNumResources());
		assertTrue(0 == p2.getResources().get(ResourceType.Grain));

		// Neither player has resources, so this will fail.
		// Exception thrown and caught in processMove
		game.processPlayerTrade(playerTrade.build(), p.getColour());

		// assert resources are swapped
		assertTrue(1 == p.getResources().get(ResourceType.Brick) && 1 == p.getNumResources());
		assertTrue(0 == p.getResources().get(ResourceType.Grain));
		assertTrue(1 == p2.getResources().get(ResourceType.Grain) && 1 == p2.getNumResources());
		assertTrue(0 == p2.getResources().get(ResourceType.Brick));

	}

	@Test(expected = CannotAffordException.class)
	public void cannotAffordPortTradeTest() throws IllegalTradeException, IllegalPortTradeException, CannotAffordException
	{
		Port port = game.getGrid().ports.get(0);
		PortTradeProto.Builder portTrade = setUpPortTrade(port);

		// assert resources are NOT set up
		ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
		assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(0));

		game.processPortTrade(portTrade.build());
	}

	@Test(expected = IllegalPortTradeException.class)
	public void invalidPortTradeRequestTest() throws IllegalTradeException, IllegalPortTradeException, CannotAffordException
	{
		Port port = game.getGrid().ports.get(0);
		ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(exchangeType, port.getExchangeAmount());
		p.grantResources(grant);
		PortTradeProto.Builder portTrade = setUpPortTrade(port);

		// Mess up port trade so error is thrown
		ResourceCount.Builder request = portTrade.getRequestResources().toBuilder();
		if(port.getReturnType().equals(ResourceType.Lumber))
		{
			request.setBrick(1);
		}
		else request.setLumber(1);
		portTrade.setRequestResources(request.build());

		// assert resources are set up
		assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(port.getExchangeAmount()));

		game.processPortTrade(portTrade.build());
	}

	@Test
	public void portTradeTest() throws IllegalTradeException, IllegalPortTradeException, CannotAffordException
	{
		Port port = game.getGrid().ports.get(0);
		ResourceType receiveType = port.getReturnType() == ResourceType.Generic ? ResourceType.Brick : port.getReturnType();
		ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(exchangeType, port.getExchangeAmount());
		p.grantResources(grant);
		PortTradeProto.Builder portTrade = setUpPortTrade(port);

		// assert resources are set up
		assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(port.getExchangeAmount()));

		game.processPortTrade(portTrade.build());

		// assert resources are swapped
		assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(0));
		assertEquals(new Integer(p.getResources().get(receiveType)), new Integer(port.getReturnAmount()));
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

	/////HELPER METHODS/////

	private PortTradeProto.Builder setUpPortTrade(Port port)
	{
		// Set up playerTrade move and offer and request
		ResourceCount.Builder offer = ResourceCount.newBuilder(), request = ResourceCount.newBuilder();
		PortTradeProto.Builder portTrade = PortTradeProto.newBuilder();

		// Set up empty offer and request
		switch(port.getExchangeType())
		{
			case Wool:
				offer.setWool(port.getExchangeAmount());
				break;
			case Ore:
				offer.setOre(port.getExchangeAmount());
				break;
			case Grain:
				offer.setGrain(port.getExchangeAmount());
				break;
			case Brick:
				offer.setBrick(port.getExchangeAmount());
				break;
			case Generic:
			case Lumber:
				offer.setLumber(port.getExchangeAmount());
				break;
		}
		switch(port.getReturnType())
		{
			case Wool:
				request.setWool(1);
				break;
			case Ore:
				request.setOre(1);
				break;
			case Grain:
				request.setGrain(1);
				break;
			case Generic:
			case Brick:
				request.setBrick(1);
				break;
			case Lumber:
				request.setLumber(1);
				break;
		}

		portTrade.setOfferResources(offer.build());
		portTrade.setPlayer(Colour.toProto(p.getColour()));
		portTrade.setRequestResources(request.build());
		portTrade.setPort(port.toPortProto());

		return portTrade;
	}

}

