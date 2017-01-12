package game.players;

import java.awt.Point;
import java.util.*;

import board.*;
import enums.*;
import exceptions.*;
import game.build.*;
import protocol.ResourceProtos.*;

/**
 * Abstract class describing a player (AI, or network)
 * @author 140001596
 */
public abstract class Player 
{
	private int vp; // Victory points
	private Colour colour;
	private Map<ResourceType, Integer> resources;
	private List<List<Road>> roads; 
	private HashMap<Point, Building> settlements;
	private int knightsUsed;
	private boolean hasLongestRoad;
	private HashMap<DevelopmentCardType, Integer> cards;
	private int numResources;

	private static final int THRESHHOLD = 10;
	
	public Player(Colour colour)
	{
		this.colour = colour;
		roads = new ArrayList<List<Road>>();
		settlements = new HashMap<Point, Building>();
		resources = new HashMap<ResourceType, Integer>();
		cards = new HashMap<DevelopmentCardType, Integer>();
		
		// Initialise resources
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.Generic) continue;
			resources.put(r, 0);
		}
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
	 * Attempts to build a road for this player
	 * @param edge the edge to build the road on
	 * @return the length of the player's longest road
	 * @throws CannotAffordException if the player cannot afford it
	 * @throws CannotBuildRoadException if it is not a valid place to build a road
	 * @throws RoadExistsException 
	 */
	public int buildRoad(Edge edge) throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		boolean valid = false;
		List<Integer> listsAddedTo = new ArrayList<Integer>();
		Road r = new Road(edge, colour);

		// Road already here. Cannot build
		if(edge.getRoad() != null)
			throw new RoadExistsException(r);

		// Find out where this road is connected
		valid = checkRoadsAndAdd(r, listsAddedTo);
		
		// Check the location is valid for building and that the player can afford it
		if(r.getEdge().hasSettlement() || valid)
		{
			spendResources(r.getCost());
			edge.setRoad(r);
			
			// If not connected to any other roads
			if (listsAddedTo.size() == 0)
			{
				List<Road> newList = new ArrayList<Road>();
				newList.add(r);
				roads.add(newList);
			}

			// merge lists if necessary
			else if(listsAddedTo.size() > 1)
				mergeRoads(r, listsAddedTo);
		}
		else throw new CannotBuildRoadException(r);

		return calcRoadLength();
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
	private boolean checkRoadsAndAdd(Road r, List<Integer> listsAddedTo)
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
						listsAddedTo.add(index++);
					}
					break;	
				}
			}
			
			// Update list
			if(isConnected)
			{
				list.add(r);
				valid = true;
			}
			else index++;
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
	private void mergeRoads(Road r, List<Integer> listsAddedTo)
	{
		for(int i = 1; i < listsAddedTo.size(); i++)
		{
			List<Road> last = roads.get(listsAddedTo.get(i - 1));
			List<Road> current = roads.get(listsAddedTo.get(i));
			
			// Remove the duplicate road in one of the lists and add it to the other one.
			last.remove(r);
			current.addAll(last);
			
			roads.remove(last);
		}
	}

	/**
	 * Attempts to build a settlement for this player
	 * @param node the node to build the settlement on
	 * @throws CannotAffordException
	 * @throws SettlementExistsException 
	 */
	public void buildSettlement(Node node) throws CannotAffordException, IllegalPlacementException, SettlementExistsException
	{
		Settlement s = new Settlement(node, colour);
		
		// Check if empty
		if(node.getSettlement() != null)
		{
			throw new SettlementExistsException(s);
		}
		
		// Check if within two nodes of any other settlement
		if(s.isNearSettlement())
		{
			throw new IllegalPlacementException(s);
		}
		
		// If valid placement, attempt to spend the required resources
		spendResources(s.getCost());
		node.setSettlement(s);
		
		settlements.put(new Point(node.getX(), node.getY()), s);
	}

	/**
	 * Attempts to purchase a development card for this player
	 * @return the bought development card
	 * @throws CannotAffordException
	 */
	public DevelopmentCardType buyDevelopmentCard() throws CannotAffordException
	{
		DevelopmentCardType card = DevelopmentCardType.chooseRandom(colour);
		int existing = cards.containsKey(card) ? cards.get(card) : 0;

		// Try to buy a development card
		spendResources(DevelopmentCardType.getCardCost());
		cards.put(card, existing + 1);

		return card;
	}

	/**
	 * Attempts to purchase a development card for this player
	 * @param card the desired type. Testing only
	 * @return the bought development card
	 * @throws CannotAffordException
	 */
	public DevelopmentCardType buyDevelopmentCard(DevelopmentCardType card) throws CannotAffordException
	{
		int existing = cards.containsKey(card) ? cards.get(card) : 0;

		// Try to buy a development card
		spendResources(DevelopmentCardType.getCardCost());
		cards.put(card, existing + 1);

		return card;
	}
	
	/**
	 * Attempts to play the development card for this player
	 * @param card the development card to play
	 * @throws DoesNotOwnException if the user does not own the given card
	 */
	public void playDevelopmentCard(DevelopmentCardType card) throws DoesNotOwnException
	{
		// Check if the player owns the given card
		if(!cards.containsKey(card))
		{
			throw new DoesNotOwnException(card, getColour());
		}
		
		// Remove from inventory
		int existing = cards.containsKey(card) ? cards.get(card) : 0;
		cards.put(card, existing - 1);

	}
	
	/**
	 * Attempts to upgrade a settlement for this player
	 * @param node the node to build the settlement on
	 * @throws CannotAffordException
	 */
	public void upgradeSettlement(Node node) throws CannotAffordException, CannotUpgradeException
	{
		Point p = new Point(node.getX(), node.getY());
		
		// If settlement doesn't yet exist, cannot upgrade
		if(!settlements.containsKey(p))
			throw new CannotUpgradeException(node.getX(), node.getY());
			
		// Otherwise build city
		City c = new City(node, colour);
		spendResources(c.getCost());
		
		// Override settlement
		node.setSettlement(c);
		settlements.put(p, c);
	}
	
	/**
	 * Grants resources to the player
	 * @param newResources a map of resources to give to the player
	 */
	public void grantResources(Map<ResourceType, Integer> newResources)
	{
		// Add each new resource and its amount to the player's resource bank
		for(ResourceType r : newResources.keySet())
		{
			int value = newResources.get(r);
			int existing = resources.containsKey(r) ? resources.get(r) : 0;
			
			// Add to overall resource bank
			resources.put(r, value + existing);
			numResources += value;
		}
	}

	/**
	 * Grants resources to the player
	 * @param count a map of resources to give to the player
	 */
	public void grantResources(ResourceCount count) throws CannotAffordException
	{
		Map<ResourceType, Integer> newResources = new HashMap<ResourceType, Integer> ();
		newResources.put(ResourceType.Brick, count.getBrick());
		newResources.put(ResourceType.Wool, count.getWool());
		newResources.put(ResourceType.Ore, count.getOre());
		newResources.put(ResourceType.Grain, count.getGrain());
		newResources.put(ResourceType.Lumber, count.getLumber());

		grantResources(newResources);
	}

	/**
	 * Spends resources to the player
	 * @param count the resources describing the IBuildable that the player
	 * wants to construct
	 * @throws CannotAffordException if the player does not have enough resources
	 */
	public void spendResources(ResourceCount count) throws CannotAffordException
	{
		Map<ResourceType, Integer> cost = new HashMap<ResourceType, Integer> ();
		cost.put(ResourceType.Brick, count.getBrick());
		cost.put(ResourceType.Wool, count.getWool());
		cost.put(ResourceType.Ore, count.getOre());
		cost.put(ResourceType.Grain, count.getGrain());
		cost.put(ResourceType.Lumber, count.getLumber());

		spendResources(cost);
	}

	/**
	 * Spends resources to the player
	 * @param cost a map of resources describing the IBuildable that the player
	 * wants to construct
	 * @throws CannotAffordException if the player does not have enough resources
	 */
	public void spendResources(Map<ResourceType, Integer> cost) throws CannotAffordException
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
			numResources -= value;
		}
	}

	/**
	 * Checks if a player has more than 7 resource cards.
	 * 
	 * If so, cards are randomly removed until the player has 7 again.
	 */
	public Map<ResourceType, Integer> loseResources()
	{
		Random rand = new Random();
		int resourceLimit = 7;
		Map<ResourceType, Integer> removed = new HashMap<ResourceType, Integer>();

		// Randomly remove resources until the cap is reached
		while(numResources > resourceLimit)
		{
			ResourceType key = (ResourceType) resources.keySet().toArray()[rand.nextInt(resources.size())];
			
			if(resources.get(key) > 0)
			{
				int existing =  removed.containsKey(key) ? removed.get(key) : 0;
				resources.put(key, resources.get(key) - 1);
				removed.put(key, existing + 1);
				numResources--;
			}
		}

		return removed;
	}
	
	/**
	 * Take one resource randomly from the other player
	 * @param other the other player
	 */
	public ResourceType takeResource(Player other)
	{
		Random rand = new Random();
		ResourceType key = ResourceType.Generic;
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();

		// Check there are resources to take
		if(other.getNumResources() == 0) return ResourceType.Generic;
		
		// Find resource to take
		while((key = (ResourceType) other.getResources().keySet().toArray()[rand.nextInt(other.getResources().size())]) == ResourceType.Generic || other.getResources().get(key) == 0);
		grant.put(key, 1);

		try
		{
			other.spendResources(grant);
		}
		catch (CannotAffordException e){ /* Cannot happen*/ }

		// Grant and return
		grantResources(grant);
		return key;
	}

	
	/**
	 * @return the total number of resource cards the player has
	 */
	public int getNumResources()
	{
		return numResources;
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
	 * @return duplicate of this player
	 */
	public Player copy()
	{
		// Set up player
		Player p = this instanceof AIPlayer ? new AIPlayer(colour) : new NetworkPlayer(colour);
		p.resources = new HashMap<ResourceType, Integer>();
		p.cards = new HashMap<DevelopmentCardType, Integer>();
		p.settlements = new HashMap<Point, Building>();
		p.roads = new ArrayList<List<Road>>();

		// Initialise Resources
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.Generic) continue;
			p.resources.put(r, 0);
		}

		// Copy over this player's information
		p.setVp(vp);
		p.grantResources(this.resources);
		p.cards.putAll(this.cards);
		p.settlements.putAll(this.settlements);
		p.roads.addAll(this.roads);
		p.hasLongestRoad = this.hasLongestRoad;
		p.knightsUsed = this.knightsUsed;
		
		return p;
	}
	
	/**
	 * Restores the player to the given state
	 * @param copy the copy
	 * @param card the card that was spent
	 */
	public void restoreCopy(Player copy, DevelopmentCardType card)
	{
		numResources = 0;
		resources = new HashMap<ResourceType, Integer>();
		cards = new HashMap<DevelopmentCardType, Integer>();
		settlements = new HashMap<Point, Building>();
		roads = new ArrayList<List<Road>>();

		// Initialise Resources
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.Generic) continue;
			resources.put(r, 0);
		}
		
		// Copy over this player's information
		setVp(vp);
		grantResources(copy.resources);
		cards.putAll(copy.cards);
		settlements.putAll(copy.settlements);
		roads.addAll(copy.roads);
		hasLongestRoad = copy.hasLongestRoad;
		knightsUsed = copy.knightsUsed;

		// Re-add the spent card:
		if(card != null)
		{			
			cards.put(card, cards.containsKey(card) ? cards.get(card) + 1 : 1);
		}
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
	public void setVp(int amount)
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
		return vp >= THRESHHOLD;
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
	public HashMap<DevelopmentCardType, Integer> getDevelopmentCards()
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
	 * @return the hasLongestRoad
	 */
	public boolean hasLongestRoad()
	{
		return hasLongestRoad;
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
}
