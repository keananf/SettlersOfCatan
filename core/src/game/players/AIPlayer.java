package game.players;

import game.enums.Colour;
import game.moves.Moves;

/**
 * Class representing an AI player
 */
public class AIPlayer extends Player
{
	public AIPlayer(Colour colour)
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
