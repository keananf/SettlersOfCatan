package game.players;

import game.moves.Moves;

/**
 * Class representing a player from across the network
 */
public class NetworkPlayer extends Player
{

	@Override
	public Moves receiveMoves()
	{
		// TODO Auto-generated method stub
		return new Moves();
	}

}