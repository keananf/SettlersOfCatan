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
 * Abstract class describing a player (AI, or network)
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

	private static final int VP_THRESHOLD = 10;
	private static final int MIN_SETTLEMENTS = 2;

	public Player(Colour colour, String userName)
	{
		this.colour = colour;
		roads = new ArrayList<List<Road>>();
		settlements = new HashMap<Point, Building>();
		resources = new HashMap<ResourceType, Integer>();
		cards = new HashMap<DevelopmentCardType, Integer>();
		playedDevCards = new HashMap<DevelopmentCardType, Integer>();
		this.userName = userName;

		// Initialise resources
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.Generic) continue;
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
		for(List<Road> list : roads)
		{
			if (list.size() > longest) 
				longest = list.size();
		}
		
		return longest;
	}

	/**
	 * Checks the new road against all other lists of connected roads.
	 * 
	 * If it is connected to one of the connected lists, then
	 * it is added and all the members' of that list are updated to reflect
	 * the new chain length
	 * @param r the new road
	 * @param listsAddedTo the list which records if more than one list was added to
	 * (i.e. this new road connects two previously separate ones)
	 * @return boolean dictating whether or not this method was successful
	 */
	protected boolean checkRoadsAndAdd(Road r, List<Integer> listsAddedTo)
	{
		boolean isConnected = false, valid = false;
		int index = 0;
		
		// Check if this road is adjacent to any others
		for (List<Road> list : roads)
		{
			isConnected  = false;
			for(Road road : list)
			{
				Edge e = r.getEdge(), e2 = road.getEdge();
				if (r.isConnected(road))
				{
					// Find if the joined node has a settlement on it
					Node joinedNode = e.getX().equals(e2.getX()) || e.getX().equals(e2.getY()) ? e.getX() : e.getY();
					Building building = joinedNode.getSettlement();
					
					// If there is a settlement and it is not foreign, then the join is not broken
					if((building ==  null) || (building != null && building.getPlayerColour().equals(r.getPlayerColour())))
					{
						isConnected = true;
						listsAddedTo.add(index);
					}
					break;	
				}
			}
			
			// Update list
			if(isConnected)
			{
				valid = true;
			}
			index++;
		}
		
		return valid;
	}

	/**
	 * Finds the road chain these two edges belong to, and breaks it into two
	 * @param e the first edge
	 * @param other the other edge
	 */
	public void breakRoad(Edge e, Edge other)
	{
		List<Road> newList1 = new ArrayList<Road>();
		List<Road> newList2 = new ArrayList<Road>();
		boolean isConnected = false;
		int index = 0;
		
		// For each road chain
		for(List<Road> subList : roads)
		{
			// For each road in the chain
			for(Road road : subList)
			{
				// Divide up subList
				if(e.getRoad().isConnected(road))
				{
					isConnected = true;
					newList1.add(road);
				}
				else if(other.getRoad().isConnected(road))
				{
					isConnected = true;
					newList2.add(road);
				}
			}
			index++;
			if(isConnected) break;
		}
		
		// Remove old list and add two new ones
		roads.remove(index - 1);
		roads.add(newList1);
		roads.add(newList2);
	}
	
	/**
	 * Merges two roads together if a road was recently built which connects
	 * to previously separate ones
	 * @param r the new road
	 * @param listsAddedTo the number of lists (of adjacent roads) it was added to 
	 */
	protected void mergeRoads(Road r, List<Integer> listsAddedTo)
	{
		if(listsAddedTo.size() > 0)
		{
			// Add road to first added road chain.
			roads.get(listsAddedTo.get(0)).add(r);
		}

		// Cascade all road chains back to the first road chain (since it has the new road added)
		for(int i = listsAddedTo.size() - 1; i >= 1; i--)
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
		for(Integer i : resources.values())
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
		for(Integer i : getDevelopmentCards().values())
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
	 * @param cost
	 * @throws CannotAffordException
	 */
	public boolean canAfford(Map<ResourceType, Integer> cost)
	{
		// Check if the player can afford this before initiating the purchase
		for(ResourceType r : cost.keySet())
		{
			if(resources.get(r) < cost.get(r))
				return false;
		}
		
		return true;
	}

	/**
	 * Checks to see if building a road is valid at the given edge
	 * @param edge the desired road location
	 * @return if the desired location is valid for a road
	 */
	public boolean canBuildRoad(Edge edge)
	{
		List<Integer> listsAddedTo = new ArrayList<Integer>();
		Road r = new Road(edge, colour);

		// Road already here. Cannot build
		if (edge.getRoad() != null) return false;

		// Find out where this road is connected
		boolean valid = checkRoadsAndAdd(r, listsAddedTo);

		// Check the location is valid for building and that the player can
		// afford it
		if (r.getEdge().hasSettlement() || valid || (getRoads().size() < 2 && r.getEdge().hasSettlement()))
		{
			return getRoads().size() < 2 || canAfford(Road.getRoadCost());
		}

		return false;
	}

	/**
	 * Checks to see if the player can build a settlement
	 * @param node the desired settlement location
	 * @return if building a settlement at the given node is legal
	 */
	public boolean canBuildSettlement(Node node)
	{
		Point p = new Point(node.getX(), node.getY());
		Settlement s = new Settlement(node, colour);

		return (canAfford(Settlement.getSettlementCost()) || getSettlements().size() < MIN_SETTLEMENTS) && !settlements.containsKey(p)
				&& !s.isNearSettlement() && (node.isNearRoad(colour) || getSettlements().size() < MIN_SETTLEMENTS);
	}

	/**
	 * Checks to see if the player can build a city
	 * @param node the desired city location
	 * @return if building a city at the given node is legal
	 */
	public boolean canBuildCity(Node node)
	{
		Point p = new Point(node.getX(), node.getY());

		return canAfford(City.getCityCost()) && settlements.containsKey(p) && settlements.get(p) instanceof Settlement;
	}

	/**
	 * Grants resources to the player
	 * @param newResources a map of resources to give to the player
	 */
	public void grantResources(Map<ResourceType, Integer> newResources, Bank bank) throws BankLimitException
	{
		bank.spendResources(newResources);

		// Add each new resource and its amount to the player's resource bank
		for(ResourceType r : newResources.keySet())
		{
			int value = newResources.get(r);
			int existing = resources.containsKey(r) ? resources.get(r) : 0;

			// Add to overall resource bank
			resources.put(r, value + existing);
		}
	}

	/**
	 * Grants resources to the player
	 * @param count a map of resources to give to the player
	 */
	public void grantResources(Resource.Counts count, Bank bank) throws CannotAffordException, BankLimitException
	{
		Map<ResourceType, Integer> newResources = new HashMap<ResourceType, Integer> ();
		newResources.put(ResourceType.Brick, count.getBrick());
		newResources.put(ResourceType.Wool, count.getWool());
		newResources.put(ResourceType.Ore, count.getOre());
		newResources.put(ResourceType.Grain, count.getGrain());
		newResources.put(ResourceType.Lumber, count.getLumber());

		grantResources(newResources, bank);
	}


	/**
	 * Spends resources to the player
	 * @param count the resources describing the IBuildable that the player
	 * wants to construct
	 * @throws CannotAffordException if the player does not have enough resources
	 */
	public void spendResources(Resource.Counts count, Bank bank) throws CannotAffordException
	{
		Map<ResourceType, Integer> cost = new HashMap<ResourceType, Integer> ();
		cost.put(ResourceType.Brick, count.getBrick());
		cost.put(ResourceType.Wool, count.getWool());
		cost.put(ResourceType.Ore, count.getOre());
		cost.put(ResourceType.Grain, count.getGrain());
		cost.put(ResourceType.Lumber, count.getLumber());

		spendResources(cost, bank);
	}

	/**
	 * Spends resources to the player
	 * @param cost a map of resources describing the IBuildable that the player
	 * wants to construct
	 * @throws CannotAffordException if the player does not have enough resources
	 */
	public void spendResources(Map<ResourceType, Integer> cost, Bank bank) throws CannotAffordException
	{
		if(!canAfford(cost))
			throw new CannotAffordException(resources, cost);

		// Subtract each resource and its amount from the player's resource bank
		for(ResourceType r : cost.keySet())
		{
			int value = cost.get(r);
			int existing = resources.get(r);

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
		List<Road> total = new ArrayList<Road>();
		
		for(List<Road> list : roads)
			total.addAll(list);
		
		return total;
	}

	/**
	 * @return the hasLargestArmy
	 */
	public boolean hasLargestArmy()
	{
		return hasLargestArmy;
	}

	/**
	 * @return the hasLongestRoad
	 */
	public boolean hasLongestRoad()
	{
		return hasLongestRoad;
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
	 * @param b the new building
	 */
	public void addSettlement(Building b)
	{
		Node node = b.getNode();
		Point point = new Point(node.getX(), node.getY());

		node.setSettlement(b);
		addVp(1);
		settlements.put(point, b);
	}

	/**
	 * Adds the given development card
	 * @param card the development card to add
	 */
    public void addDevelopmentCard(Board.DevCard card)
	{
		DevelopmentCardType type = DevelopmentCardType.fromProto(card);

		int existing = cards.containsKey(type) ? cards.get(type) : 0;
		cards.put(type, existing + 1);

		// Grant VP point if necessary
		if(type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University))
		{
			existing = playedDevCards.containsKey(DevelopmentCardType.Library) ?playedDevCards.get(DevelopmentCardType.Library) : 0;
			playedDevCards.put(DevelopmentCardType.Library, existing + 1);
			vp++;
		}
		else if(type.equals(DevelopmentCardType.University))
		{
			existing = playedDevCards.containsKey(DevelopmentCardType.University) ?playedDevCards.get(DevelopmentCardType.University) : 0;
			playedDevCards.put(DevelopmentCardType.University, existing + 1);
			vp++;
		}
	}

	/**
	 * Plays the given development card
	 * @param card the development card to add
	 */
	protected void playCard(DevelopmentCardType card)
	{
		int existing = cards.containsKey(card) ? cards.get(card) : 0;
		cards.put(card, existing - 1);
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

	public String getUsername() {
		return userName;
	}

    public void setUserName(String userName)
	{
        this.userName = userName;
    }

	public void setResources(Map<ResourceType,Integer> resources)
	{
		this.resources = resources;
	}

    public void setDevelopmentCards(Map<DevelopmentCardType,Integer> developmentCards)
	{
        this.cards = developmentCards;
    }
}

