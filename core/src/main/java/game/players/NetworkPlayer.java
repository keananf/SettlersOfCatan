package game.players;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import grid.Edge;
import grid.Node;

import java.awt.*;
import java.net.InetAddress;
import java.util.*;
import java.util.List;

/**
 * Class representing a player from across the network
 */
public class NetworkPlayer extends Player
{
	private InetAddress inetAddress;

	public NetworkPlayer(Colour colour, String username)
	{
		super(colour, username);
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

		// If valid placement, attempt to spend the required resources
		if(canBuildSettlement(node))
		{
			spendResources(s.getCost());
			addSettlement(s);
		}

		else if(!canAfford(Settlement.getSettlementCost()))
		{
			throw new CannotAffordException(resources, Settlement.getSettlementCost());
		}

		// Check if empty
		else if(node.getSettlement() != null)
		{
			throw new SettlementExistsException(s);
		}

		// Check if within two nodes of any other settlement
		else if(s.isNearSettlement())
		{
			throw new IllegalPlacementException(s);
		}
	}

	/**
	 * Attempts to purchase a development card for this player
	 * @param card the desired type. Testing only
	 * @return the bought development card
	 * @throws CannotAffordException
	 */
	public DevelopmentCardType buyDevelopmentCard(DevelopmentCardType card) throws CannotAffordException
	{
		// Try to buy a development card
		spendResources(DevelopmentCardType.getCardCost());
		addDevelopmentCard(DevelopmentCardType.toProto(card));

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
		// Check that the move is legal
		if (canBuildCity(node))
		{
			// Otherwise build city
			City c = new City(node, colour);
			spendResources(c.getCost());
			addSettlement(c);
		}
		else if(node.getSettlement() == null)
		{
			throw new CannotUpgradeException(node.getX(), node.getY());
		}
		else if (!canAfford(City.getCityCost()))
		{
			throw new CannotAffordException(getResources(), City.getCityCost());
		}
	}

	/**
	 * Take one resource randomly from the other player
	 * @param other the other player
	 * @param resource the resource to take
	 */
	public void takeResource(Player other, ResourceType resource) throws CannotStealException
	{
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();

		// Check the specified resource can be taken
		if(!other.getResources().containsKey(resource) || other.getResources().get(resource) == 0)
		{
			throw new CannotStealException(colour, other.getColour());
		}

		try
		{
			grant.put(resource, 1);
			other.spendResources(grant);
		}
		catch (CannotAffordException e){ /* Cannot happen*/ }

		// Grant and return
		grantResources(grant);
	}

	/**
	 * @return duplicate of this player
	 */
	public Player copy()
	{
		// Set up player
		Player p = new NetworkPlayer(colour, userName);
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
	 */
	public void restoreCopy(Player copy)
	{
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

	}
}