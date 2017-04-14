package server;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.Game;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.Player;
import game.players.ServerPlayer;
import grid.Edge;
import grid.Hex;
import grid.Node;
import grid.Port;
import intergroup.EmptyOuterClass;
import intergroup.Events;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

import java.util.*;

public class ServerGame extends Game
{
	private Random dice;

	public ServerGame()
	{
		super();
		dice = new Random();
	}

	/**
	 * Assigns resources to each player based upon their settlements and the
	 * dice
	 * 
	 * @param dice the dice roll
	 * @return
	 */
	public Map<Colour, Map<ResourceType, Integer>> allocateResources(int dice)
	{
		Map<Colour, Map<ResourceType, Integer>> playerResources = new HashMap<>();
		List<ResourceType> list = new ArrayList<>();

		if (dice == 7) return playerResources;

		// for each player, figure out what their grant should be
		for (Player player : players.values())
		{
			Map<ResourceType, Integer> grant = getNewResources(dice, player.getColour());
			playerResources.put(player.getColour(), grant);
		}

		// For each resource type, ensure there is enough to go around
		for (ResourceType r : ResourceType.values())
		{
			if (r.equals(ResourceType.Generic)) continue;
			int total = bank.getAvailableResources().get(r);

			// Subtract each player's new amount of 'r'
			for (Colour c : playerResources.keySet())
			{
				total -= playerResources.get(c).getOrDefault(r, 0);
			}

			// Not enough
			if (total < 0) list.add(r);
		}

		// Prevent invalid resources from being distributed
		for (ResourceType r : list)
		{
			for (Colour c : playerResources.keySet())
			{
				playerResources.get(c).remove(r);
			}
		}

		// Now, grant resources
		for (Colour c : playerResources.keySet())
		{
			try
			{
				getPlayer(c).grantResources(playerResources.get(c), bank);
			}
			catch (BankLimitException ignored)
			{}
		}

		return playerResources;
	}

	/**
	 * Determines whether or not the given trade type is for a port or bank
	 * 
	 * @param trade
	 * @return the trade if nothing went wrong
	 */
	public Trade.WithBank determineTradeType(Trade.WithBank trade)
			throws IllegalBankTradeException, CannotAffordException, IllegalPortTradeException, BankLimitException
	{
		ResourceType offerType = null, requestType = null;

		// Extract the trade's contents
		Player current = getPlayer(currentPlayer);
		Map<ResourceType, Integer> request = processResources(trade.getWanting());
		Map<ResourceType, Integer> offer = processResources(trade.getOffering());

		// Check that the player can afford the offer
		if (!current.canAfford(offer)
				|| offer.size() < 1) { throw new CannotAffordException(current.getResources(), offer); }

		// Must only be requesting one type of resource and giving one type of
		// resource
		if (offer.size() > 1 || request.size() != 1) { throw new IllegalBankTradeException(current.getColour()); }

		// Retrieve resources
		for (ResourceType r : ResourceType.values())
		{
			if (request.containsKey(r)) requestType = r;
			if (offer.containsKey(r)) offerType = r;
		}

		// Check all roads this player owns
		for (Road r : current.getRoads())
		{
			Port p = (Port) r.getEdge();
			// If this road is on a port and the resource types match up
			if (r.getEdge() instanceof Port
					&& (p.getExchangeType().equals(offerType) || p.getExchangeType().equals(ResourceType.Generic))
					&& offer.get(offerType) / request.get(requestType) == Port.EXCHANGE_AMOUNT)
				return processPortTrade(trade, (Port) r.getEdge(), requestType, offerType);
		}

		// Otherwise assume it is with the bank
		return processBankTrade(trade, requestType, offerType);
	}

	/**
	 * If trade was successful, exchange of resources occurs here
	 * 
	 * @param trade the trade object detailing the trade
	 * @param requestType the request type
	 * @param offerType the offer type
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
		if (offer.get(offerType) % exchangeAmount != 0 || offer.get(offerType)
				/ request.get(requestType) != exchangeAmount) { throw new IllegalBankTradeException(
						current.getColour()); }

		// Perform swap and return
		try
		{
			current.spendResources(offer, bank);
			current.grantResources(request, bank);
		}
		catch (BankLimitException e)
		{
			e.printStackTrace();
		}
		return trade;
	}

	/**
	 * If trade was successful, exchange of resources occurs here
	 * 
	 * @param trade the trade object detailing the trade
	 * @param port the port that is being traded with
	 * @param requestType the request type
	 * @param offerType the offer type
	 * @return the response status
	 */
	private Trade.WithBank processPortTrade(Trade.WithBank trade, Port port, ResourceType requestType,
			ResourceType offerType) throws IllegalPortTradeException, CannotAffordException, BankLimitException
	{
		int exchangeAmount = 3;

		// Extract the trade's contents
		Player current = getPlayer(currentPlayer);
		Map<ResourceType, Integer> request = processResources(trade.getWanting());
		Map<ResourceType, Integer> offer = processResources(trade.getOffering());

		// If request doesn't match what the offer should give
		if (offer.get(offerType) % exchangeAmount != 0 || offer.get(offerType)
				/ request.get(requestType) != exchangeAmount) { throw new IllegalPortTradeException(current.getColour(),
						port); }

		// Exchange resources
		port.exchange(current, offer, request, bank);

		return trade;
	}

	/**
	 * If trade was successful, exchange of resources occurs here
	 * 
	 * @param trade the trade object detailing the trade
	 * @return the response status
	 */
	public Trade.WithPlayer processPlayerTrade(Trade.WithPlayer trade) throws IllegalTradeException
	{
		// Find the recipient and extract the trade's contents
		Resource.Counts offer = trade.getOffering();
		Resource.Counts request = trade.getWanting();
		Colour recipientColour = getPlayer(trade.getOther().getId()).getColour();
		ServerPlayer recipient = (ServerPlayer) players.get(recipientColour);
		Player offerer = players.get(currentPlayer);

		// Both players need to be able to afford the trade
		if (!offerer.canAfford(processResources(offer)) || !recipient.canAfford(
				processResources(request))) { throw new IllegalTradeException(offerer.getColour(), recipientColour); }

		try
		{
			// Exchange resources
			offerer.spendResources(offer, bank);
			recipient.grantResources(offer, bank);

			recipient.spendResources(request, bank);
			offerer.grantResources(request, bank);
		}
		catch (CannotAffordException | BankLimitException e)
		{
			e.printStackTrace();
		}

		return trade;
	}

	/**
	 * Processes the discard request to ensure that it is valid
	 * 
	 * @param discardRequest the resources the player is wishing to discard
	 * @param col the colour of the player who sent the discard request
	 */
	public void processDiscard(Resource.Counts discardRequest, Colour col)
			throws CannotAffordException, InvalidDiscardRequest
	{
		Player current = players.get(col);
		int oldAmount = current.getNumResources();
		int discardAmount = 0;
		Map<ResourceType, Integer> resources = processResources(discardRequest);

		for (ResourceType r : resources.keySet())
		{
			discardAmount += resources.get(r);
		}

		// Invalid request
		if (oldAmount - discardAmount > ((oldAmount / 2) + 1)) { throw new InvalidDiscardRequest(oldAmount,
				current.getNumResources()); }

		// If the player can afford the request, then spend the resources
		current.spendResources(processResources(discardRequest), bank);
	}

	/**
	 * Checks that the player can build a city at the desired location, and
	 * builds it.
	 * 
	 * @param city the point to build the city
	 * @throws CannotUpgradeException
	 * @throws CannotAffordException
	 */
	public void upgradeSettlement(Board.Point city)
			throws CannotAffordException, CannotUpgradeException, InvalidCoordinatesException, BankLimitException
	{
		Player p = players.get(currentPlayer);
		Node node = grid.getNode(city.getX(), city.getY());

		// Invalid request coordinates.
		if (node == null) { throw new InvalidCoordinatesException(city.getX(), city.getY()); }

		// Cannot upgrade
		if (bank.getAvailableCities() == 0) { throw new BankLimitException("No more cities available"); }

		// Try to upgrade settlement
		((ServerPlayer) p).upgradeSettlement(node, bank);
		bank.setAvailableSettlements(p.getColour(), bank.getAvailableSettlements(p.getColour()) + 1);
		bank.setAvailableCities(p.getColour(), bank.getAvailableCities(p.getColour()) - 1);
	}

	/**
	 * Checks that the player can build a settlement at the desired location,
	 * and builds it.
	 * 
	 * @param request the request
	 * @throws IllegalPlacementException
	 * @throws CannotAffordException
	 * @throws SettlementExistsException
	 */
	public void buildSettlement(Board.Point request) throws CannotAffordException, IllegalPlacementException,
			SettlementExistsException, InvalidCoordinatesException, BankLimitException
	{
		Player p = players.get(currentPlayer);
		Node node = grid.getNode(request.getX(), request.getY());

		// Invalid request coordinates.
		if (node == null) { throw new InvalidCoordinatesException(request.getX(), request.getY()); }

		// Cannot upgrade
		if (bank.getAvailableSettlements() == 0) { throw new BankLimitException("No more settlements available"); }

		// Try to build settlement
		((ServerPlayer) p).buildSettlement(node, bank);
		bank.setAvailableSettlements(p.getColour(), bank.getAvailableSettlements(p.getColour()) - 1);

		checkIfRoadBroken(node);
	}

	/**
	 * Checks that the player can buy a development card
	 * 
	 * @param card the card of development card to play
	 */
	public void playDevelopmentCard(Board.PlayableDevCard card) throws DoesNotOwnException, CannotPlayException
	{
		Player p = players.get(currentPlayer);

		// Try to play card
		DevelopmentCardType type = DevelopmentCardType.fromProto(card);
		((ServerPlayer) p).playDevelopmentCard(type);

		// Perform any additional actions not accomplished through
		// updating expected moves (i.e. road building, year of plenty)
		switch (type)
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
	 * 
	 * @return the bought card
	 * @throws CannotAffordException
	 */
	public Board.DevCard buyDevelopmentCard() throws CannotAffordException, BankLimitException
	{
		Player p = players.get(currentPlayer);

		// Cannot upgrade
		if (bank.getNumAvailableDevCards() == 0) { throw new BankLimitException("No more settlements available"); }

		// Try to buy card
		DevelopmentCardType card = ((ServerPlayer) p).buyDevelopmentCard(bank);
		return DevelopmentCardType.toProto(card);
	}

	/**
	 * Moves the robber and takes a card from the player who has a settlement on
	 * the hex
	 * 
	 * @param point the point to move the robber to
	 * @throws CannotStealException if the specified player cannot provide a
	 *             resource
	 */
	public void moveRobber(Board.Point point) throws InvalidCoordinatesException
	{
		// Retrieve the new hex the robber will move to.
		Hex newHex = grid.getHex(point.getX(), point.getY());

		// Invalid request coordinates.
		if (newHex == null) { throw new InvalidCoordinatesException(point.getX(), point.getY()); }

		// Actually perform swap
		grid.swapRobbers(newHex);
	}

	/**
	 * Attempts to take a RANDOM resource from the given player.
	 * 
	 * @param id the id of the player to take from
	 * @throws CannotStealException
	 */
	public Board.Steal takeResource(Board.Player.Id id)
	{
		Player other = getPlayer(id);
		ResourceType r = ResourceType.Generic;

		if (other.getNumResources() == 0)
			return Board.Steal.newBuilder().setVictim(Board.Player.newBuilder().setId(id).build())
					.setResource(ResourceType.toProto(ResourceType.Generic)).setQuantity(0).build();

		// Randomly choose resource that the player has
		while (r == ResourceType.Generic || other.getResources().get(r) == 0)
		{
			r = ResourceType.random();
		}

		return takeResource(id, r);
	}

	/**
	 * Attempts to take a resource from the given player.
	 * 
	 * @param id the id of the player to take from
	 * @param resource the resource to take
	 * @throws CannotStealException
	 */
	private Board.Steal takeResource(Board.Player.Id id, ResourceType resource)
	{
		Colour otherColour = getPlayer(id).getColour();

		// Verify this player can take from the specified one
		for (Node n : getGrid().getHexWithRobber().getNodes())
		{
			// If node has a settlement and it is of the specified colour
			if (n.getBuilding() != null && n.getBuilding().getPlayerColour().equals(otherColour))
			{
				ServerPlayer p = (ServerPlayer) players.get(currentPlayer);
				p.takeResource(players.get(otherColour), resource, bank);
			}
		}

		return Board.Steal.newBuilder().setVictim(Board.Player.newBuilder().setId(id).build())
				.setResource(ResourceType.toProto(resource)).setQuantity(1).build();
	}

	/**
	 * Choose a new resource.
	 * 
	 * @param r1 the first resource that was chosen
	 */
	public void chooseResources(Resource.Kind r1) throws BankLimitException
	{
		// Set up grant
		Map<ResourceType, Integer> grant = new HashMap<>();
		grant.put(ResourceType.fromProto(r1), 1);
		players.get(currentPlayer).grantResources(grant, bank);
	}

	/**
	 * Process the playing of the 'Monopoly' development card.
	 * 
	 * @param r the resource to take
	 * @return the sum of resources of the given type that were taken
	 */
	public Board.MultiSteal playMonopolyCard(Resource.Kind r)
	{
		Board.MultiSteal.Builder multiSteal = Board.MultiSteal.newBuilder();
		Map<ResourceType, Integer> grant = new HashMap<>();
		ResourceType type = ResourceType.fromProto(r);
		int sum = 0;

		// for each player
		for (Player p : players.values())
		{
			if (p.getColour().equals(currentPlayer)) continue;
			int num = p.getResources().get(type);

			try
			{
				// Give p's resources of type 'r' to currentPlayer
				grant.put(type, num);
				p.spendResources(grant, bank);
				sum += num;
				players.get(currentPlayer).grantResources(grant, bank);
			}
			catch (CannotAffordException | BankLimitException e)
			{
				/* Will never happen */ }

			// Set up steal event
			Board.Steal.Builder steal = Board.Steal.newBuilder();
			steal.setQuantity(num).setResource(r);
			steal.setVictim(Board.Player.newBuilder().setId(p.getId()).build());
			multiSteal.addThefts(steal.build());
		}

		// Grant resources
		grant.put(type, sum);
		return multiSteal.build();
	}

	/**
	 * Checks that the player can build a road at the desired location, and
	 * builds it.
	 * 
	 * @param edge the edge to build a road on
	 * @return the response message to the client
	 * @throws RoadExistsException
	 * @throws CannotBuildRoadException
	 * @throws CannotAffordException
	 */
	public Events.Event buildRoad(Board.Edge edge) throws CannotAffordException, CannotBuildRoadException,
			RoadExistsException, InvalidCoordinatesException, BankLimitException
	{
		ServerPlayer p = (ServerPlayer) players.get(currentPlayer);
		Board.Point p1 = edge.getA(), p2 = edge.getB();
		Node n = grid.getNode(p1.getX(), p1.getY());
		Node n2 = grid.getNode(p2.getX(), p2.getY());
		Events.Event.Builder ev = Events.Event.newBuilder();

		// Check valid coordinates
		if (n == null) { throw new InvalidCoordinatesException(p1.getX(), p1.getY()); }
		if (n2 == null) { throw new InvalidCoordinatesException(p2.getX(), p2.getY()); }

		// Cannot upgrade
		if (bank.getAvailableRoads(p.getColour()) == 0) { throw new BankLimitException("No more roads available"); }

		// Try to build the road and update the longest road
		p.buildRoad(grid.getEdge(p1, p2), bank);
		bank.setAvailableRoads(p.getColour(), bank.getAvailableRoads(p.getColour()) - 1);
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

		// Add hexes
		for (Hex h : getGrid().getHexesAsList())
		{
			builder.addHexes(h.toHexProto());
		}

		// Add ports
		for (Port p : getGrid().getPortsAsList())
		{
			builder.addHarbours(p.toPortProto());
		}

		// Add player settings
		for (Player p : getPlayersAsList())
		{
			builder.addPlayerSettings(p.getPlayerSettings());

			// set own player
			if (p.getColour().equals(request))
			{
				builder.setOwnPlayer(p.getPlayerSettings().getPlayer());
			}
		}

		return builder.build();
	}

	public Lobby.GameInfo getGameInfo(Colour c)
	{
		Lobby.GameInfo.Builder gameInfo = Lobby.GameInfo.newBuilder();
		gameInfo.setResources(processResources(players.get(c).getResources()));
		gameInfo.setCards(processCards(players.get(c).getDevelopmentCards()));
		gameInfo.setGameInfo(getGameSettings(c));

		// Set roads
		for (Edge e : grid.edges)
		{
			if (e.getRoad() != null)
			{
				Lobby.GameInfo.Road.Builder road = Lobby.GameInfo.Road.newBuilder();
				road.setOwner(Board.Player.newBuilder().setId(getPlayer(c).getId()).build());
				road.setEdge(e.toEdgeProto());
				gameInfo.addRoads(road.build());
			}
		}

		// Set settlements and cities
		for (Node n : grid.nodes.values())
		{
			Building building = n.getBuilding();
			if (building != null && building instanceof Settlement)
			{
				Lobby.GameInfo.Settlement.Builder s = Lobby.GameInfo.Settlement.newBuilder();
				s.setOwner(Board.Player.newBuilder().setId(getPlayer(c).getId()).build());
				s.setPoint(n.toProto());
				gameInfo.addSettlements(s.build());
			}
			else if (building != null && building instanceof City)
			{
				Lobby.GameInfo.Settlement.Builder city = Lobby.GameInfo.Settlement.newBuilder();
				city.setOwner(Board.Player.newBuilder().setId(getPlayer(c).getId()).build());
				city.setPoint(n.toProto());
				gameInfo.addCities(city.build());
			}
		}

		// Set players
		for (Player p : players.values())
		{
			int unusedCards = p.getNumDevCards();
			int resources = p.getNumResources();

			gameInfo.addPlayers(
					Lobby.GameInfo.PlayerInfo.newBuilder().setPlayer(Board.Player.newBuilder().setId(p.getId()).build())
							.setPlayedCards(processCards(p.getPlayedDevCards())).setResources(resources)
							.setUnusedCards(unusedCards).build());
		}

		return gameInfo.build();
	}

	/**
	 * Toggles a player's turn
	 * 
	 * @return
	 */
	public EmptyOuterClass.Empty changeTurn()
	{
		// Reset recent dev card for player, if they bought one this turn
		getPlayer(getCurrentPlayer()).clearRecentDevCards();

		// Update turn and set event.
		setCurrentPlayer(getNextPlayer());
		current++;
		return EmptyOuterClass.Empty.getDefaultInstance();
	}

	/**
	 * Generates a random roll between 2 and 12
	 */
	public Board.Roll generateDiceRoll()
	{
		Board.Roll.Builder roll = Board.Roll.newBuilder();
		roll.setA(dice.nextInt(6) + 1).setB(dice.nextInt(6) + 1);

		Map<Colour, Map<ResourceType, Integer>> playerResources = allocateResources(roll.getA() + roll.getB());

		// Add resource generation
		for (Colour c : playerResources.keySet())
		{
			Player p = getPlayer(c);
			Board.ResourceAllocation.Builder alloc = Board.ResourceAllocation.newBuilder();
			alloc.setPlayer(Board.Player.newBuilder().setId(p.getId()).build());
			alloc.setResources(processResources(playerResources.get(c)));
			roll.addResourceAllocation(alloc.build());
		}

		return roll.build();
	}

	/**
	 * Looks to see if any player has won
	 * 
	 * @return true if a player has won
	 */
	public boolean isOver()
	{
		for (Player p : getPlayersAsList())
		{
			if (p.hasWon()) return true;
		}

		return false;
	}

	/**
	 *
	 * @param joinLobby the join lobby request
	 * @param colour
	 * @return the updated list of usernames
	 */
	public Lobby.Usernames joinGame(Lobby.Join joinLobby, Colour colour) throws GameFullException
	{
		// If player not registered yet
		if (colour == null)
		{
			colour = joinGame();
		}

		Player p = players.get(colour);
		p.setUserName(joinLobby.getUsername());

		// Add all users to update message
		Lobby.Usernames.Builder users = Lobby.Usernames.newBuilder();
		for (Player player : players.values())
		{
			users.addUsername(player.getUsername());
		}

		return users.build();
	}

	public Colour joinGame() throws GameFullException
	{
		// If game is full
		if (numPlayers == NUM_PLAYERS) throw new GameFullException();

		// Assign colour and id
		Colour newCol = Colour.values()[numPlayers];
		Board.Player.Id id = Board.Player.Id.forNumber(numPlayers++);
		ServerPlayer p = new ServerPlayer(newCol, "");
		p.setId(id);

		// Add player info and return assigned colour
		idsToColours.put(id, newCol);
		players.put(p.getColour(), p);
		return p.getColour();
	}

	/**
	 * @return the gameWon message with everyone's card reveals
	 */
	public Lobby.GameWon getGameWon()
	{
		Lobby.GameWon.Builder gameWon = Lobby.GameWon.newBuilder();
		List<DevelopmentCardType> vps = new ArrayList<>();
		vps.add(DevelopmentCardType.Library);
		vps.add(DevelopmentCardType.University);

		// Set card reveal
		for (Player p : getPlayers().values())
		{
			Lobby.GameWon.CardReveal.Builder reveal = Lobby.GameWon.CardReveal.newBuilder();
			reveal.setPlayer(Board.Player.newBuilder().setId(p.getId()).build());

			// If is the winner
			if (p.hasWon())
			{
				gameWon.setWinner(reveal.getPlayer());
			}

			// For each type of VP card
			for (DevelopmentCardType type : vps)
			{
				int num = p.getDevelopmentCards().getOrDefault(type, 0);

				// For the number of this vp card that are revealed
				for (int i = 0; i < num; i++)
					reveal.addVPCards(DevelopmentCardType.toProto(type).getVictoryPoint());
			}
			gameWon.addHiddenCards(reveal.build());
		}

		return gameWon.build();
	}

	public Map<Colour, Player> getPlayers()
	{
		return players;
	}
}
