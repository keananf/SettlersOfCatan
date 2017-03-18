package tests;

import intergroup.board.Board;
import client.ClientGame;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.CannotAffordException;
import exceptions.InvalidCoordinatesException;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.LocalPlayer;
import game.players.Player;
import grid.Edge;
import grid.Hex;
import grid.Node;
import org.junit.Before;

import java.util.Map;

public class ClientTestHelper extends TestHelper
{
	protected ClientGame clientGame;
	protected Player clientPlayer;
	private Board.Player player;

	@Before
	public void start() throws CannotAffordException, InvalidCoordinatesException
	{
		reset();
		clientGame = new ClientGame();
		clientPlayer = new LocalPlayer(Colour.BLUE, "");
		clientPlayer.setId(Board.Player.Id.PLAYER_1);
		clientGame.addPlayer(clientPlayer);

		player = Board.Player.newBuilder().setId(clientPlayer.getId()).build();
		clientGame.setBoard(game.getGameSettings(clientPlayer.getColour()));
		clientPlayer = clientGame.getPlayer(clientPlayer.getId());
	}

	public void processRoadEvent(Edge edge, Colour col)
	{
		// Set up request
		Board.Edge e = edge.toEdgeProto();

		player = Board.Player.newBuilder().setId(clientGame.getPlayer(col).getId()).build();
		try
		{
			clientGame.getPlayer(col).grantResources(Road.getRoadCost(), clientGame.getBank());
			if (clientGame.getPlayer(col).getRoads().size() < 2)
			{
				clientGame.getPlayer(col).spendResources(Road.getRoadCost(), clientGame.getBank());
			}
			clientGame.setCurrentPlayer(col);
			clientGame.processRoad(e, player);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void processSettlementEvent(Node node, Colour col)
	{
		// Set up request
		Board.Point n = node.toProto();
		Map<ResourceType, Integer> grant = Settlement.getSettlementCost();

		player = Board.Player.newBuilder().setId(clientGame.getPlayer(col).getId()).build();
		try
		{
			clientGame.getPlayer(col).grantResources(grant, clientGame.getBank());
			if (clientGame.getPlayer(col).getSettlements().size() < 2)
			{
				clientGame.getPlayer(col).spendResources(grant, clientGame.getBank());
			}
			clientGame.setCurrentPlayer(col);
			clientGame.processNewSettlement(n, player);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void processCityEvent(Node node, Colour col)
	{
		// Set up request
		Board.Point n = node.toProto();
		Map<ResourceType, Integer> grant = City.getCityCost();

		player = Board.Player.newBuilder().setId(clientGame.getPlayer(col).getId()).build();
		try
		{
			clientGame.getPlayer(col).grantResources(grant, clientGame.getBank());
			clientGame.setCurrentPlayer(col);
			clientGame.processNewCity(n, player);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void processPlayKnightEvent(Hex h, Colour c)
	{
		// Make a move robber event
		Board.Point point = h.toHexProto().getLocation();

		player = Board.Player.newBuilder().setId(clientGame.getPlayer(c).getId()).build();
		try
		{
			clientGame.getPlayer(c).grantResources(DevelopmentCardType.getCardCost(), clientGame.getBank());
			clientGame.setCurrentPlayer(c);
			clientGame.recordDevCard(
					Board.DevCard.newBuilder().setPlayableDevCard(Board.PlayableDevCard.KNIGHT).build(), player);
			clientGame.processPlayedDevCard(Board.PlayableDevCard.KNIGHT, player);
			clientGame.moveRobber(point);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void processBoughtDevCard(Board.DevCard type, Colour c)
	{
		try
		{
			clientGame.getPlayer(c).grantResources(DevelopmentCardType.getCardCost(), clientGame.getBank());
			clientGame.setCurrentPlayer(c);
			player = Board.Player.newBuilder().setId(clientGame.getPlayer(c).getId()).build();
			clientGame.recordDevCard(type, player);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void processPlayedDevCard(Board.DevCard type, Colour c)
	{

		try
		{
			clientGame.getPlayer(c).grantResources(DevelopmentCardType.getCardCost(), clientGame.getBank());
			clientGame.setCurrentPlayer(c);

			player = Board.Player.newBuilder().setId(clientGame.getPlayer(c).getId()).build();
			clientGame.recordDevCard(type, player);
			clientGame.processPlayedDevCard(type.getPlayableDevCard(), player);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
