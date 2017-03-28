package tests;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.Player;
import game.players.ServerPlayer;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.board.Board;
import server.ServerGame;

import static org.junit.Assert.assertTrue;

public class TestHelper
{
	protected ServerGame game;
	protected ServerPlayer p;
	protected Node n;
	protected Hex hex;

	protected Settlement makeSettlement(Player p, Node n) throws SettlementExistsException
	{
		int oldSize = p.getSettlements().size();

		// Build settlement
		try
		{
			game.setCurrentPlayer(p.getColour());
			game.buildSettlement(n.toProto());
		}
		catch (IllegalPlacementException | CannotAffordException e)
		{
			e.printStackTrace();
		}
		catch (InvalidCoordinatesException | BankLimitException e)
		{
			e.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getSettlements().size() > oldSize);
		// assertTrue(oldResources > p.getNumResources());

		return (Settlement) p.getSettlements().values().toArray()[p.getSettlements().values().size() - 1];
	}

	protected City makeCity(Player p, Node n)
	{
		assertTrue(hasResources(p));
		int old = p.getSettlements().size();

		// Build settlement
		try
		{
			game.setCurrentPlayer(p.getColour());
			game.upgradeSettlement(n.toProto());
		}
		catch (CannotAffordException | CannotUpgradeException e)
		{
			e.printStackTrace();
		}
		catch (BankLimitException | InvalidCoordinatesException e)
		{
			e.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getSettlements().size() == old);

		return (City) p.getSettlements().values().toArray()[p.getSettlements().values().size() - 1];
	}

	protected Road buildRoad(Player p, Edge e) throws CannotBuildRoadException, RoadExistsException
	{
		int oldSize = p.getRoads().size();

		try
		{
			game.setCurrentPlayer(p.getColour());
			game.buildRoad(e.toEdgeProto());
		}
		catch (CannotAffordException ex)
		{
			ex.printStackTrace();
		}
		catch (InvalidCoordinatesException | BankLimitException e1)
		{
			e1.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getRoads().size() > oldSize);
		// assertFalse(hasResources(p));

		return p.getRoads().get(p.getRoads().size() - 1);
	}

	protected DevelopmentCardType buyDevelopmentCard() throws CannotAffordException
	{
		int oldSize = p.getDevelopmentCards().size();
		DevelopmentCardType c = DevelopmentCardType.Knight;

		assertTrue(hasResources(p));
		try
		{
			c = ((ServerPlayer) p).buyDevelopmentCard(DevelopmentCardType.RoadBuilding, game.getBank());
		}
		catch (CannotAffordException ex)
		{
			ex.printStackTrace();
		}

		// Test it was built correctly and that resources were taken away
		assertTrue(p.getDevelopmentCards().size() > oldSize);
		// assertFalse(hasResources(p));

		return c;
	}

	protected boolean hasResources(Player p)
	{
		for (ResourceType r : p.getResources().keySet())
		{
			if (p.getResources().get(r) > 0) return true;
		}

		return false;
	}

	protected void reset()
	{
		game = new ServerGame();
		p = new ServerPlayer(Colour.BLUE, "");
		p.setId(Board.Player.Id.PLAYER_1);
		game.addPlayer(p);
		game.setCurrentPlayer(p.getColour());

		// Find hex without 'Generic'
		for (int i = 0; i < game.getGrid().nodes.values().size(); i++)
		{
			n = (Node) game.getGrid().nodes.values().toArray()[i];
			hex = n.getHexes().get(0);

			// for each hex
			boolean valid = true;
			for (Hex h : n.getHexes())
			{
				for (Hex h2 : n.getHexes())
					if (h2.getChit() == h.getChit() && !h.equals(h2))
					{
						valid = false;
						break;
					}

			}

			// Skip if this one isn't the desert
			if (valid && hex.getResource() != ResourceType.Generic && !hex.hasRobber()) break;

		}
	}
}
