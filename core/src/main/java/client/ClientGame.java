package client;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.Game;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.ClientPlayer;
import game.players.Player;
import grid.*;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A game with additional methods for processing protobufs for the client
 * Created by 140001596
 */
public class ClientGame extends Game
{
	private boolean gameOver;
	private Map<Colour, Integer> boughtDevCards, resources;
	private Map<Colour, Map<DevelopmentCardType, Integer>> playedDevCards;
	private ChatBoard chatBoard;
	private int turns = 0;
	private Client client;

	public ClientGame()
	{
		super();
		grid = null;
		boughtDevCards = new HashMap<Colour, Integer>();
		resources = new HashMap<Colour, Integer>();
		playedDevCards = new HashMap<Colour, Map<DevelopmentCardType, Integer>>();
		chatBoard = new ChatBoard();

		// Instantiate the playedDevCards maps
		for (Colour c : Colour.values())
		{
			resources.put(c, 0);
			boughtDevCards.put(c, 0);
			playedDevCards.put(c, new HashMap<DevelopmentCardType, Integer>());

			for (DevelopmentCardType d : DevelopmentCardType.values())
			{
				playedDevCards.get(c).put(d, 0);
			}
		}
	}

	public ClientGame(Client client)
	{
		this();
		this.client = client;
	}

	/**
	 * @return a representation of the board that is compatible with protofbufs
	 * @param beginGame
	 */
	public HexGrid setBoard(Lobby.GameSetup beginGame) throws InvalidCoordinatesException, CannotAffordException
	{
		HexGrid grid = new HexGrid(false);
		this.grid = grid;
		List<Hex> hexes = processHexes(beginGame.getHexesList());
		processPlayerSettings(beginGame.getOwnPlayer(), beginGame.getPlayerSettingsList());

		// Overwrite current grid
		grid.setNodesAndHexes(hexes);
		List<Port> ports = processPorts(beginGame.getHarboursList());
		grid.setPorts(ports);

		return grid;
	}

	/**
	 * @return a representation of the game that is compatible with protofbufs
	 * @param gameInfo
	 */
	public HexGrid processGameInfo(Lobby.GameInfo gameInfo)
			throws InvalidCoordinatesException, CannotAffordException, RoadExistsException, CannotBuildRoadException
	{
		HexGrid grid = setBoard(gameInfo.getGameInfo());
		getPlayer().setResources(processResources(gameInfo.getResources()));
		getPlayer().setDevelopmentCards(processCards(gameInfo.getCards()));

		// Add each player
		for (Lobby.GameInfo.PlayerInfo p : gameInfo.getPlayersList())
		{
			Colour c = getPlayer(p.getPlayer().getId()).getColour();
			boughtDevCards.put(c, p.getUnusedCards());
			playedDevCards.put(c, processCards(p.getPlayedCards()));
			resources.put(c, p.getResources());
		}

		// Add each settlement
		for (Lobby.GameInfo.Settlement s : gameInfo.getSettlementsList())
		{
			Colour c = getPlayer(s.getOwner().getId()).getColour();
			Node n = getGrid().getNode(s.getPoint().getX(), s.getPoint().getY());
			getPlayer(s.getOwner().getId()).addSettlement(new Settlement(n, c));
		}

		// Add each city
		for (Lobby.GameInfo.Settlement city : gameInfo.getCitiesList())
		{
			Colour c = getPlayer(city.getOwner().getId()).getColour();
			Node n = getGrid().getNode(city.getPoint().getX(), city.getPoint().getY());
			getPlayer(city.getOwner().getId()).addSettlement(new City(n, c));
		}

		// Add each road
		for (Lobby.GameInfo.Road road : gameInfo.getRoadsList())
		{
			Colour c = getPlayer(road.getOwner().getId()).getColour();
			Node n = getGrid().getNode(road.getEdge().getA().getX(), road.getEdge().getA().getY());
			Node n2 = getGrid().getNode(road.getEdge().getB().getX(), road.getEdge().getB().getY());
			Edge e = n.findEdge(n2);
			((ClientPlayer) players.get(c)).addRoad(e);
		}

		return grid;
	}

	/**
	 * Loads in all the player information
	 * 
	 * @param ownPlayer this player's information
	 * @param playerSettingsList the list of other players' information
	 */
	private void processPlayerSettings(Board.Player ownPlayer, List<Lobby.GameSetup.PlayerSetting> playerSettingsList)
	{
		// Load in each player's info
		for (Lobby.GameSetup.PlayerSetting player : playerSettingsList)
		{
			enums.Colour col = enums.Colour.fromProto(player.getColour());
			ClientPlayer newPlayer = new ClientPlayer(col, player.getUsername());
			newPlayer.setId(player.getPlayer().getId());

			// Update current turn
			if (newPlayer.getId().equals(Board.Player.Id.PLAYER_1))
			{
				setCurrentPlayer(newPlayer.getColour());
			}

			// Check if it is this player
			if (player.getPlayer().getId().equals(ownPlayer.getId()))
			{
				setPlayer(newPlayer);
				players.put(col, getPlayer());
			}
			else
			{
				players.put(col, newPlayer);
			}

			// Add mapping from colours to ids
			idsToColours.put(player.getPlayer().getId(), col);
		}
	}

	/**
	 * Retrieve the hex objects referred to by the proto
	 * 
	 * @param protos the hex protos
	 */
	private List<Hex> processHexes(List<Board.Hex> protos)
	{
		List<Hex> hexes = new ArrayList<Hex>();

		// Add nodes
		for (Board.Hex proto : protos)
		{
			hexes.add(Hex.fromProto(proto));
		}

		return hexes;
	}

	/**
	 * Retrieve the port objects referred to by the proto
	 * 
	 * @param protos the port protos
	 */
	private List<Port> processPorts(List<Board.Harbour> protos)
	{
		List<Port> ports = new ArrayList<Port>();

		// Add ports
		for (Board.Harbour harbour : protos)
		{
			Board.Edge e = harbour.getLocation();
			Board.Point p1 = e.getA(), p2 = e.getB();
			Port port = new Port(grid.getNode(p1.getX(), p1.getY()), grid.getNode(p2.getX(), p2.getY()));
			port.setExchangeType(ResourceType.fromProto(harbour.getResource()));

			ports.add(port);
		}

		return ports;
	}

	/**
	 * @return if the game has ended or not
	 */
	public boolean isOver()
	{
		return gameOver;
	}

	/**
	 * Once called, the listener thread will terminate the connection to the
	 * server
	 */
	public void setGameOver()
	{
		gameOver = true;
	}

	/**
	 * Writes the given string to the chatBoard
	 * 
	 * @param chatMessage
	 */
	public void writeMessage(String chatMessage, Board.Player instigator)
	{
		Player p = getPlayer(instigator.getId());
		chatBoard.writeMessage(chatMessage, p.getUsername(), p.getColour());
	}

	/**
	 * Updates the dice roll and allocate resources
	 * 
	 * @param dice the new dice roll
	 * @param resourceAllocationList
	 */
	public void processDice(int dice, List<Board.ResourceAllocation> resourceAllocationList)
	{
		if (dice != 7)
		{
			// For each player's new resources
			for (Board.ResourceAllocation alloc : resourceAllocationList)
			{
				Map<ResourceType, Integer> grant = processResources(alloc.getResources());
				Player p = getPlayer(alloc.getPlayer().getId());
				int num = 0;

				try
				{
					if (p.getColour().equals(getPlayer().getColour()))
					{
						p.grantResources(grant, bank);
					}
					else
					{
						int existing = resources.containsKey(p.getColour()) ? resources.get(p.getColour()) : 0;
						for (ResourceType r : grant.keySet())
						{
							num += grant.get(r);
						}
						resources.put(p.getColour(), existing + num);
					}
				}
				catch (BankLimitException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Swap the robber to the given point received from the server
	 * 
	 * @param robberMove the robber's new position
	 */
	public void moveRobber(Board.Point robberMove) throws InvalidCoordinatesException
	{
		Hex hex = grid.getHex(robberMove.getX(), robberMove.getY());

		// If invalid coordinates
		if (hex == null) { throw new InvalidCoordinatesException(robberMove.getX(), robberMove.getY()); }

		grid.swapRobbers(hex);
	}

	/**
	 * Adds the new road received from the server to the board
	 * 
	 * @param newRoad the new road to add.
	 * @param instigator the instigator who instigated the event
	 */
	public Road processRoad(Board.Edge newRoad, Board.Player instigator)
			throws RoadExistsException, CannotBuildRoadException, CannotAffordException
	{
		// Extract information and find edge
		Edge newEdge = grid.getEdge(newRoad.getA(), newRoad.getB());
		Player player = getPlayer(instigator.getId());

		// Spend resources if it is not a preliminary move
		if (player.getRoads().size() >= 2)
		{
			if (player.equals(getPlayer()))
			{
				player.spendResources(Road.getRoadCost(), bank);
			}
			else
			{
				int existing = resources.get(player.getColour());
				resources.put(player.getColour(), existing - Road.getRoadCost().size());
			}
		}

		// Make new road object
		Road r = ((ClientPlayer) players.get(player.getColour())).addRoad(newEdge);
		checkLongestRoad(false);

		return r;
	}

	/**
	 * Processes a new bulding, and adds it to the board
	 * 
	 * @param city the new city
	 * @param instigator the person who built the city
	 * @return the new city
	 */
	public City processNewCity(Board.Point city, Board.Player instigator)
			throws InvalidCoordinatesException, CannotAffordException
	{
		// Extract information
		Node node = grid.getNode(city.getX(), city.getY());
		Player player = getPlayer(instigator.getId());

		// If invalid coordinates
		if (node == null || (node.getBuilding() != null && node.getBuilding() instanceof City))
			throw new InvalidCoordinatesException(city.getX(), city.getY());

		// Handle resources
		if (player.equals(getPlayer()))
		{
			player.spendResources(City.getCityCost(), bank);
		}
		else
		{
			int existing = resources.get(player.getColour());
			int cost = 0;
			for (ResourceType r : City.getCityCost().keySet())
				cost += City.getCityCost().get(r);
			resources.put(player.getColour(), existing - cost);
		}

		// Create and add the city
		City c = new City(node, player.getColour());

		// Updates settlement and score
		players.get(player.getColour()).addSettlement(c);
		return c;
	}

	/**
	 * Processes a new bulding, and adds it to the board
	 * 
	 * @param settlement the new settlement
	 * @param instigator the person who built the settlement
	 * @return the new settlement
	 */
	public Settlement processNewSettlement(Board.Point settlement, Board.Player instigator)
			throws InvalidCoordinatesException, CannotAffordException
	{
		// Extract information
		Node node = grid.getNode(settlement.getX(), settlement.getY());
		Player player = getPlayer(instigator.getId());

		// If invalid coordinates
		if (node == null || (node.getBuilding() != null
				&& node.getBuilding() instanceof Settlement)) { throw new InvalidCoordinatesException(settlement.getX(),
						settlement.getY()); }

		// Spend resources if this is not an initial move
		if (player.getSettlements().size() >= 2)
		{
			if (player.equals(getPlayer()))
			{
				player.spendResources(Settlement.getSettlementCost(), bank);
			}
			else
			{
				int existing = resources.get(player.getColour());
				resources.put(player.getColour(), existing - Settlement.getSettlementCost().size());
			}
		}

		// Create and add the settlement
		Settlement s = new Settlement(node, player.getColour());
		checkIfRoadBroken(node);

		// Updates settlement and score
		players.get(player.getColour()).addSettlement(s);
		return s;
	}

	/**
	 * Records the played dev card for the given player
	 * 
	 * @param playedDevCard the played card
	 */
	public void processPlayedDevCard(Board.PlayableDevCard playedDevCard, Board.Player instigator)
			throws DoesNotOwnException
	{
		DevelopmentCardType card = DevelopmentCardType.fromProto(playedDevCard);
		Map<DevelopmentCardType, Integer> playedCards = playedDevCards.get(currentPlayer);
		Player player = getPlayer(instigator.getId());

		// Record card being played
		int existing = playedCards.get(card);
		playedCards.put(card, existing + 1);

		// Eliminate one bought dev card from player
		existing = boughtDevCards.containsKey(player.getColour()) ? boughtDevCards.get(player.getColour()) : 0;
		if (existing > 0)
		{
			boughtDevCards.put(player.getColour(), existing - 1);
		}
		else
			throw new DoesNotOwnException(card, player.getColour());

		if (player.equals(getPlayer()))
		{
			existing = player.getDevelopmentCards().get(card);
			player.getDevelopmentCards().put(card, existing - 1);
		}

		// Update largest army
		if (card.equals(DevelopmentCardType.Knight))
		{
			players.get(player.getColour()).addKnightPlayed();
			checkLargestArmy();
		}
	}

	/**
	 * Records that the given player bought a dev card
	 * 
	 * @param boughtDevCard the bought dev card
	 * @param instigator the player who caused the event
	 */
	public void recordDevCard(Board.DevCard boughtDevCard, Board.Player instigator) throws CannotAffordException
	{
		Player player = getPlayer(instigator.getId());

		// Handle resources
		if (player.equals(getPlayer()))
		{
			player.spendResources(DevelopmentCardType.getCardCost(), bank);
			getPlayer().addDevelopmentCard(boughtDevCard);
		}
		else
		{
			int existing = resources.get(player.getColour());
			resources.put(player.getColour(), existing - DevelopmentCardType.getCardCost().size());
		}

		// Update number of dev cards each player is known to have
		Colour c = player.getColour();
		int existing = boughtDevCards.containsKey(c) ? boughtDevCards.get(c) : 0;
		boughtDevCards.put(c, existing + 1);
	}

	/**
	 * Processes a bank trade
	 * 
	 * @param bankTrade the bank trade
	 */
	public void processBankTrade(Trade.WithBank bankTrade, Board.Player instigator)
			throws CannotAffordException, BankLimitException
	{
		Map<ResourceType, Integer> offering = processResources(bankTrade.getOffering());
		Map<ResourceType, Integer> wanting = processResources(bankTrade.getWanting());
		Player player = getPlayer(instigator.getId());

		int offeringSize = 0, wantingSize = 0;
		for (ResourceType r : offering.keySet())
			offeringSize += offering.get(r);
		for (ResourceType r : wanting.keySet())
			wantingSize += wanting.get(r);

		// Handle resources
		if (player.equals(getPlayer()))
		{
			// Update resources
			player.spendResources(offering, bank);
			player.grantResources(wanting, bank);
		}
		else
		{
			int existing = resources.get(player.getColour());
			resources.put(player.getColour(), existing - offeringSize + wantingSize);
		}
	}

	/**
	 * Processes a player trade
	 * 
	 * @param playerTrade the player trade
	 */
	public void processPlayerTrade(Trade.WithPlayer playerTrade, Board.Player sender)
			throws CannotAffordException, BankLimitException
	{
		Map<ResourceType, Integer> offering = processResources(playerTrade.getOffering());
		Map<ResourceType, Integer> wanting = processResources(playerTrade.getWanting());
		Player instigator = getPlayer(sender.getId());
		Player recipient = getPlayer(playerTrade.getOther().getId());

		int offeringSize = 0, wantingSize = 0;
		for (ResourceType r : offering.keySet())
			offeringSize += offering.get(r);
		for (ResourceType r : wanting.keySet())
			wantingSize += wanting.get(r);

		// Handle resources for this player
		if (instigator.equals(getPlayer()))
		{
			instigator.spendResources(offering, bank);
			instigator.grantResources(wanting, bank);

			int existing = resources.get(recipient.getColour());
			resources.put(recipient.getColour(), existing - wantingSize + offeringSize);
		}
		else if (recipient.equals(getPlayer()))
		{
			recipient.spendResources(wanting, bank);
			recipient.grantResources(offering, bank);

			int existing = resources.get(instigator.getColour());
			resources.put(instigator.getColour(), existing + wantingSize - offeringSize);
		}
		else
		{
			int existing1 = resources.get(instigator.getColour()), existing2 = resources.get(recipient.getColour());
			resources.put(instigator.getColour(), existing1 + wantingSize - offeringSize);
			resources.put(recipient.getColour(), existing2 - wantingSize + offeringSize);
		}
	}

	/**
	 * Processes the cards this player had to discard
	 * 
	 * @param cardsDiscarded the discarded resources
	 * @param instigator the player who discarded them
	 */
	public void processDiscard(Resource.Counts cardsDiscarded, Board.Player instigator)
			throws CannotAffordException, BankLimitException
	{
		Player player = getPlayer(instigator.getId());

		// Handle resources
		if (player.equals(getPlayer()))
		{
			// Update resources
			player.spendResources(processResources(cardsDiscarded), bank);
		}
		else
		{
			int existing = resources.get(player.getColour());
			int sum = 0;
			for (ResourceType r : processResources(cardsDiscarded).keySet())
				sum += processResources(cardsDiscarded).get(r);
			resources.put(player.getColour(), existing - sum);
		}
	}

	/**
	 * Processes the cards this player had to discard
	 * 
	 * @param steal the stolen resources
	 * @param id the player who stole them
	 */
	public void processResourcesStolen(Board.Steal steal, Board.Player id)
			throws CannotAffordException, BankLimitException
	{
		Player instigator = getPlayer(id.getId());
		Player victim = getPlayer(steal.getVictim().getId());
		ResourceType r = ResourceType.fromProto(steal.getResource());
		int quantity = steal.getQuantity();

		if (quantity == 0) return;

		// Update resources
		Map<ResourceType, Integer> stolen = new HashMap<ResourceType, Integer>();
		stolen.put(r, quantity);

		// Handle resources for this player
		if (instigator.equals(getPlayer()))
		{
			instigator.grantResources(stolen, bank);

			int existing = resources.get(victim.getColour());
			resources.put(victim.getColour(), existing - quantity);
		}
		else if (victim.equals(getPlayer()))
		{
			victim.spendResources(stolen, bank);

			int existing = resources.get(instigator.getColour());
			resources.put(instigator.getColour(), existing + quantity);
		}
		else
		{
			int existing1 = resources.get(instigator.getColour()), existing2 = resources.get(victim.getColour());
			resources.put(instigator.getColour(), existing1 + quantity);
			resources.put(victim.getColour(), existing2 - quantity);
		}
	}

	/**
	 * Processes the cards this player had to discard
	 * 
	 * @param multiSteal the resources taken from each user
	 * @param instigator the player who stole them
	 */
	public void processMonopoly(Board.MultiSteal multiSteal, Board.Player instigator)
			throws CannotAffordException, BankLimitException
	{
		// For each player stolen from
		for (Board.Steal steal : multiSteal.getTheftsList())
		{
			processResourcesStolen(steal, instigator);
		}
	}

	/**
	 * Processes the cards this player had to discard
	 * 
	 * @param resource the stolen resources
	 * @param instigator the player who stole them
	 */
	public void processResourceChosen(Resource.Kind resource, Board.Player instigator) throws BankLimitException
	{
		Player p = getPlayer(instigator.getId());

		if (p.equals(getPlayer()))
		{
			Map<ResourceType, Integer> map = new HashMap<ResourceType, Integer>();
			map.put(ResourceType.fromProto(resource), 1);
			p.grantResources(map, bank);
		}
		else
		{
			int existing = resources.get(p.getColour());
			resources.put(p.getColour(), existing + 1);
		}
	}

	/**
	 * Return the total amounts of dev cards owned by each player
	 * 
	 * @return
	 */
	public Map<Colour, Integer> getBoughtDevCards()
	{
		return boughtDevCards;
	}

	/**
	 * @return the map of played dev cards
	 */
	public Map<Colour, Map<DevelopmentCardType, Integer>> getPlayedDevCards()
	{
		return playedDevCards;
	}

	/**
	 * @return this player
	 */
	public Player getPlayer()
	{
		return client.getPlayer();
	}

	public void setPlayer(ClientPlayer p)
	{
		client.setPlayer(p);
	}

	public int getPlayerResources(Colour colour)
	{
		return resources.get(colour);
	}

	public void giveResources(int size, Colour colour)
	{
		int existing = resources.get(colour);
		resources.put(colour, existing + size);
	}

	public void updateCurrentPlayer()
	{
		if (++turns >= NUM_PLAYERS && turns < NUM_PLAYERS * 2 && current > 0)
		{
			setCurrentPlayer(getLastPlayer());
			current--;
		}
		else if (turns != NUM_PLAYERS * 2 - 1)
		{
			setCurrentPlayer(getNextPlayer());
			current++;
		}
	}

	public int getTurns()
	{
		return turns;
	}

}
