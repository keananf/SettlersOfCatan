package game.players;

import game.enums.Colour;
import game.moves.Moves;

/**
 * Class representing a player from across the network
 */
public class NetworkPlayer extends Player
{

	public NetworkPlayer(Colour colour)
	{
		super(colour);
	}

	@Override
	public Moves receiveMoves()
	{
		// TODO Auto-generated method stub
		return new Moves();
	}

}