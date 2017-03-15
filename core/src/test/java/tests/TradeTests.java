package tests;

import enums.Colour;
import enums.ResourceType;
import exceptions.*;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.NetworkPlayer;
import game.players.Player;
import grid.Port;
import intergroup.Messages;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.resource.Resource;
import intergroup.trade.Trade;
import org.junit.Before;
import org.junit.Test;
import server.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TradeTests extends TestHelper
{
    private Server server;

    @Before
    public void setUp()
    {
        reset();
        server = new Server();
        server.setGame(game);
    }
    @Test
    public void illegalTradeTest()
    {
        // Set up player 2
        Player p2 = new NetworkPlayer(Colour.RED, "");
        p2.setId(Board.Player.Id.PLAYER_2);
        game.addPlayer(p2);

        // Set up playerTrade move and offer and request
        Resource.Counts.Builder resource = Resource.Counts.newBuilder();
        Trade.WithPlayer.Builder playerTrade = Trade.WithPlayer.newBuilder();

        // Set offer and request
        resource.setBrick(1);
        playerTrade.setOffering(resource.build());
        resource.clearBrick().setWool(1);
        playerTrade.setWanting(resource);

        playerTrade.setOther(Board.Player.newBuilder().setIdValue(game.getPlayer(Colour.RED).getId().getNumber()).build());

        // Neither player has resources, so this will fail.
        // Exception thrown and caught in processMove
        assertTrue(p.getNumResources() == 0 && p2.getNumResources() == 0);
        game.processPlayerTrade(playerTrade.build());

        // assert failed
        assertTrue(p.getNumResources() == 0 && p2.getNumResources() == 0);
    }

    @Test
    public void playerTradeTest() throws IllegalTradeException
    {
        // Set up player 2
        Player p2 = new NetworkPlayer(Colour.RED, "");
        p2.setId(Board.Player.Id.PLAYER_2);
        game.addPlayer(p2);

        // set up resources
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(ResourceType.Brick, 1);
        p2.grantResources(grant);
        grant.put(ResourceType.Brick, 0);
        grant.put(ResourceType.Grain, 1);
        p.grantResources(grant);

        // Set up playerTrade move and offer and request
        Resource.Counts.Builder resource = Resource.Counts.newBuilder();
        Trade.WithPlayer.Builder playerTrade = Trade.WithPlayer.newBuilder();

        // Set offer and request
        resource.setGrain(1);
        playerTrade.setOffering(resource.build());
        resource.clearGrain().setBrick(1);
        playerTrade.setWanting(resource);
        playerTrade.setOther(Board.Player.newBuilder().setIdValue(game.getPlayer(Colour.RED).getId().getNumber()).build());

        // assert resources are set up
        assertTrue(1 == p.getResources().get(ResourceType.Grain) && 1 == p.getNumResources());
        assertTrue(0 == p.getResources().get(ResourceType.Brick));
        assertTrue(1 == p2.getResources().get(ResourceType.Brick) && 1 == p2.getNumResources());
        assertTrue(0 == p2.getResources().get(ResourceType.Grain));

        // Neither player has resources, so this will fail.
        // Exception thrown and caught in processMove
        game.processPlayerTrade(playerTrade.build());

        // assert resources are swapped
        assertTrue(1 == p.getResources().get(ResourceType.Brick) && 1 == p.getNumResources());
        assertTrue(0 == p.getResources().get(ResourceType.Grain));
        assertTrue(1 == p2.getResources().get(ResourceType.Grain) && 1 == p2.getNumResources());
        assertTrue(0 == p2.getResources().get(ResourceType.Brick));

    }

    @Test(expected = CannotAffordException.class)
    public void cannotAffordBankTradeTest() throws IllegalTradeException, IllegalPortTradeException,
            CannotAffordException, IllegalBankTradeException
    {
        Trade.WithBank.Builder trade = setUpBankTrade(new HashMap<ResourceType, Integer>(), new HashMap<ResourceType, Integer>());
        game.determineTradeType(trade.build());
}

    @Test(expected = IllegalBankTradeException.class)
    public void invalidPortTradeRequestTest() throws IllegalTradeException, IllegalPortTradeException,
            CannotAffordException, IllegalBankTradeException, SettlementExistsException, CannotBuildRoadException, RoadExistsException
    {
        Port port = game.getGrid().ports.get(0);

        // Build settlement and road on port
        p.grantResources(Settlement.getSettlementCost());
        makeSettlement(p, port.getX());
        p.grantResources(Road.getRoadCost());
        buildRoad(port);

        // Set up trade
        ResourceType receiveType = ResourceType.Brick;
        ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
        Map<ResourceType, Integer> req = new HashMap<ResourceType, Integer>();
        req.put(receiveType, Port.RETURN_AMOUNT);

        // Give player resources in prep for trade
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(exchangeType, Port.EXCHANGE_AMOUNT);
        p.grantResources(grant);
        Trade.WithBank.Builder portTrade = setUpBankTrade(grant, req);

        // Mess up port trade so error is thrown. Request another resource as well.
        Resource.Counts.Builder request = portTrade.getWanting().toBuilder();
        request.setWool(1);
        portTrade.setWanting(request.build());

        // assert resources are set up
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(Port.EXCHANGE_AMOUNT));

        game.determineTradeType(portTrade.build());
    }

    @Test
    public void portTradeTest() throws IllegalTradeException, IllegalPortTradeException, CannotAffordException,
            IllegalBankTradeException, SettlementExistsException, CannotBuildRoadException, RoadExistsException
    {
        Port port = game.getGrid().ports.get(0);

        // Build settlement and road on port
        p.grantResources(Settlement.getSettlementCost());
        makeSettlement(p, port.getX());
        p.grantResources(Road.getRoadCost());
        buildRoad(port);

        // Set up trade
        ResourceType receiveType = ResourceType.Brick;
        ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
        Map<ResourceType, Integer> req = new HashMap<ResourceType, Integer>();
        req.put(receiveType, Port.RETURN_AMOUNT);

        // Give player resources in prep for trade
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(exchangeType, Port.EXCHANGE_AMOUNT);
        p.grantResources(grant);
        Trade.WithBank.Builder portTrade = setUpBankTrade(grant, req);

        // assert resources are set up
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(Port.EXCHANGE_AMOUNT));

        game.determineTradeType(portTrade.build());

        // assert resources are swapped
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(0));
        assertEquals(new Integer(p.getResources().get(receiveType)), new Integer(Port.RETURN_AMOUNT));
    }

    @Test(expected = IllegalBankTradeException.class)
    public void portIllegalDoubleTradeTest() throws IllegalTradeException, IllegalPortTradeException,
            CannotAffordException, SettlementExistsException, CannotBuildRoadException, RoadExistsException, IllegalBankTradeException
    {
        Port port = game.getGrid().ports.get(0);

        // Build settlement and road on port
        p.grantResources(Settlement.getSettlementCost());
        makeSettlement(p, port.getX());
        p.grantResources(Road.getRoadCost());
        buildRoad(port);

        // Set up trade
        ResourceType receiveType = ResourceType.Brick;
        ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
        Map<ResourceType, Integer> req = new HashMap<ResourceType, Integer>();
        req.put(receiveType, Port.RETURN_AMOUNT * 2 + 1);

        // Give player resources in prep for trade
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(exchangeType, Port.EXCHANGE_AMOUNT * 2);
        p.grantResources(grant);
        Trade.WithBank.Builder portTrade = setUpBankTrade(grant, req);

        // Mess up port trade so error is thrown. Request another resource as well.
        Resource.Counts.Builder request = portTrade.getWanting().toBuilder();
        request.setWool(1);
        portTrade.setWanting(request.build());

        // assert resources are set up
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(Port.EXCHANGE_AMOUNT * 2));

        game.determineTradeType(portTrade.build());
    }

    @Test
    public void portDoubleTradeTest() throws IllegalTradeException, IllegalPortTradeException, CannotAffordException,
            IllegalBankTradeException, CannotBuildRoadException, RoadExistsException, SettlementExistsException
    {
        Port port = game.getGrid().ports.get(0);

        // Build settlement and road on port
        p.grantResources(Settlement.getSettlementCost());
        makeSettlement(p, port.getX());
        p.grantResources(Road.getRoadCost());
        buildRoad(port);

        // Set up trade
        ResourceType receiveType = ResourceType.Brick;
        ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
        Map<ResourceType, Integer> req = new HashMap<ResourceType, Integer>();
        req.put(receiveType, Port.RETURN_AMOUNT * 2);

        // Give player resources in prep for trade
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(exchangeType, Port.EXCHANGE_AMOUNT * 2);
        p.grantResources(grant);
        Trade.WithBank.Builder portTrade = setUpBankTrade(grant, req);

        // assert resources are set up
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(Port.EXCHANGE_AMOUNT * 2));

        game.determineTradeType(portTrade.build());

        // assert resources are swapped
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(0));
        assertEquals(new Integer(p.getResources().get(receiveType)), new Integer(Port.RETURN_AMOUNT * 2));
    }

    @Test
    public void emptyTradeTest() throws IllegalTradeException
    {
        // Set up player 2
        Player p2 = new NetworkPlayer(Colour.RED, "");
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(ResourceType.Brick, 1);
        p2.grantResources(grant);
        p2.setId(Board.Player.Id.PLAYER_2);
        game.addPlayer(p2);

        // Set up playerTrade move and offer and request
        Resource.Counts.Builder resource = Resource.Counts.newBuilder();
        Trade.WithPlayer.Builder playerTrade = Trade.WithPlayer.newBuilder();

        // Set up request and empty offer
        playerTrade.setOffering(resource.build());
        resource.setBrick(1);
        playerTrade.setWanting(resource.build());
        playerTrade.setOther(Board.Player.newBuilder().setIdValue(game.getPlayer(Colour.RED).getId().getNumber()).build());

        // Neither player has resources, so this will fail.
        // Exception thrown and caught in processMove
        assertTrue(p.getNumResources() == 0 && p2.getNumResources() == 1);
        game.processPlayerTrade(playerTrade.build());

        // assert success
        assertTrue(p.getNumResources() == 1 && p2.getNumResources() == 0);
    }

    @Test(expected = IllegalBankTradeException.class)
    public void bankTradeIllegalAmounts() throws CannotAffordException, IllegalBankTradeException, IllegalPortTradeException
    {
        // Set up offers and requests
        Map<ResourceType, Integer> offer = new HashMap<ResourceType, Integer>();
        Map<ResourceType, Integer> request = new HashMap<ResourceType, Integer>();
        offer.put(ResourceType.Grain, 3); // Doesn't meet threshold
        request.put(ResourceType.Ore, 1);

        // Grant player the offer so that the trade and complete
        // Assert resources are set up correctly
        p.grantResources(offer);
        assertTrue(p.getNumResources() == 3);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(3));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(0));

        // Set up and perform bank trade
        Trade.WithBank.Builder bankTrade = setUpBankTrade(offer, request);
        game.determineTradeType(bankTrade.build());
    }

    @Test(expected = IllegalBankTradeException.class)
    public void bankTradeIllegalAmounts2() throws CannotAffordException, IllegalBankTradeException, IllegalPortTradeException
    {
        // Set up offers and requests
        Map<ResourceType, Integer> offer = new HashMap<ResourceType, Integer>();
        Map<ResourceType, Integer> request = new HashMap<ResourceType, Integer>();
        offer.put(ResourceType.Grain, 5); // Not evenly dividable by 4, therefore invalid trade amount
        request.put(ResourceType.Ore, 1);

        // Grant player the offer so that the trade and complete
        // Assert resources are set up correctly
        p.grantResources(offer);
        assertTrue(p.getNumResources() == 5);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(5));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(0));

        // Set up and perform bank trade
        Trade.WithBank.Builder bankTrade = setUpBankTrade(offer, request);
        game.determineTradeType(bankTrade.build());
    }

    @Test
    public void bankTradeDoubleTrade() throws CannotAffordException, IllegalBankTradeException, IllegalPortTradeException
    {
        // Set up offers and requests
        Map<ResourceType, Integer> offer = new HashMap<ResourceType, Integer>();
        Map<ResourceType, Integer> request = new HashMap<ResourceType, Integer>();
        offer.put(ResourceType.Grain, 8); // Evenly dividable by 4, therefore should receive 2
        request.put(ResourceType.Ore, 2);

        // Grant player the offer so that the trade and complete
        // Assert resources are set up correctly
        p.grantResources(offer);
        assertTrue(p.getNumResources() == 8);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(8));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(0));

        // Set up and perform bank trade
        Trade.WithBank.Builder bankTrade = setUpBankTrade(offer, request);
        game.determineTradeType(bankTrade.build());

        // assert swap
        assertTrue(p.getNumResources() == 2);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(0));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(2));
    }

    @Test(expected = IllegalBankTradeException.class)
    public void bankTradeDoubleTradeInvalidRequest() throws CannotAffordException, IllegalBankTradeException, IllegalPortTradeException
    {
        // Set up offers and requests
        Map<ResourceType, Integer> offer = new HashMap<ResourceType, Integer>();
        Map<ResourceType, Integer> request = new HashMap<ResourceType, Integer>();
        offer.put(ResourceType.Grain, 8); // Evenly dividable by 4, therefore should receive 2
        request.put(ResourceType.Ore, 1);

        // Grant player the offer so that the trade and complete
        // Assert resources are set up correctly
        p.grantResources(offer);
        assertTrue(p.getNumResources() == 8);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(8));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(0));

        // Set up and perform bank trade
        Trade.WithBank.Builder bankTrade = setUpBankTrade(offer, request);
        game.determineTradeType(bankTrade.build());
    }

    @Test
    public void bankTradeTest() throws CannotAffordException, IllegalBankTradeException, IllegalPortTradeException
    {
        // Set up offers and requests
        Map<ResourceType, Integer> offer = new HashMap<ResourceType, Integer>();
        Map<ResourceType, Integer> request = new HashMap<ResourceType, Integer>();
        offer.put(ResourceType.Grain, 4);
        request.put(ResourceType.Ore, 1);

        // Grant player the offer so that the trade and complete
        // Assert resources are set up correctly
        p.grantResources(offer);
        assertTrue(p.getNumResources() == 4);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(4));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(0));

        // Set up and perform bank trade
        Trade.WithBank.Builder bankTrade = setUpBankTrade(offer, request);
        game.determineTradeType(bankTrade.build());

        // assert swap
        assertTrue(p.getNumResources() == 1);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(0));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(1));
    }

    @Test
    public void tradePhaseTest() throws SettlementExistsException, IOException
    {
        // Set up player 2
        Player p2 = new NetworkPlayer(Colour.RED, "");
        p2.setId(Board.Player.Id.PLAYER_2);
        game.addPlayer(p2);

        // Set up playerTrade move and offer and request
        Resource.Counts.Builder resource = Resource.Counts.newBuilder();
        Trade.WithPlayer.Builder playerTrade = Trade.WithPlayer.newBuilder();

        // Set offer and request
        resource.setGrain(1);
        playerTrade.setOffering(resource.build());
        resource.clearGrain().setBrick(1);
        playerTrade.setWanting(resource);
        playerTrade.setOther(Board.Player.newBuilder().setIdValue(game.getPlayer(Colour.RED).getId().getNumber()).build());

        // allocate resources so trade will succeed
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(ResourceType.Brick, 1);
        p2.grantResources(grant);
        grant.put(ResourceType.Brick, 0);
        grant.put(ResourceType.Grain, 1);
        p.grantResources(grant);

        // Build settlement. IN BUILD PHASE
        p.grantResources(Settlement.getSettlementCost());
        makeSettlement(p, n);

        // Assert resources set up
        assertTrue(1 == p.getResources().get(ResourceType.Grain) && 1 == p.getNumResources());
        assertTrue(0 == p.getResources().get(ResourceType.Brick));
        assertTrue(1 == p2.getResources().get(ResourceType.Brick) && 1 == p2.getNumResources());
        assertTrue(0 == p2.getResources().get(ResourceType.Grain));

        // INITIATE TRADE PHASE
        assertFalse(server.isTradePhase());
        server.processMessage(Messages.Message.newBuilder().setRequest(
                Requests.Request.newBuilder().setInitiateTrade(
                        Trade.Kind.newBuilder().setPlayer(playerTrade.build()).build()).build()).build(), p.getColour());

        // Assert it is the trade phase, and that player red has an expected move
        assertTrue(server.isTradePhase());
        assertTrue(server.getExpectedMoves(Colour.RED).get(0).equals(Requests.Request.BodyCase.SUBMITTRADERESPONSE));

        // Player red accepts. Assert trade occurred and that it is still trade phase
        server.processMessage(Messages.Message.newBuilder().setRequest(
                Requests.Request.newBuilder().setSubmitTradeResponseValue(Trade.Response.ACCEPT.getNumber()).build()).build(), p2.getColour());
        assertTrue(1 == p.getResources().get(ResourceType.Brick) && 1 == p.getNumResources());
        assertTrue(0 == p.getResources().get(ResourceType.Grain));
        assertTrue(1 == p2.getResources().get(ResourceType.Grain) && 1 == p2.getNumResources());
        assertTrue(0 == p2.getResources().get(ResourceType.Brick));
        assertTrue(server.isTradePhase());
        assertTrue(server.getExpectedMoves(Colour.RED).size() == 0);

        // Try to upgrade settlement. WONT WORK BECAUSE NOW IN TRADE PHASE
        int old = p.getNumResources();
        p.grantResources(City.getCityCost());
        assertTrue(p.getNumResources() > old);
        server.processMessage(Messages.Message.newBuilder().setRequest(
                Requests.Request.newBuilder().setBuildCity(n.toProto()).build()).build(), p.getColour());

        assertTrue(p.getNumResources() > old);
        assertTrue(p.getSettlements().size() == 1);
    }

    /////HELPER METHODS/////
    private Trade.WithBank.Builder setUpBankTrade(Map<ResourceType, Integer> offer, Map<ResourceType, Integer> request)
    {
        // Set up bankTrade move and offer and request
        Resource.Counts.Builder offerProto = toResourceCount(offer), requestProto = toResourceCount(request);
        Trade.WithBank.Builder bankTrade = Trade.WithBank.newBuilder();

        bankTrade.setOffering(offerProto.build());
        bankTrade.setWanting(requestProto.build());

        return bankTrade;

    }

    private Resource.Counts.Builder toResourceCount(Map<ResourceType, Integer> map)
    {
        int brick = map.containsKey(ResourceType.Brick) ? map.get(ResourceType.Brick) : 0;
        int lumber = map.containsKey(ResourceType.Lumber) ? map.get(ResourceType.Lumber) :
                map.containsKey(ResourceType.Generic) ? map.get(ResourceType.Generic) : 0;
        int wool = map.containsKey(ResourceType.Wool) ? map.get(ResourceType.Wool) : 0;
        int ore = map.containsKey(ResourceType.Ore) ? map.get(ResourceType.Ore) : 0;
        int grain = map.containsKey(ResourceType.Grain) ? map.get(ResourceType.Grain) : 0;

        Resource.Counts.Builder proto = Resource.Counts.newBuilder();
        proto.setBrick(brick);
        proto.setLumber(lumber);
        proto.setWool(wool);
        proto.setOre(ore);
        proto.setGrain(grain);

        return proto;
    }
}