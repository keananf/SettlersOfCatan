package game.players;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.Bank;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import grid.Edge;
import grid.Node;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a player from across the network
 */
public class ServerPlayer extends Player
{
	private InetAddress inetAddress;

	public ServerPlayer(Colour colour, String username)
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
	 * 
	 * @param inetAddress
	 */
	public void setInetAddress(InetAddress inetAddress)
	{
		this.inetAddress = inetAddress;
	}

	/**
	 * Attempts to build a road for this player
	 * 
	 * @param edge the edge to build the road on
	 * @return the length of the player's longest road
	 * @throws CannotAffordException if the player cannot afford it
	 * @throws CannotBuildRoadException if it is not a valid place to build a
	 *             road
	 * @throws RoadExistsException
	 */
	public int buildRoad(Edge edge, Bank bank)
			throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		boolean valid = false;
		List<Integer> listsAddedTo = new ArrayList<Integer>();
		Road r = new Road(edge, colour);

		// Road already here. Cannot build
		if (edge.getRoad() != null) throw new RoadExistsException(r);

		// Find out where this road is connected
		valid = checkRoadsAndAdd(r, listsAddedTo);

		// Check the location is valid for building and that the player can
		// afford it
		if (r.getEdge().hasSettlement() || valid)
		{
			spendResources(r.getCost(), bank);
			edge.setRoad(r);

			// If not connected to any other roads
			if (listsAddedTo.size() == 0)
			{
				java.util.List<Road> newList = new ArrayList<Road>();
				newList.add(r);
				roads.add(newList);
			}

			// merge lists if necessary
			else if (listsAddedTo.size() > 1) mergeRoads(r, listsAddedTo);
		}
		else
			throw new CannotBuildRoadException(r);

		return calcRoadLength();
	}

	/**
	 * Attempts to build a settlement for this player
	 * 
	 * @param node the node to build the settlement on
	 * @param bank
	 * @throws CannotAffordException
	 * @throws SettlementExistsException
	 */
	public void buildSettlement(Node node, Bank bank)
			throws CannotAffordException, IllegalPlacementException, SettlementExistsException
	{
		Settlement s = new Settlement(node, colour);

		// If valid placement, attempt to spend the required resources
		if (canBuildSettlement(node))
		{
			spendResources(s.getCost(), bank);
			addSettlement(s);
		}

		else if (!canAfford(Settlement.getSettlementCost()))
		{
			throw new CannotAffordException(resources, Settlement.getSettlementCost());
		}

		// Check if empty
		else if (node.getSettlement() != null)
		{
			throw new SettlementExistsException(s);
		}

		// Check if within two nodes of any other settlement
		else if (s.isNearSettlement()) { throw new IllegalPlacementException(s); }
	}

	/**
	 * Attempts to purchase a development card for this player
	 * 
	 * @param bank the desired type. Testing only
	 * @return the bought development card
	 * @throws CannotAffordException
	 */
	public DevelopmentCardType buyDevelopmentCard(Bank bank) throws CannotAffordException
	{
		// Try to buy a development card
		spendResources(DevelopmentCardType.getCardCost(), bank);
		DevelopmentCardType card = DevelopmentCardType.chooseRandom(bank);
		addDevelopmentCard(DevelopmentCardType.toProto(card));
		return card;
	}

	/**
	 * Attempts to purchase a development card for this player
	 * 
	 * @param bank the desired type. Testing only
	 * @return the bought development card
	 * @throws CannotAffordException
	 */
	public DevelopmentCardType buyDevelopmentCard(DevelopmentCardType card, Bank bank) throws CannotAffordException
	{
		// Try to buy a development card
		spendResources(DevelopmentCardType.getCardCost(), bank);
		bank.getAvailableDevCards().put(card, bank.getAvailableDevCards().get(card) - 1);
		addDevelopmentCard(DevelopmentCardType.toProto(card));

		return card;
	}

	/**
	 * Attempts to play the development card for this player
	 * 
	 * @param card the development card to play
	 * @throws DoesNotOwnException if the user does not own the given card
	 */
	public void playDevelopmentCard(DevelopmentCardType card) throws DoesNotOwnException
	{
		// Check if the player owns the given card
		if (!cards.containsKey(card)) { throw new DoesNotOwnException(card, getColour()); }

		// Remove from inventory
		int existing = cards.containsKey(card) ? cards.get(card) : 0;
		cards.put(card, existing - 1);
	}

	/**
	 * Attempts to upgrade a settlement for this player
	 * 
	 * @param node the node to build the settlement on
	 * @param bank
	 * @throws CannotAffordException
	 */
	public void upgradeSettlement(Node node, Bank bank) throws CannotAffordException, CannotUpgradeException
	{
		// Check that the move is legal
		if (canBuildCity(node))
		{
			// Otherwise build city
			City c = new City(node, colour);
			spendResources(c.getCost(), bank);
			addSettlement(c);
		}
		else if (node.getSettlement() == null)
		{
			throw new CannotUpgradeException(node.getX(), node.getY());
		}
		else if (!canAfford(
				City.getCityCost())) { throw new CannotAffordException(getResources(), City.getCityCost()); }
	}

	/**
	 * Take one resource randomly from the other player
	 * 
	 * @param other the other player
	 * @param resource the resource to take
	 */
	public void takeResource(Player other, ResourceType resource, Bank bank) throws CannotStealException
	{
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();

		// Check the specified resource can be taken
		if (other.getNumResources() > 0 && (!other.getResources().containsKey(resource) || other.getResources()
				.get(resource) == 0)) { throw new CannotStealException(colour, other.getColour()); }

		// Ignore if player simply has no resources to take
		if(other.getNumResources() == 0)
			return;

		try
		{
			grant.put(resource, 1);
			other.spendResources(grant, bank);
			grantResources(grant, bank);
		}
		catch (CannotAffordException | BankLimitException e)
		{
			/* Cannot happen */
		}
	}

}