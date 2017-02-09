package tests;

import board.Edge;
import board.Hex;
import board.Node;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.NetworkPlayer;
import game.players.Player;
import server.ServerGame;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestHelper
{
	protected ServerGame game;
	protected NetworkPlayer p;
	protected Node n;
	protected Hex hex;

	protected Settlement makeSettlement(Player p, Node n) throws SettlementExistsException
	{
		assertTrue(hasResources(p));
		int oldSize = p.getSettlements().size();
		int oldResources = p.getNumResources();

		// Build settlement
		try
		{
			((NetworkPlayer)p).buildSettlement(n);
		}
		catch (IllegalPlacementException | CannotAffordException e)
		{
			e.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getSettlements().size() > oldSize);
		assertTrue(oldResources > p.getNumResources());

		return (Settlement) p.getSettlements().values().toArray()[p.getSettlements().values().size() - 1];
	}

	protected City makeCity(Node n)
	{
		assertTrue(hasResources(p));

		// Build settlement
		try
		{
			p.upgradeSettlement(n);
		}
		catch (CannotAffordException | CannotUpgradeException e)
		{
			e.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getSettlements().size() == 1);

		return (City) p.getSettlements().values().toArray()[p.getSettlements().values().size() - 1];
	}

	protected Road buildRoad(Edge e) throws CannotBuildRoadException, RoadExistsException
	{
		int oldSize = p.getRoads().size();

		assertTrue(hasResources(p));
		try
		{
			p.buildRoad(e);
		}
		catch (CannotAffordException ex)
		{
			ex.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getRoads().size() > oldSize);
		assertFalse(hasResources(p));

		return p.getRoads().get(p.getRoads().size() - 1);
	}

	protected DevelopmentCardType buyDevelopmentCard() throws CannotAffordException
	{
		int oldSize = p.getDevelopmentCards().size();
		DevelopmentCardType c = DevelopmentCardType.Knight;

		assertTrue(hasResources(p));
		try
		{
			c = p.buyDevelopmentCard(DevelopmentCardType.RoadBuilding);
		}
		catch (CannotAffordException ex)
		{
			ex.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getDevelopmentCards().size() > oldSize);
		assertFalse(hasResources(p));

		return c;
	}

	protected boolean hasResources(Player p)
	{
		for(ResourceType r : p.getResources().keySet())
		{
			if(p.getResources().get(r) > 0)
				return true;
		}

		return false;
	}

	protected void reset()
	{
		game = new ServerGame();
		p = new NetworkPlayer(Colour.BLUE);
		game.addPlayer(p);
		game.setCurrentPlayer(p.getColour());

		// Find hex without 'Generic'
		for(int i = 0; i < game.getGrid().nodes.values().size(); i++)
		{
			n = (Node) game.getGrid().nodes.values().toArray()[i];
			hex = n.getHexes().get(0);

			// for each hex
			boolean valid = true;
			for(Hex h : n.getHexes())
			{
				for(Hex h2 : n.getHexes())
					if(h2.getChit() == h.getChit() && !h.equals(h2))
					{
						valid = false;
						break;
					}

			}

			// Skip if this one isn't the desert
			if(valid && hex.getResource() != ResourceType.Generic && !hex.hasRobber())
				break;

		}
	}
}
