package game;

import game.build.*;
import game.enums.ResourceType;
import game.moves.Moves;
import game.players.Player;

import java.util.*;

import board.*;

public class Game
{
	HexGrid grid;
	Player[] players;
	Hashtable<Player, List<Building>> thingsBuilt;
	Random dice;
	Player currentPlayer;
	private int current; // index of current player
	private static final int NUM_PLAYERS = 4;
	
	public Game()
	{
		grid = new HexGrid();
		players = new Player[NUM_PLAYERS];
		thingsBuilt = new Hashtable<Player, List<Building>>();
		dice = new Random();
	}
	
	public static void main(String[] args)
	{
		Game game = new Game();
		game.getPlayers(); //TODO
		game.chooseFirstPlayer();
		game.getInitialSettlementsAndRoads(); //TODO
		
		// Main game loop
		while(!game.isOver())
		{
			int dice = game.generateDiceRoll();
			game.allocateResources(dice);
			
			Moves moves = game.currentPlayer.receiveMoves();
			game.processMoves(moves); //TODO
			game.changeTurn();
		}
	}

	private void getPlayers()
	{
		// TODO Auto-generated method stub
		
	}

	private void getInitialSettlementsAndRoads()
	{
		// TODO Auto-generated method stub
		
	}

	private void processMoves(Moves moves)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Chooses first player.
	 */
	private void chooseFirstPlayer()
	{
		int dice = this.dice.nextInt(NUM_PLAYERS);
		
		current = dice;
		currentPlayer = players[dice];
	}

	/**
	 * Assigns resources to each player based upon their settlements and the dice
	 * @param dice the dice roll
	 */
	private void allocateResources(int dice)
	{
		// for each player
		for(Player player : players)
		{
			Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
			
			// for each of the players settlements
			for(Building building : thingsBuilt.get(player))
			{
				int amount = building instanceof City ? 2 : 1;
				List<Hex> hexes = building.getNode().getHexes();
				
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
	private void changeTurn()
	{
		currentPlayer = players[++current % NUM_PLAYERS];
	}

	/**
	 * Generates a random roll between 1 and 12
	 */
	private int generateDiceRoll()
	{
		return dice.nextInt(12) + 1;
	}

	/**
	 * Looks to see if any player has won
	 * @return true if a player has won
	 */
	private boolean isOver()
	{
		for(Player p : players)
		{
			if(p.hasWon()) return true;
		}
		
		return false;
	}
}
