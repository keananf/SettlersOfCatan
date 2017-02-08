package server;

import board.Board;
import catan.EmptyOuterClass;
import catan.Events;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.Game;
import game.players.NetworkPlayer;
import game.players.Player;
import grid.Hex;
import grid.Node;
import grid.Port;
import lobby.Lobby;
import resource.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ServerGame extends Game
{
	private Random dice;
	private int current; // index of current player
	protected int numPlayers;
	public static final int NUM_PLAYERS = 4;

	public ServerGame()
	{
		super();
		dice = new Random();
	}

	/**
	 * Chooses first player.
	 */
	public void chooseFirstPlayer()
	{
		int dice = this.dice.nextInt(NUM_PLAYERS);
		
		current = dice;
		setCurrentPlayer(getPlayersAsList()[dice].getColour());
	}


	/**
	 * Assigns resources to each player based upon their settlements and the dice
	 * @param dice the dice roll
	 * @return
	 */
	public Map<Colour, Map<ResourceType, Integer>> allocateResources(int dice)
	{
		Map<Colour, Map<ResourceType, Integer>> playerResources = new HashMap<Colour, Map<ResourceType, Integer>>();

		// for each player
		for(Player player : players.values())
		{
			Map<ResourceType, Integer> grant = getNewResources(dice, player.getColour());

			if(dice != 7)
				player.grantResources(grant);

			playerResources.put(player.getColour(), grant);
		}

		return playerResources;
	}
/*
	*//**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @return the response status
	 *//*
	public TradeStatusProto processBankTrade(BankTradeProto trade) throws IllegalBankTradeException, CannotAffordException
	{
		int exchangeAmount = 4, offerSum = 0, reqSum = 0;

		// Extract the trade's contents
		Player recipient = players.get(Colour.fromProto(trade.getPlayer()));
		Map<ResourceType, Integer> request = processResources(trade.getRequestResources());
		Map<ResourceType, Integer> offer = processResources(trade.getOfferResources());

		// Check that the player can afford the offer
		if(!recipient.canAfford(offer))
		{
			throw new CannotAffordException(recipient.getResources(), offer);
		}

		// Check that requested trade is allowed
		for(ResourceType r : ResourceType.values())
		{
			// sum up total quantities of offer and request
			offerSum += offer.containsKey(r) ? offer.get(r) : 0;
			reqSum += request.containsKey(r) ? request.get(r) : 0;

			// If too little or too many resources for a trade on this port
			if(offer.containsKey(r) && offer.get(r) % exchangeAmount != 0)
				throw new IllegalBankTradeException(recipient.getColour());
		}

		// If request doesn't match what the offer should give
		if(offerSum / reqSum != exchangeAmount)
		{
			throw new IllegalBankTradeException(recipient.getColour());
		}

		// Perform swap and return
		recipient.spendResources(offer);
		recipient.grantResources(request);
		return TradeStatusProto.ACCEPT;
	}

	*//**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @return the response status
	 *//*
	public TradeStatusProto processPortTrade(PortTradeProto trade) throws IllegalPortTradeException, CannotAffordException
	{
		// Find the port and extract the trade's contents
		Player recipient = players.get(Colour.fromProto(trade.getPlayer()));
		Map<ResourceType, Integer> request = processResources(trade.getRequestResources());
		Map<ResourceType, Integer> offer = processResources(trade.getOfferResources());
		Port port = grid.getPort(trade.getPort());
		int offerSum = 0, reqSum = 0;
		int exchangeAmount = port.getExchangeAmount();

		// Check that the player can afford the offer
		if(!recipient.canAfford(offer))
		{
			throw new CannotAffordException(recipient.getResources(), offer);
		}

		// Check that requested trade is allowed
		for(ResourceType r : ResourceType.values())
		{
			// sum up total quantities of offer and request
			offerSum += offer.containsKey(r) ? offer.get(r) : 0;
			reqSum += request.containsKey(r) ? request.get(r) : 0;

			// If too little or too many resources for a trade on this port
			if(offer.containsKey(r) && offer.get(r) % exchangeAmount != 0)
				throw new IllegalPortTradeException(recipient.getColour(), port);
		}

		// If request doesn't match what the offer should give
		if(offerSum / reqSum != exchangeAmount)
		{
			throw new IllegalPortTradeException(recipient.getColour(), port);
		}

		// Exchange resources
		port.exchange(recipient, offer, request);

		return TradeStatusProto.ACCEPT;
	}

	*//**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @param offererColour the offerer's colour
	 * @param recipientColour the recipient's colour
	 * @return the response status
	 *//*
	public SuccessFailResponse processPlayerTrade(PlayerTradeProto trade, Colour offererColour, Colour recipientColour)
	{
        SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();

        // Find the recipient and extract the trade's contents
		ResourceCount offer = trade.getOffer();
		ResourceCount request = trade.getRequest();
		NetworkPlayer recipient = (NetworkPlayer) players.get(recipientColour), recipientCopy = (NetworkPlayer) recipient.copy();
		Player offerer = players.get(offererColour);

		try
		{
			// Exchange resources
			offerer.spendResources(offer);
			recipient.grantResources(offer);
			
			recipient.spendResources(request);
			offerer.grantResources(request);
        	resp.setResult(ResultProto.SUCCESS);
		}
		catch(CannotAffordException e)
		{
			// Reset recipient and throw exception. Offerer is reset in above method
			recipient.restoreCopy(recipientCopy, null);

			resp.setResult(ResultProto.FAILURE);
		}

        return resp.build();
	}*/

	/**
	 * Processes the discard request to ensure that it is valid
	 * @param discardRequest the resources the player is wishing to discard
	 * @param col the colour of the player who sent the discard request
	 */
	public void processDiscard(Resource.Counts discardRequest, Colour col)
			throws CannotAffordException, InvalidDiscardRequest
	{
		Player current = players.get(col);
		int oldAmount = current.getNumResources(), newAmount = 0;

		// If the player can afford the request, then spend the resources
		current.spendResources(processResources(discardRequest));

		// Invalid request.
		if((newAmount = current.getNumResources()) > 7)
		{
			// Give resources back, and throw exception
			current.grantResources(discardRequest);
			throw new InvalidDiscardRequest(oldAmount, newAmount);
		}
	}

	/**
	 * Checks that the player can build a city at the desired location, and builds it.
	 * @param city the point to build the city
	 * @throws CannotUpgradeException 
	 * @throws CannotAffordException 
	 */
	public void upgradeSettlement(Board.Point city)
			throws CannotAffordException, CannotUpgradeException, InvalidCoordinatesException
	{
		Player p = players.get(currentPlayer);
		Node node = grid.getNode(city.getX(), city.getY());

		// Invalid request coordinates.
		if(node == null)
		{
			throw new InvalidCoordinatesException(city.getX(), city.getY());
		}

		// Try to upgrade settlement
		((NetworkPlayer) p).upgradeSettlement(node);
    }
	
	/**
	 * Checks that the player can build a settlement at the desired location, and builds it.
	 * @param request the request
	 * @throws IllegalPlacementException 
	 * @throws CannotAffordException 
	 * @throws SettlementExistsException 
	 */
	public void buildSettlement(Board.Point request)
			throws CannotAffordException, IllegalPlacementException, SettlementExistsException, InvalidCoordinatesException
	{
		Player p = players.get(currentPlayer);
        Node node = grid.getNode(request.getX(), request.getY());

		// Invalid request coordinates.
		if(node == null)
		{
			throw new InvalidCoordinatesException(request.getX(), request.getY());
		}

		// Try to build settlement
		((NetworkPlayer) p).buildSettlement(node);
		
		checkIfRoadBroken(node);
	}

	/**
	 * Checks that the player can buy a development card
	 * @param card the card of development card to play
	 */
	public void playDevelopmentCard(Board.PlayableDevCard card) throws DoesNotOwnException
	{
	    Player p = players.get(currentPlayer);

		// Try to play card
		DevelopmentCardType type = DevelopmentCardType.fromProto(card);
		((NetworkPlayer)p).playDevelopmentCard(type);
	}
	
	/**
	 * Checks that the player can buy a development card
	 * @return the bought card
	 * @throws CannotAffordException 
	 */
	public Board.DevCard buyDevelopmentCard() throws CannotAffordException
	{
        Player p = players.get(currentPlayer);

		// Try to buy card
		// Return the response with the card parameter set
        DevelopmentCardType card = ((NetworkPlayer)p).buyDevelopmentCard(DevelopmentCardType.chooseRandom());
        return DevelopmentCardType.toProto(card);
	}

	/**
	 * Moves the robber and takes a card from the player
	 * who has a settlement on the hex
	 * @param point the point to move the robber to
	 * @throws CannotStealException if the specified player cannot provide a resource 
	 */
	public void moveRobber(Board.Point point) throws InvalidCoordinatesException
	{
		// Retrieve the new hex the robber will move to.
     	Hex newHex = grid.getHex(point.getX(), point.getY());

		// Invalid request coordinates.
		if(newHex == null)
		{
			throw new InvalidCoordinatesException(point.getX(), point.getY());
		}
		
		// Actually perform swap
		grid.swapRobbers(newHex);
	}

	/**
	 * Attempts to take a RANDOM resource from the given player.
	 * @param id the id of the player to take from
	 * @throws CannotStealException
	 */
	public void takeResource(Board.Player.Id id) throws CannotStealException
	{
		Player other = getPlayer(id);
		ResourceType r = ResourceType.Generic;

		// Randomly choose resource that the player has
		while(r == ResourceType.Generic || other.getResources().get(r) == 0)
		{
			r = ResourceType.random();
		}

		takeResource(id, r);
	}

	/**
	 * Attempts to take a resource from the given player.
	 * @param id the id of the player to take from
	 * @param resource the resource to take
	 * @throws CannotStealException
	 */
	public void takeResource(Board.Player.Id id, ResourceType resource) throws CannotStealException
	{
		boolean valid = false;
		Colour otherColour = getPlayer(id).getColour();

		// Verify this player can take from the specified one
		for(Node n : getGrid().getHexWithRobber().getNodes())
		{
			// If node has a settlement and it is of the specified colour
			if(n.getSettlement() != null && n.getSettlement().getPlayerColour().equals(otherColour))
			{
				NetworkPlayer p = (NetworkPlayer) players.get(currentPlayer);
				p.takeResource(players.get(otherColour), resource);
				valid = true;
			}
		}

		// Cannot take from this player
		if(!valid) throw new CannotStealException(currentPlayer, otherColour);
	}
/*
	*//**
	 * Process the playing of the 'Build Roads' development card.
	 * @param request the request to process
     * @param playerColour the player's colour
	 * @return return message
	 * @throws RoadExistsException 
	 * @throws CannotBuildRoadException 
	 * @throws CannotAffordException 
	 *//*
	public PlayRoadBuildingCardResponse playBuildRoadsCard(PlayRoadBuildingCardRequest request, Colour playerColour)
			throws CannotAffordException, CannotBuildRoadException,
				RoadExistsException, DoesNotOwnException, InvalidCoordinatesException
	{
		PlayRoadBuildingCardResponse.Builder resp = PlayRoadBuildingCardResponse.newBuilder();

		playDevelopmentCard(DevelopmentCardType.RoadBuilding, currentPlayer);

		// Set responses and return
		resp.setResponse1(buildRoad(request.getRequest1(), playerColour));
		resp.setResponse2(buildRoad(request.getRequest2(), playerColour));
		return resp.build();
	}*/
	
	/**
	 * Process the playing of the 'University' development card.
	 */
	public void playUniversityCard() throws DoesNotOwnException
	{
        grantVpPoint();
	}
	
	/**
	 * Process the playing of the 'Library' development card.
	 */
	public void playLibraryCard() throws DoesNotOwnException
	{
        grantVpPoint();
	}
	
	/**
	 * Choose a new resource.
	 * @param r1 the first resource that was chosen
	 * */
	public void chooseResources(Resource.Kind r1)
	{
		// Set up grant
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(ResourceType.fromProto(r1), 1);
		players.get(currentPlayer).grantResources(grant);
	}
	
	/**
	 * Process the playing of the 'Monopoly' development card.
	 * @param r the resource to take
	 * @return the sum of resources of the given type that were taken
	 */
	public int playMonopolyCard(Resource.Kind r)
	{
        Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		int sum = 0;

		// for each player
		for(Player p : players.values())
		{
			if(p.equals(currentPlayer)) continue;
			
			try
			{
				// Give p's resources of type 'r' to currentPlayer
				int num = p.getResources().get(ResourceType.fromProto(r));
				grant.put(ResourceType.fromProto(r), num);
				p.spendResources(grant);
				sum += num;
			} 
			catch (CannotAffordException e) { /* Will never happen */ }
			players.get(currentPlayer).grantResources(grant);
		}
		
		// Return message is string showing number of resources taken
        return sum;
	}
	
	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param edge the edge to build a road on
	 * @return the response message to the client
	 * @throws RoadExistsException 
	 * @throws CannotBuildRoadException 
	 * @throws CannotAffordException 
	 */
	public Events.Event buildRoad(Board.Edge edge) throws CannotAffordException,CannotBuildRoadException,
			RoadExistsException, InvalidCoordinatesException
	{
		Player p = players.get(currentPlayer);
		board.Board.Point p1 = edge.getA(), p2 = edge.getB();
		Node n = grid.getNode(p1.getX(), p1.getY());
		Node n2 = grid.getNode(p2.getX(), p2.getY());
		Events.Event.Builder ev = Events.Event.newBuilder();

        // Check valid coordinates
        if(n == null)
		{
			throw new InvalidCoordinatesException(p1.getX(), p1.getY());
		}
		if(n2 == null)
		{
			throw new InvalidCoordinatesException(p2.getX(), p2.getY());
		}

		// Try to build the road and update the longest road 
		checkLongestRoad(false);
		
		// return success message
        ev.setRoadBuilt(edge);
        ev.setInstigator(players.get(currentPlayer).getPlayerSettings().getPlayer());
        return ev.build();
	}

	/**
	 * @return a representation of the board that is compatible with protofbufs
	 */
	public Lobby.GameSetup getGameSettings(Colour request)
	{
		Lobby.GameSetup.Builder builder = Lobby.GameSetup.newBuilder();
		int index = 0;

		// Add hexes
		index = 0;
		for(Hex h : getGrid().getHexesAsList())
		{
			builder.addHexesBuilder();
			builder.setHexes(index++, h.toHexProto());
		}

		// Add ports
		index = 0;
		for(Port p : getGrid().getPortsAsList())
		{
			builder.addHarboursBuilder();
			builder.setHarbours(index++, p.toPortProto());
		}

		// Add player settings
		index = 0;
		for(Player p : getPlayersAsList())
		{
			builder.addPlayerSettingsBuilder();
			builder.setPlayerSettings(index++, p.getPlayerSettings());

			// set own player
			if(p.getColour().equals(request))
			{
				builder.setOwnPlayer(p.getPlayerSettings().getPlayer());
			}
		}

		return builder.build();
	}

	/**
	 * Toggles a player's turn
	 * @return 
	 */
	public EmptyOuterClass.Empty changeTurn()
	{
		// Update turn and set event.
		setCurrentPlayer(getPlayersAsList()[++current % NUM_PLAYERS].getColour());
		return EmptyOuterClass.Empty.getDefaultInstance();
	}

	/**
	 * Generates a random roll between 2 and 12
	 */
	public Board.Roll generateDiceRoll()
	{
		Board.Roll.Builder roll = Board.Roll.newBuilder();
		roll.setA(dice.nextInt(6) + 1).setB(dice.nextInt(6) + 1);
		return roll.build();
	}

	/**
	 * Looks to see if any player has won
	 * @return true if a player has won
	 */
	public boolean isOver()
	{
		for(Player p : getPlayersAsList())
		{
			if(p.hasWon()) return true;
		}
		
		return false;
	}
	/**
	 *
	 * @param joinLobby the join lobby request
	 * @return the new colour assigned to the player
	 */
	public Colour joinGame(Lobby.Join joinLobby) throws GameFullException
	{
		// If game is full
		if(numPlayers == NUM_PLAYERS) throw new GameFullException();

		// Assign colour and id
	    Colour newCol = Colour.values()[numPlayers++];
		Board.Player.Id id = Board.Player.Id.forNumber(numPlayers);
		NetworkPlayer p = new NetworkPlayer(newCol, joinLobby.getUsername());
		p.setId(id);

		// Add player info and return assigned colour
		idsToColours.put(id, newCol);
		players.put(p.getColour(), p);
		return p.getColour();
	}

	public Colour joinGame()
	{
		// Assign colour and id
		Colour newCol = Colour.values()[numPlayers++];
		Board.Player.Id id = Board.Player.Id.forNumber(numPlayers);
		NetworkPlayer p = new NetworkPlayer(newCol, "");
		p.setId(id);

		// Add player info and return assigned colour
		idsToColours.put(id, newCol);
		players.put(p.getColour(), p);
		return p.getColour();
	}

	public Map<Colour, Player> getPlayers()
	{
		return players;
	}

	public Player[] getPlayersAsList()
	{
		return players.values().toArray(new Player[]{});
	}

	public void addPlayer(Player p)
	{
		players.put(p.getColour(), p);
	}

	private void grantVpPoint()
	{
		Player currentPlayer = players.get(this.currentPlayer);
		currentPlayer.addVp(currentPlayer.getVp() + 1);
	}

	public void restorePlayerFromCopy(Player copy)
	{
		((NetworkPlayer)players.get(copy.getColour())).restoreCopy(copy);
	}
}
