package game;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.players.Player;
import grid.Edge;
import grid.Hex;
import grid.HexGrid;
import grid.Node;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Game
{
	protected HexGrid grid;
	protected final Map<Colour, Player> players;
	protected final Map<Board.Player.Id, Colour> idsToColours;
	protected Colour currentPlayer;
	private Colour playerWithLongestRoad;
	private Colour playerWithLargestArmy;
	private int longestRoad;
	private int largestArmy;
	protected final Bank bank;
	protected int numPlayers;
	protected int current; // index of current player
	public static int NUM_PLAYERS = 4;
	private static final int MIN_ROAD_LENGTH = 5;
	private static final int MIN_ARMY_SIZE = 3;

	protected Game()
	{
		bank = new Bank();
		grid = new HexGrid(true);
		players = new HashMap<>();
		idsToColours = new HashMap<>();
	}

	/**
	 * Retrieves the resources granted to this specific player based on the dice
	 * 
	 * @param dice the dice roll
	 * @param c the colour to get the new resources for
	 * @return the map of new resources to grant
	 */
	public Map<ResourceType, Integer> getNewResources(int dice, Colour c)
	{
		int resourceLimit = 7;
		Player player = players.get(c);
		Map<ResourceType, Integer> grant = new HashMap<>();

		// If 7, check that no one is above the resource limit
		if (dice == resourceLimit) { return grant; }

		// for each of this player's settlements
		for (Building building : player.getSettlements().values())
		{
			int amount = building instanceof City ? 2 : 1;
			List<Hex> hexes = building.getNode().getHexes();

			// for each hex on this settlement
			for (Hex hex : hexes)
			{
				// If the hex's chit is equal to the dice roll
				if (hex.getChit() == dice && !hex.hasRobber())
				{
					grant.put(hex.getResource(), amount);
				}
			}
		}
		return grant;
	}

	/**
	 * Converts a map of dev cards into an object combatible with protobufs
	 * 
	 * @param map the map to convert
	 * @return the protobuf representation of the map
	 */
	protected Lobby.GameInfo.PlayerDevCardInfo processCards(Map<DevelopmentCardType, Integer> map)
	{
		Lobby.GameInfo.PlayerDevCardInfo.Builder info = Lobby.GameInfo.PlayerDevCardInfo.newBuilder();
		info.setUniversity(map.getOrDefault(DevelopmentCardType.University, 0));
		info.setLibrary(map.getOrDefault(DevelopmentCardType.Library, 0));
		info.setYearOfPlenty(map.getOrDefault(DevelopmentCardType.YearOfPlenty, 0));
		info.setMonopoly(map.getOrDefault(DevelopmentCardType.Monopoly, 0));
		info.setRoadBuilding(map.getOrDefault(DevelopmentCardType.RoadBuilding, 0));
		info.setKnight(map.getOrDefault(DevelopmentCardType.Knight, 0));

		return info.build();
	}

	/**
	 * Translates the protobuf representation of dev cards into a map.
	 * 
	 * @param info the dev cards received from the network
	 * @return a map of dev cards to number
	 */
	protected Map<DevelopmentCardType, Integer> processCards(Lobby.GameInfo.PlayerDevCardInfo info)
	{
		Map<DevelopmentCardType, Integer> ret = new HashMap<>();

		// Add all info with amounts
		if (info.getKnight() > 0) ret.put(DevelopmentCardType.Knight, info.getKnight());
		if (info.getYearOfPlenty() > 0) ret.put(DevelopmentCardType.YearOfPlenty, info.getYearOfPlenty());
		if (info.getLibrary() > 0) ret.put(DevelopmentCardType.Library, info.getLibrary());
		if (info.getMonopoly() > 0) ret.put(DevelopmentCardType.Monopoly, info.getMonopoly());
		if (info.getUniversity() > 0) ret.put(DevelopmentCardType.University, info.getUniversity());
		if (info.getRoadBuilding() > 0) ret.put(DevelopmentCardType.RoadBuilding, info.getRoadBuilding());

		return ret;
	}

	/**
	 * Translates the protobuf representation of a resources allocation into a
	 * map.
	 * 
	 * @param resources the resources received from the network
	 * @return a map of resources to number
	 */
	public Map<ResourceType, Integer> processResources(Resource.Counts resources)
	{
		Map<ResourceType, Integer> ret = new HashMap<>();

		// Add all resources with amounts
		if (resources.getBrick() > 0) ret.put(ResourceType.Brick, resources.getBrick());
		if (resources.getLumber() > 0) ret.put(ResourceType.Lumber, resources.getLumber());
		if (resources.getGrain() > 0) ret.put(ResourceType.Grain, resources.getGrain());
		if (resources.getOre() > 0) ret.put(ResourceType.Ore, resources.getOre());
		if (resources.getWool() > 0) ret.put(ResourceType.Wool, resources.getWool());

		return ret;
	}

	/**
	 * Converts a map of resources into an object combatible with protobufs
	 * 
	 * @param map the map to convert
	 * @return the protobuf representation of the map
	 */
	public Resource.Counts processResources(Map<ResourceType, Integer> map)
	{
		Resource.Counts.Builder resources = Resource.Counts.newBuilder();
		resources.setGrain(map.getOrDefault(ResourceType.Grain, 0));
		resources.setBrick(map.getOrDefault(ResourceType.Brick, 0));
		resources.setOre(map.getOrDefault(ResourceType.Ore, 0));
		resources.setWool(map.getOrDefault(ResourceType.Wool, 0));
		resources.setLumber(map.getOrDefault(ResourceType.Lumber, 0));

		return resources.build();
	}

	/**
	 * Checks and updates who has the longest road
	 * 
	 * @param broken if this method is being called after a road was broken
	 */
	protected void checkLongestRoad(boolean broken)
	{
		Player playerWithLongestRoad = players.get(this.playerWithLongestRoad);
		List<Colour> cols = new ArrayList<>();
		int max = 0;

		// Calculate which player(s) has / have the longest road
		for (Player player : players.values())
		{
			Colour c = player.getColour();
			int length = player.calcRoadLength();

			if (length > max)
			{
				cols.clear();
				max = length;
				cols.add(c);
			}
			else if (length == max) cols.add(c);
		}

		// Remove longest road victory points if necessary
		if (longestRoad >= MIN_ROAD_LENGTH)
		{
			// If the player no longer has longest road
			if (max < MIN_ROAD_LENGTH || !cols.contains(playerWithLongestRoad.getColour()))
			{
				this.playerWithLongestRoad = null;
				playerWithLongestRoad.setHasLongestRoad(false);
			}

			// if the road was broken but the player with the longest road still
			// has it
			if (broken && cols.contains(playerWithLongestRoad.getColour()))
			{
				longestRoad = max;
				return;
			}
		}
		longestRoad = max;

		// If the new longest road meets the threshold
		if (max >= MIN_ROAD_LENGTH)
		{
			// If there is a tie, no one gets longest road
			if (!broken || cols.size() <= 1)
			{
				Colour c = cols.get(0);
				Player player = players.get(c);
				this.playerWithLongestRoad = c;
				player.setHasLongestRoad(true);
			}
		}
	}

	/**
	 * This API is used for both the client and server when determining if a new
	 * settlement has broken a road chain.
	 * 
	 * @param node
	 */
	protected void checkIfRoadBroken(Node node)
	{
		// Check all combinations of edges to check if a road chain was broken
		for (int i = 0; i < node.getEdges().size(); i++)
		{
			boolean broken = false;
			for (int j = 0; j < node.getEdges().size(); j++)
			{
				Edge e = node.getEdges().get(i), other = node.getEdges().get(j);
				Road r = e.getRoad(), otherR = other.getRoad();

				if (e.equals(other)) continue;

				// If this settlement is between two roads of the same colour
				if (r != null && otherR != null && r.getPlayerColour().equals(otherR.getPlayerColour()))
				{
					// retrieve owner of roads and break the road chain
					players.get(e.getRoad().getPlayerColour()).breakRoad(e, other);
					broken = true;
					break;
				}
			}
			if (broken)
			{
				checkLongestRoad(broken);
				break;
			}
		}
	}

	/**
	 * Checks all players for who has the largest army, and updates VP points
	 */
	protected void checkLargestArmy()
	{
		Player playerWithLargestArmy = players.get(this.playerWithLargestArmy);

		// Calculate who has longest road
		for (Colour c : Colour.values())
		{
			if (!players.containsKey(c)) continue;

			Player player = players.get(c);
			int armySize = player.getArmySize();
			if (armySize > largestArmy)
			{
				// Update victory points
				if (largestArmy >= MIN_ARMY_SIZE)
				{
					playerWithLargestArmy.addVp(-2);
				}
				if (armySize >= MIN_ARMY_SIZE) player.addVp(2);

				largestArmy = armySize;
				this.playerWithLargestArmy = c;
			}
		}
	}

	/**
	 * Chooses first player.
	 */
	public void chooseFirstPlayer()
	{
		setCurrentPlayer(getPlayer(Board.Player.Id.PLAYER_1).getColour());
	}

	/**
	 * @return the grid
	 */
	public HexGrid getGrid()
	{
		return grid;
	}

	/**
	 * @return the currentPlayer
	 */
	public Colour getCurrentPlayer()
	{
		return currentPlayer;
	}

	/**
	 * @param currentPlayer the currentPlayer to set
	 */
	public void setCurrentPlayer(Colour currentPlayer)
	{
		this.currentPlayer = currentPlayer;
	}

	/**
	 * @return the next player
	 */
	protected Colour getNextPlayer()
	{
		return getPlayer(Board.Player.Id.forNumber((1 + current) % NUM_PLAYERS)).getColour();
	}

	/**
	 * @return the next player
	 */
	protected Colour getLastPlayer()
	{
		return getPlayer(Board.Player.Id.forNumber((current - 1) % NUM_PLAYERS)).getColour();
	}

	public Player[] getPlayersAsList()
	{
		return players.values().toArray(new Player[] {});
	}

	/**
	 * @return the players
	 */
	public Map<Colour, Player> getPlayers()
	{
		return players;
	}

	/**
	 * Retrieves the corresponding player
	 * 
	 * @param col the player's colour
	 * @return
	 */
	public Player getPlayer(Colour col)
	{
		return players.get(col);
	}

	/**
	 * Retrieves the corresponding player
	 * 
	 * @param id the player's id
	 * @return
	 */
	public Player getPlayer(Board.Player.Id id)
	{
		enums.Colour col = idsToColours.get(id);
		return players.get(col);
	}

	public void addPlayer(Player clientPlayer)
	{
		idsToColours.put(clientPlayer.getId(), clientPlayer.getColour());
		players.put(clientPlayer.getColour(), clientPlayer);
	}

	public Bank getBank()
	{
		return bank;
	}
}
