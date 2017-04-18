package game.players;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.BankLimitException;
import exceptions.CannotAffordException;
import game.Bank;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import grid.Edge;
import grid.Node;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class describing a player (ai, or network)
 * 
 * @author 140001596
 */
public abstract class Player
{
	protected int vp; // Victory points
	protected Colour colour;
	protected Map<ResourceType, Integer> resources;
	protected List<List<Road>> roads;
	protected HashMap<Point, Building> settlements;
	protected boolean hasLongestRoad;
	protected boolean hasLargestArmy;
	protected Map<DevelopmentCardType, Integer> cards, playedDevCards;
	protected boolean playedDevCard;
	protected int armySize;
	protected Board.Player.Id id;
	protected String userName;
	protected Map<DevelopmentCardType, Integer> recentBoughtCards;

	private static final int VP_THRESHOLD = 10;
	private static final int MIN_SETTLEMENTS = 2;
	protected int expectedRoads, expectedResources;

	public Player(Colour colour, String userName)
	{
		this.colour = colour;
		roads = new ArrayList<>();
		recentBoughtCards = new HashMap<>();
		settlements = new HashMap<>();
		resources = new HashMap<>();
		cards = new HashMap<>();
		playedDevCards = new HashMap<>();
		this.userName = userName;

		// Initialise resources
		for (ResourceType r : ResourceType.values())
		{
			if (r == ResourceType.Generic) continue;
			resources.put(r, 0);
		}

		playedDevCard = false;
	}

	/**
	 * @return the length of this player's longest road
	 */
	public int calcRoadLength()
	{
		int longest = 0;
		for (List<Road> list : roads)
		{
			if (list.size() > longest) longest = list.size();
		}

		return longest;
	}

	/**
	 * Checks the new road against all other lists of connected roads.
	 * 
	 * If it is connected to one of the connected lists, then it is added and
	 * all the members' of that list are updated to reflect the new chain length
	 * 
	 * @param r the new road
	 * @param listsAddedTo the list which records if more than one list was
	 *            added to (i.e. this new road connects two previously separate
	 *            ones)
	 * @return boolean dictating whether or not this method was successful
	 */
	protected boolean checkRoadsAndAdd(Road r, List<Integer> listsAddedTo)
	{
		boolean isConnected = false, valid = false;
		int index = 0;

		// Check if this road is adjacent to any others
		for (List<Road> list : roads)
		{
			isConnected = false;
			for (Road road : list)
			{
				Edge e = r.getEdge(), e2 = road.getEdge();
				if (r.isConnected(road))
				{
					// Find if the joined node has a settlement on it
					Node joinedNode = e.getX().equals(e2.getX()) || e.getX().equals(e2.getY()) ? e.getX() : e.getY();
					Building building = joinedNode.getBuilding();

					// If there is a settlement and it is not foreign, then the
					// join is not broken
					if ((building == null) || building.getPlayerColour().equals(r.getPlayerColour()))
					{
						isConnected = true;
						listsAddedTo.add(index);
					}
					break;
				}
			}

			// Update list
			if (isConnected)
			{
				valid = true;
			}
			index++;
		}

		return valid;
	}

	/**
	 * Finds the road chain these two edges belong to, and breaks it into two
	 * 
	 * @param e the first edge
	 * @param other the other edge
	 */
	public void breakRoad(Edge e, Edge other)
	{
		List<Road> newList1 = new ArrayList<>();
		List<Road> newList2 = new ArrayList<>();
		boolean isConnected = false;
		int index = 0;

		// For each road chain
		for (List<Road> subList : roads)
		{
			// For each road in the chain
			for (Road road : subList)
			{
				// Divide up subList
				if (e.getRoad().isConnected(road) || other.getRoad().isConnected(road))
				{
					isConnected = true;
					break;
				}
			}
			index++;
			if (isConnected)
			{
				List<List<Road>> lists = segmentRoads(subList, e, other);
				newList1 = lists.get(0);
				newList2 = lists.get(1);
				break;
			}
		}

		// Remove old list and add two new ones
		roads.remove(index - 1);
		roads.add(newList1);
		roads.add(newList2);
	}

	public List<List<Road>> segmentRoads(List<Road> sublist, Edge e, Edge other)
	{
		List<Road> newList1 = new ArrayList<Road>();
		List<Road> newList2 = new ArrayList<Road>();
		List<Road> skipped = new ArrayList<Road>();
		Road eRoad = e.getRoad(), otherRoad = other.getRoad();
		newList1.add(eRoad);
		newList2.add(otherRoad);

		// For each road in the chain
		for (Road road : sublist)
		{
			if (newList1.contains(road) || newList2.contains(road)) continue;

			// Divide up subList
			if (e.getRoad().isConnected(road))
			{
				newList1.add(road);
			}
			else if (other.getRoad().isConnected(road))
			{
				newList2.add(road);
			}
			else
				skipped.add(road);
		}

		// Loop until all roads sorted
		while (newList1.size() + newList2.size() != sublist.size())
		{
			// For each skipped road
			for (Road road : skipped)
			{
				if (newList1.contains(road) || newList2.contains(road)) continue;

				// Check if connected to any road in first list
				boolean added = false;
				for (Road r1 : newList1)
				{
					if (road.isConnected(r1))
					{
						newList1.add(road);
						added = true;
						break;
					}
				}
				if (added) continue;

				// Check if connected to any road in second list
				for (Road r2 : newList2)
				{
					if (road.isConnected(r2))
					{
						newList2.add(road);
						break;
					}
				}
			}
		}

		List<List<Road>> newList = new ArrayList<List<Road>>();
		newList.add(newList1);
		newList.add(newList2);
		return newList;
	}

	/**
	 * Merges two roads together if a road was recently built which connects to
	 * previously separate ones
	 * 
	 * @param r the new road
	 * @param listsAddedTo the number of lists (of adjacent roads) it was added
	 *            to
	 */
	protected void mergeRoads(Road r, List<Integer> listsAddedTo)
	{
		if (listsAddedTo.size() > 0)
		{
			// Add road to first added road chain.
			roads.get(listsAddedTo.get(0)).add(r);
		}

		// Cascade all road chains back to the first road chain (since it has
		// the new road added)
		for (int i = listsAddedTo.size() - 1; i >= 1; i--)
		{
			List<Road> last = roads.get(listsAddedTo.get(i - 1));
			List<Road> current = roads.get(listsAddedTo.get(i));

			// Add all elements
			last.addAll(current);
			roads.remove(last);
		}
	}

	/**
	 * @return the total number of resource cards the player has
	 */
	public int getNumResources()
	{
		int numResources = 0;
		for (Integer i : resources.values())
		{
			numResources += i;
		}
		return numResources;
	}

	/**
	 * @return the total number of dev cards the player has
	 */
	public int getNumDevCards()
	{
		int numResources = 0;
		for (Integer i : getDevelopmentCards().values())
		{
			numResources += i;
		}
		return numResources;
	}

	/**
	 * @return the total number of dev cards the player has
	 */
	public Map<DevelopmentCardType, Integer> getPlayedDevCards()
	{
		return playedDevCards;
	}

	/**
	 * Checks to see if the user canAfford something
	 * 
	 * @param cost
	 * @throws CannotAffordException
	 */
	public boolean canAfford(Map<ResourceType, Integer> cost)
	{
		// Check if the player can afford this before initiating the purchase
		for (ResourceType r : cost.keySet())
		{
			if (cost.get(r) == 0) continue;
			if (!resources.containsKey(r) || resources.get(r) < cost.get(r)) return false;
		}

		return true;
	}

	/**
	 * Checks to see if building a road is valid at the given edge
	 * 
	 * @param edge the desired road location
	 * @return if the desired location is valid for a road
	 */
	public boolean canBuildRoad(Edge edge, Bank bank)
	{
		List<Integer> listsAddedTo = new ArrayList<>();
		Road r = new Road(edge, colour);
		Building b = null;

		// Road already here. Cannot build
		if (edge.getRoad() != null) return false;

		// Find out where this road is connected
		boolean valid = checkRoadsAndAdd(r, listsAddedTo);
		if (r.getEdge().getX().getBuilding() != null
				&& r.getEdge().getX().getBuilding().getPlayerColour().equals(colour))
		{
			b = r.getEdge().getX().getBuilding();
		}
		else if (r.getEdge().getY().getBuilding() != null
				&& r.getEdge().getY().getBuilding().getPlayerColour().equals(colour))
		{
			b = r.getEdge().getY().getBuilding();
		}

		// Does b already have an edge and is it the initial phase?
		boolean val = true;
		if (b != null && getRoads().size() < 2)
		{
			for (Edge e : b.getNode().getEdges())
			{
				if (e.getRoad() != null)
				{
					val = false;
				}
			}
		}

		// Check the location is valid for building and that the player can
		// afford it
		return val && bank.getAvailableRoads(colour) > 0
				&& ((getRoads().size() < 2 || canAfford(Road.getRoadCost()) || expectedRoads > 0)
						&& (b != null || valid));

	}

	/**
	 * Checks to see if the player can build a settlement
	 * 
	 * @param node the desired settlement location
	 * @return if building a settlement at the given node is legal
	 */
	public boolean canBuildSettlement(Node node, Bank bank)
	{
		Point p = new Point(node.getX(), node.getY());
		Settlement s = new Settlement(node, colour);

		return bank.getAvailableSettlements(colour) > 0
				&& (canAfford(Settlement.getSettlementCost()) || getSettlements().size() < MIN_SETTLEMENTS)
				&& !settlements.containsKey(p) && node.getBuilding() == null && !s.isNearSettlement()
				&& (node.isNearRoad(colour) || getSettlements().size() < MIN_SETTLEMENTS);
	}

	/**
	 * Checks to see if the player can build a city
	 * 
	 * @param node the desired city location
	 * @return if building a city at the given node is legal
	 */
	public boolean canBuildCity(Node node, Bank bank)
	{
		Point p = new Point(node.getX(), node.getY());

		return bank.getAvailableCities(colour) > 0 && canAfford(City.getCityCost()) && settlements.containsKey(p)
				&& settlements.get(p) instanceof Settlement;
	}

	/**
	 * Grants resources to the player
	 * 
	 * @param newResources a map of resources to give to the player
	 */
	public void grantResources(Map<ResourceType, Integer> newResources, Bank bank) throws BankLimitException
	{
		bank.spendResources(newResources);

		// Add each new resource and its amount to the player's resource bank
		for (ResourceType r : newResources.keySet())
		{
			int value = newResources.get(r);
			int existing = resources.getOrDefault(r, 0);

			// Add to overall resource bank
			resources.put(r, value + existing);
		}
	}

	/**
	 * Grants resources to the player
	 * 
	 * @param count a map of resources to give to the player
	 */
	public void grantResources(Resource.Counts count, Bank bank) throws BankLimitException
	{
		Map<ResourceType, Integer> newResources = new HashMap<>();
		newResources.put(ResourceType.Brick, count.getBrick());
		newResources.put(ResourceType.Wool, count.getWool());
		newResources.put(ResourceType.Ore, count.getOre());
		newResources.put(ResourceType.Grain, count.getGrain());
		newResources.put(ResourceType.Lumber, count.getLumber());

		grantResources(newResources, bank);
	}

	/**
	 * Spends resources to the player
	 * 
	 * @param count the resources describing the IBuildable that the player
	 *            wants to construct
	 * @throws CannotAffordException if the player does not have enough
	 *             resources
	 */
	public void spendResources(Resource.Counts count, Bank bank) throws CannotAffordException
	{
		Map<ResourceType, Integer> cost = new HashMap<>();
		cost.put(ResourceType.Brick, count.getBrick());
		cost.put(ResourceType.Wool, count.getWool());
		cost.put(ResourceType.Ore, count.getOre());
		cost.put(ResourceType.Grain, count.getGrain());
		cost.put(ResourceType.Lumber, count.getLumber());

		spendResources(cost, bank);
	}

	/**
	 * Spends resources to the player
	 * 
	 * @param cost a map of resources describing the IBuildable that the player
	 *            wants to construct
	 * @throws CannotAffordException if the player does not have enough
	 *             resources
	 */
	public void spendResources(Map<ResourceType, Integer> cost, Bank bank) throws CannotAffordException
	{
		if (!canAfford(cost)) throw new CannotAffordException(resources, cost);

		// Subtract each resource and its amount from the player's resource bank
		for (ResourceType r : cost.keySet())
		{
			int value = cost.get(r);
			int existing = resources.getOrDefault(r, 0);

			// Add to overall resource bank
			resources.put(r, existing - value);
		}
		bank.grantResources(cost);
	}

	/**
	 * @return the victory points for this player
	 */
	public int getVp()
	{
		return vp;
	}

	/**
	 * This function increments a player's vp
	 */
	public void addVp(int amount)
	{
		vp += amount;
	}

	/**
	 * @return the colour
	 */
	public Colour getColour()
	{
		return colour;
	}

	/**
	 * @param colour the colour to set
	 */
	public void setColour(Colour colour)
	{
		this.colour = colour;
	}

	/**
	 * @return whether or not the player has enough victory points to win.
	 */
	public boolean hasWon()
	{
		// Add the card reveal
		int vp = this.vp;
		vp += cards.getOrDefault(DevelopmentCardType.University, 0);
		vp += cards.getOrDefault(DevelopmentCardType.Library, 0);

		return vp >= VP_THRESHOLD;
	}

	/**
	 * @return the player's resources
	 */
	public Map<ResourceType, Integer> getResources()
	{
		return resources;
	}

	/**
	 * @return the settlements the player has built
	 */
	public HashMap<Point, Building> getSettlements()
	{
		return settlements;
	}

	/**
	 * @return the development cards in this player's hand
	 */
	public Map<DevelopmentCardType, Integer> getDevelopmentCards()
	{
		return cards;
	}

	/**
	 * @return the roads the player has built
	 */
	public List<Road> getRoads()
	{
		List<Road> total = new ArrayList<>();

		for (List<Road> list : roads)
			total.addAll(list);

		return total;
	}

	/**
	 * @param hasLargestArmy the hasLargestArmy to set
	 */
	public void setHasLargestArmy(boolean hasLargestArmy)
	{
		this.hasLargestArmy = hasLargestArmy;
	}

	/**
	 * @param hasLongestRoad the hasLongestRoad to set
	 */
	public void setHasLongestRoad(boolean hasLongestRoad)
	{
		if (!hasLongestRoad)
		{
			addVp(-2);
		}
		else if (!this.hasLongestRoad) addVp(2);
		this.hasLongestRoad = hasLongestRoad;
	}

	public int getNumOfRoadChains()
	{
		return roads.size();
	}

	/**
	 * @return this player's army size
	 */
	public int getArmySize()
	{
		return armySize;
	}

	/**
	 * Adds one knight to the army
	 */
	public void addKnightPlayed()
	{
		armySize++;
	}

	/**
	 * Adds the new building to this player's list of buildings.
	 *
	 * If a city, the original settlement is overriden
	 * 
	 * @param b the new building
	 */
	public void addSettlement(Building b)
	{
		Node node = b.getNode();
		Point point = new Point(node.getX(), node.getY());

		node.setBuilding(b);
		addVp(1);
		settlements.put(point, b);
	}

	/**
	 * Adds the given development card
	 * 
	 * @param card the development card to add
	 */
	public void addDevelopmentCard(Board.DevCard card)
	{
		DevelopmentCardType type = DevelopmentCardType.fromProto(card);

		int existing = cards.getOrDefault(type, 0);
		cards.put(type, existing + 1);

		// Update recent bought caught
		existing = recentBoughtCards.getOrDefault(type, 0);
		recentBoughtCards.put(type, existing + 1);
	}

	/**
	 * Plays the given development card
	 * 
	 * @param card the development card to add
	 */
	public void playCard(DevelopmentCardType card, Bank bank)
	{
		int existing = cards.getOrDefault(card, 0);
		cards.put(card, existing - 1);

		if (card.equals(DevelopmentCardType.RoadBuilding))
		{
			expectedRoads = bank.getAvailableRoads(colour) > 1 ? 2 : 1;
		}
		if (card.equals(DevelopmentCardType.YearOfPlenty))
		{
			expectedResources = bank.getNumAvailableResources() > 1 ? 2 : 1;
		}
	}

	/**
	 * @return the player settings to be propogated out to the clients
	 */
	public Lobby.GameSetup.PlayerSetting getPlayerSettings()
	{
		Lobby.GameSetup.PlayerSetting.Builder builder = Lobby.GameSetup.PlayerSetting.newBuilder();
		Board.Player.Builder player = Board.Player.newBuilder();

		player.setId(getId());
		builder.setUsername(userName);
		builder.setPlayer(player.build());
		builder.setColour(Colour.toProto(getColour()));

		return builder.build();
	}

	public Board.Player.Id getId()
	{
		return id;
	}

	public void setId(Board.Player.Id id)
	{
		this.id = id;
	}

	public String getUsername()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public void setResources(Map<ResourceType, Integer> resources)
	{
		this.resources = resources;
	}

	public void setDevelopmentCards(Map<DevelopmentCardType, Integer> developmentCards)
	{
		this.cards = developmentCards;
	}

	public void clearRecentDevCards()
	{
		recentBoughtCards.clear();
	}

	public Map<DevelopmentCardType, Integer> getRecentBoughtDevCards()
	{
		return recentBoughtCards;
	}

	public int getExpectedRoads()
	{
		return expectedRoads;
	}

	public int getExpectedResources()
	{
		return expectedResources;
	}

	public void subtractExpectedResources()
	{
		expectedResources--;
	}
}
