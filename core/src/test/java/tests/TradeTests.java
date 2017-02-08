package tests;

public class TradeTests extends TestHelper
{
    /*@Before
    public void setUp()
    {
        reset();
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
        playerTrade.addRecipients(Colour.toProto(p2.getColour()));

        // Neither player has resources, so this will fail.
        // Exception thrown and caught in processMove
        game.processPlayerTrade(playerTrade.build(), p.getColour(), p2.getColour());

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
        playerTrade.addRecipients(Colour.toProto(p2.getColour()));

        // assert resources are set up
        assertTrue(1 == p.getResources().get(ResourceType.Grain) && 1 == p.getNumResources());
        assertTrue(0 == p.getResources().get(ResourceType.Brick));
        assertTrue(1 == p2.getResources().get(ResourceType.Brick) && 1 == p2.getNumResources());
        assertTrue(0 == p2.getResources().get(ResourceType.Grain));

        // Neither player has resources, so this will fail.
        // Exception thrown and caught in processMove
        game.processPlayerTrade(playerTrade.build(), p.getColour(), p2.getColour());

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
        PortTradeProto.Builder portTrade = setUpPortTrade(port, new HashMap<ResourceType, Integer>(), new HashMap<ResourceType, Integer>());

        // assert resources are NOT set up
        ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(0));

        game.processPortTrade(portTrade.build());
    }

    @Test(expected = IllegalPortTradeException.class)
    public void invalidPortTradeRequestTest() throws IllegalTradeException, IllegalPortTradeException, CannotAffordException
    {
        Port port = game.getGrid().ports.get(0);
        ResourceType receiveType = port.getReturnType() == ResourceType.Generic ? ResourceType.Brick : port.getReturnType();
        ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
        Map<ResourceType, Integer> req = new HashMap<ResourceType, Integer>();
        req.put(receiveType, port.getReturnAmount());
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(exchangeType, port.getExchangeAmount());
        p.grantResources(grant);
        PortTradeProto.Builder portTrade = setUpPortTrade(port, grant, req);

        // Mess up port trade so error is thrown. Request another resource as well.
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
        Map<ResourceType, Integer> req = new HashMap<ResourceType, Integer>();
        req.put(receiveType, port.getReturnAmount());
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(exchangeType, port.getExchangeAmount());
        p.grantResources(grant);
        PortTradeProto.Builder portTrade = setUpPortTrade(port, grant, req);


        // assert resources are set up
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(port.getExchangeAmount()));

        game.processPortTrade(portTrade.build());

        // assert resources are swapped
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(0));
        assertEquals(new Integer(p.getResources().get(receiveType)), new Integer(port.getReturnAmount()));
    }

    @Test(expected = IllegalPortTradeException.class)
    public void portIllegalDoubleTradeTest() throws IllegalTradeException, IllegalPortTradeException, CannotAffordException
    {
        Port port = game.getGrid().ports.get(0);
        ResourceType receiveType = port.getReturnType() == ResourceType.Generic ? ResourceType.Brick : port.getReturnType();
        ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
        Map<ResourceType, Integer> req = new HashMap<ResourceType, Integer>();
        req.put(receiveType, port.getReturnAmount() * 2 + 1);
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(exchangeType, port.getExchangeAmount() * 2);
        p.grantResources(grant);
        PortTradeProto.Builder portTrade = setUpPortTrade(port, grant, req);


        // assert resources are set up
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(port.getExchangeAmount() * 2));

        // Asking for too many resources
        game.processPortTrade(portTrade.build());
    }

    @Test
    public void portDoubleTradeTest() throws IllegalTradeException, IllegalPortTradeException, CannotAffordException
    {
        Port port = game.getGrid().ports.get(0);
        ResourceType receiveType = port.getReturnType() == ResourceType.Generic ? ResourceType.Brick : port.getReturnType();
        ResourceType exchangeType = port.getExchangeType() == ResourceType.Generic ? ResourceType.Lumber : port.getExchangeType();
        Map<ResourceType, Integer> req = new HashMap<ResourceType, Integer>();
        req.put(receiveType, port.getReturnAmount() * 2);
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
        grant.put(exchangeType, port.getExchangeAmount() * 2);
        p.grantResources(grant);
        PortTradeProto.Builder portTrade = setUpPortTrade(port, grant, req);


        // assert resources are set up
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(port.getExchangeAmount() * 2));

        game.processPortTrade(portTrade.build());

        // assert resources are swapped
        assertEquals(new Integer(p.getResources().get(exchangeType)), new Integer(0));
        assertEquals(new Integer(p.getResources().get(receiveType)), new Integer(port.getReturnAmount() * 2));
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
        playerTrade.addRecipients(Colour.toProto(p2.getColour()));

        assertTrue(p.getNumResources() == 0 && p2.getNumResources() == 1);

        // Neither player has resources, so this will fail.
        // Exception thrown and caught in processMove
        game.processPlayerTrade(playerTrade.build(), p.getColour(), p2.getColour());

        // assert success
        assertTrue(p.getNumResources() == 1 && p2.getNumResources() == 0);
    }

    @Test(expected = IllegalBankTradeException.class)
    public void bankTradeIllegalAmounts() throws CannotAffordException, IllegalBankTradeException
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
        BankTradeProto.Builder bankTrade = setUpBankTrade(offer, request);
        game.processBankTrade(bankTrade.build());
    }

    @Test(expected = IllegalBankTradeException.class)
    public void bankTradeIllegalAmounts2() throws CannotAffordException, IllegalBankTradeException
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
        BankTradeProto.Builder bankTrade = setUpBankTrade(offer, request);
        game.processBankTrade(bankTrade.build());
    }

    @Test
    public void bankTradeDoubleTrade() throws CannotAffordException, IllegalBankTradeException
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
        BankTradeProto.Builder bankTrade = setUpBankTrade(offer, request);
        game.processBankTrade(bankTrade.build());

        // assert swap
        assertTrue(p.getNumResources() == 2);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(0));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(2));
    }

    @Test(expected = IllegalBankTradeException.class)
    public void bankTradeDoubleTradeInvalidRequest() throws CannotAffordException, IllegalBankTradeException
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
        BankTradeProto.Builder bankTrade = setUpBankTrade(offer, request);
        game.processBankTrade(bankTrade.build());
    }

    @Test
    public void bankTradeTest() throws CannotAffordException, IllegalBankTradeException
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
        BankTradeProto.Builder bankTrade = setUpBankTrade(offer, request);
        game.processBankTrade(bankTrade.build());

        // assert swap
        assertTrue(p.getNumResources() == 1);
        assertEquals(new Integer(p.getResources().get(ResourceType.Grain)), new Integer(0));
        assertEquals(new Integer(p.getResources().get(ResourceType.Ore)), new Integer(1));
    }


    /////HELPER METHODS/////

    private PortTradeProto.Builder setUpPortTrade(Port port, Map<ResourceType, Integer> grant, Map<ResourceType, Integer> requestMap)
    {
        // Set up portTrade move and offer and request
        ResourceCount.Builder offer = ResourceCount.newBuilder(), request = ResourceCount.newBuilder();
        PortTradeProto.Builder portTrade = PortTradeProto.newBuilder();

        // Set up empty offer and request
        switch(port.getExchangeType())
        {
            case Wool:
                offer.setWool(grant.containsKey(ResourceType.Wool) ? grant.get(ResourceType.Wool) : port.getReturnAmount());
                break;
            case Ore:
                offer.setOre(grant.containsKey(ResourceType.Ore) ? grant.get(ResourceType.Ore) : port.getReturnAmount());
                break;
            case Grain:
                offer.setGrain(grant.containsKey(ResourceType.Grain) ? grant.get(ResourceType.Grain) : port.getReturnAmount());
                break;
            case Brick:
                offer.setBrick(grant.containsKey(ResourceType.Brick) ? grant.get(ResourceType.Brick) : port.getReturnAmount());
                break;
            case Generic:
            case Lumber:
                offer.setLumber(grant.containsKey(ResourceType.Lumber) ? grant.get(ResourceType.Lumber) : port.getReturnAmount());
                break;
        }
        switch(port.getReturnType())
        {
            case Wool:
                request.setWool(requestMap.containsKey(ResourceType.Wool) ? requestMap.get(ResourceType.Wool) : port.getReturnAmount());
                break;
            case Ore:
                request.setOre(requestMap.containsKey(ResourceType.Ore) ? requestMap.get(ResourceType.Ore) : port.getReturnAmount());
                break;
            case Grain:
                request.setGrain(requestMap.containsKey(ResourceType.Grain) ? requestMap.get(ResourceType.Grain) : port.getReturnAmount());
                break;
            case Generic:
            case Brick:
                request.setBrick(requestMap.containsKey(ResourceType.Brick) ? requestMap.get(ResourceType.Brick) : port.getReturnAmount());
                break;
            case Lumber:
                request.setLumber(requestMap.containsKey(ResourceType.Lumber) ? requestMap.get(ResourceType.Lumber) : port.getReturnAmount());
                break;
        }

        portTrade.setOfferResources(offer.build());
        portTrade.setPlayer(Colour.toProto(p.getColour()));
        portTrade.setRequestResources(request.build());
        portTrade.setPort(port.toPortProto());

        return portTrade;
    }


    private BankTradeProto.Builder setUpBankTrade(Map<ResourceType, Integer> offer, Map<ResourceType, Integer> request)
    {
        // Set up bankTrade move and offer and request
        ResourceCount.Builder offerProto = toResourceCount(offer), requestProto = toResourceCount(request);
        BankTradeProto.Builder bankTrade = BankTradeProto.newBuilder();

        bankTrade.setOfferResources(offerProto.build());
        bankTrade.setPlayer(Colour.toProto(p.getColour()));
        bankTrade.setRequestResources(requestProto.build());

        return bankTrade;

    }

    private ResourceCount.Builder toResourceCount(Map<ResourceType, Integer> map)
    {
        int brick = map.containsKey(ResourceType.Brick) ? map.get(ResourceType.Brick) : 0;
        int lumber = map.containsKey(ResourceType.Lumber) ? map.get(ResourceType.Lumber) : 0;
        int wool = map.containsKey(ResourceType.Wool) ? map.get(ResourceType.Wool) : 0;
        int ore = map.containsKey(ResourceType.Ore) ? map.get(ResourceType.Ore) : 0;
        int grain = map.containsKey(ResourceType.Grain) ? map.get(ResourceType.Grain) : 0;

        ResourceCount.Builder proto = ResourceCount.newBuilder();
        proto.setBrick(brick);
        proto.setLumber(lumber);
        proto.setWool(wool);
        proto.setOre(ore);
        proto.setGrain(grain);

        return proto;
    }*/
}
