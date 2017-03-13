package server;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.Game;
import game.build.Road;
import game.players.NetworkPlayer;
import game.players.Player;
import grid.Hex;
import grid.Node;
import grid.Port;
import intergroup.EmptyOuterClass;
import intergroup.Events;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

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

	/**
	 * Determines whether or not the given trade type is for a port or bank
	 * @param trade
	 * @return the trade if nothing went wrong
	 */
	public Trade.WithBank determineTradeType(Trade.WithBank trade)
			throws IllegalBankTradeException, CannotAffordException, IllegalPortTradeException
	{
		ResourceType offerType = null, requestType = null;

		// Extract the trade's contents
		Player current = getPlayer(currentPlayer);
		Map<ResourceType, Integer> request = processResources(trade.getWanting());
		Map<ResourceType, Integer> offer = processResources(trade.getOffering());

		// Check that the player can afford the offer
		if(!current.canAfford(offer) || offer.size() < 1)
		{
			throw new CannotAffordException(current.getResources(), offer);
		}

		// Must only be requesting one type of resource and giving one type of resource
		if(offer.size() > 1 || request.size() != 1)
		{
			throw new IllegalBankTradeException(current.getColour());
		}

		// Retrieve resources
		for(ResourceType r : ResourceType.values())
		{
			if(request.containsKey(r)) requestType = r;
			if(offer.containsKey(r)) offerType = r;
		}

		// Check all roads this player owns
		for(Road r: current.getRoads())
		{
			Port p = (Port) r.getEdge();
			// If this road is on a port and the resource types match up
			if(r.getEdge() instanceof Port &&
					(p.getExchangeType().equals(offerType) || p.getExchangeType().equals(ResourceType.Generic)) &&
					offer.get(offerType) / request.get(requestType) == Port.EXCHANGE_AMOUNT)
				return processPortTrade(trade, (Port)r.getEdge(), requestType, offerType);
		}

		// Otherwise assume it is with the bank
		return processBankTrade(trade, requestType, offerType);
	}

	/**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @param requestType the request type
	 * @param offerType  the offer type
	 * @return the response status
	 */
	private Trade.WithBank processBankTrade(Trade.WithBank trade, ResourceType requestType, ResourceType offerType)
			throws IllegalBankTradeException, CannotAffordException
	{
		int exchangeAmount = 4;

		// Extract the trade's contents
		Player current = getPlayer(currentPlayer);
		Map<ResourceType, Integer> request = processResources(trade.getWanting());
		Map<ResourceType, Integer> offer = processResources(trade.getOffering());

		// If request doesn't match what the offer should give
		if(offer.get(offerType) % exchangeAmount != 0 || offer.get(offerType) / request.get(requestType) != exchangeAmount)
		{
			throw new IllegalBankTradeException(current.getColour());
		}

		// Perform swap and return
		current.spendResources(offer);
		current.grantResources(request);
		return trade;
	}

	/**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @param port the port that is being traded with
	 * @param requestType the request type
	 * @param offerType  the offer type
	 * @return the response status
	 */
	private Trade.WithBank processPortTrade(Trade.WithBank trade, Port port, ResourceType requestType, ResourceType offerType)
			throws IllegalPortTradeException, CannotAffordException
	{
		int exchangeAmount = 3;

		// Extract the trade's contents
		Player current = getPlayer(currentPlayer);
		Map<ResourceType, Integer> request = processResources(trade.getWanting());
		Map<ResourceType, Integer> offer = processResources(trade.getOffering());

		// If request doesn't match what the offer should give
		if(offer.get(offerType) % exchangeAmount != 0 || offer.get(offerType) / request.get(requestType) != exchangeAmount)
		{
			throw new IllegalPortTradeException(current.getColour(), port);
		}

		// Exchange resources
		port.exchange(current, offer, request);

		return trade;
	}

	/**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @return the response status
	 */
	public Trade.WithPlayer processPlayerTrade(Trade.WithPlayer trade)
	{
        // Find the recipient and extract the trade's contents
		Resource.Counts offer = trade.getOffering();
		Resource.Counts request = trade.getWanting();
		Colour recipientColour = getPlayer(trade.getOther().getId()).getColour();
		NetworkPlayer recipient = (NetworkPlayer) players.get(recipientColour), recipientCopy = (NetworkPlayer) recipient.copy();
		Player offerer = players.get(currentPlayer);

		try
		{
			// Exchange resources
			offerer.spendResources(offer);
			recipient.grantResources(offer);
			
			recipient.spendResources(request);
			offerer.grantResources(request);
		}
		catch(CannotAffordException e)
		{
			// Reset recipient and throw exception. Offerer is reset in above method
			recipient.restoreCopy(recipientCopy);
		}

        return trade;
	}

	/**
	 * Processes the discard request to ensure that it is valid
	 * @param discardRequest the resources the player is wishing to discard
	 * @param col the colour of the player who sent the discard request
	 */
	public void processDiscard(Resource.Counts discardRequest, Colour col)
			throws CannotAffordException, InvalidDiscardRequest
	{
		Player current = players.get(col);
		int oldAmount = current.getNumResources();

		// If the player can afford the request, then spend the resources
		current.spendResources(processResources(discardRequest));

		// Invalid request.
		if(current.getNumResources() > 7)
		{
			// Give resources back, and throw exception
			current.grantResources(discardRequest);
			throw new InvalidDiscardRequest(oldAmount, current.getNumResources());
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

		// Perform any additional actions not accomplished through
		// updating expected moves (i.e. road building, year of plenty)
		switch(type)
		{
			// Update army if necessary
			case Knight:
				p.addKnightPlayed();
				checkLargestArmy();
				break;

			default:
				break;
		}
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

		if(other.getNumResources() == 0)
			return;

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

	/**
	 * Process the playing of the 'University' development card.
	 */
	public void playUniversityCard() throws DoesNotOwnException
	{
		((NetworkPlayer)players.get(currentPlayer)).playDevelopmentCard(DevelopmentCardType.University);
        grantVpPoint();
	}

	/**
	 * Process the playing of the 'Library' development card.
	 */
	public void playLibraryCard() throws DoesNotOwnException
	{
		((NetworkPlayer)players.get(currentPlayer)).playDevelopmentCard(DevelopmentCardType.Library);
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
		NetworkPlayer p = (NetworkPlayer) players.get(currentPlayer);
		Board.Point p1 = edge.getA(), p2 = edge.getB();
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
		p.buildRoad(grid.getEdge(p1, p2));
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
	 * @return the updated list of usernames
	 */
	public Lobby.Usernames joinGame(Lobby.Join joinLobby) throws GameFullException
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

		// Add all users to update message
		Lobby.Usernames.Builder users = Lobby.Usernames.newBuilder();
		for(Player player : players.values())
		{
			users.addUsername(player.getUsername());
		}

		return users.build();
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
