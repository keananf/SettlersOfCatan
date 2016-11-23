package main.java.game;

import main.java.game.build.*;
import main.java.game.enums.*;
import main.java.game.moves.*;
import main.java.game.players.*;

import java.awt.Point;
import java.util.*;

import main.java.board.Hex;
import main.java.board.HexGrid;

public class Game
{
	private HexGrid grid;
	private List<Player> players;
	Random dice;
	Player currentPlayer;
	private int current; // index of current player
	private Player p;
	public static final int NUM_PLAYERS = 4;
	
	public Game()
	{
		grid = new HexGrid();
		players = new ArrayList<Player>(NUM_PLAYERS); 
		dice = new Random();
	}

	/**
	 * Chooses first player.
	 */
	public void chooseFirstPlayer()
	{
		int dice = this.dice.nextInt(NUM_PLAYERS);
		
		current = dice;
		currentPlayer = getPlayers().get(dice);
	}

	/**
	 * Assigns resources to each player based upon their settlements and the dice
	 * @param dice the dice roll
	 */
	public void allocateResources(int dice)
	{
		// for each player
		for(Player player : getPlayers())
		{
			Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
			
			// for each of the players settlements
			for(Building building : player.getSettlements().values())
			{
				int amount = building instanceof City ? 2 : 1;
				List<Hex> hexes = ((Building)building).getNode().getHexes();
				
				// for each hex on this settlement
				for(Hex hex : hexes)
				{
					// If the hex's chit is equal to the dice roll
					if(hex.getChit() == dice)
					{
						grant.put(hex.getResource(), amount);
					}
				}
			}
			player.grantResources(grant); // Will be overriden in each type of player's implementation
		}
	}

	/**
	 * Toggles a player's turn
	 */
	public void changeTurn()
	{
		currentPlayer = getPlayers().get(++current % NUM_PLAYERS);
	}

	/**
	 * Generates a random roll between 1 and 12
	 */
	public int generateDiceRoll()
	{
		return dice.nextInt(12) + 1;
	}

	/**
	 * Looks to see if any player has won
	 * @return true if a player has won
	 */
	public boolean isOver()
	{
		for(Player p : getPlayers())
		{
			if(p.hasWon()) return true;
		}
		
		return false;
	}

	/**
	 * @param players the players to set
	 */
	public void addPlayer(Player player)
	{
		this.players.add(player);
	}

	/**
	 * @return the grid
	 */
	public HexGrid getGrid()
	{
		return grid;
	}
	
	public List<Player> getPlayers()
	{
		return players;
	}
}
