package game.players;

import board.Edge;
import board.Node;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.build.Settlement;

import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Class representing a player from across the network
 */
public class NetworkPlayer extends Player
{
	private InetAddress inetAddress;

	public NetworkPlayer(Colour colour)
	{
		super(colour);
	}


	/**
	 * @return the inetAddress
	 */
	public InetAddress getInetAddress()
	{
		return inetAddress;
	}

	/**
	 * sets the inetaddress of this network player
	 * @param inetAddress
	 */
	public void setInetAddress(InetAddress inetAddress)
	{
		this.inetAddress = inetAddress;
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
		java.util.List<Integer> listsAddedTo = new ArrayList<Integer>();
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
				java.util.List<Road> newList = new ArrayList<Road>();
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
		addSettlement(s);
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
		addSettlement(c);
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
	 * @return duplicate of this player
	 */
	public Player copy()
	{
		// Set up player
		Player p = new NetworkPlayer(colour);
		p.resources = new HashMap<ResourceType, Integer>();
		p.cards = new HashMap<DevelopmentCardType, Integer>();
		p.settlements = new HashMap<Point, Building>();
		p.roads = new ArrayList<java.util.List<Road>>();

		// Initialise Resources
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.Generic) continue;
			p.resources.put(r, 0);
		}

		// Copy over this player's information
		p.addVp(vp);
		p.grantResources(this.resources);
		p.cards.putAll(this.cards);
		p.settlements.putAll(this.settlements);
		p.roads.addAll(this.roads);
		p.hasLongestRoad = this.hasLongestRoad;
		p.armySize = this.armySize;

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
		roads = new ArrayList<java.util.List<Road>>();

		// Initialise Resources
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.Generic) continue;
			resources.put(r, 0);
		}

		// Copy over this player's information
		addVp(vp);
		grantResources(copy.resources);
		cards.putAll(copy.cards);
		settlements.putAll(copy.settlements);
		roads.addAll(copy.roads);
		hasLongestRoad = copy.hasLongestRoad;
		hasLargestArmy = copy.hasLargestArmy;
		armySize = copy.armySize;

		// Re-add the spent card:
		if(card != null)
		{
			cards.put(card, cards.containsKey(card) ? cards.get(card) + 1 : 1);
		}
	}
}