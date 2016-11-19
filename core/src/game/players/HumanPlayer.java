package game.players;

import game.enums.Colour;
import game.moves.Moves;

/**
 * Class representing a Human player
 */
public class HumanPlayer extends Player
{

	public HumanPlayer(Colour colour)
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