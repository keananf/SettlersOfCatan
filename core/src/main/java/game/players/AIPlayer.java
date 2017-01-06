package game.players;

import enums.Colour;
import game.moves.*;

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
	public Move receiveMove()
	{
		// TODO Auto-generated method stub
		return new BuildSettlementMove();
	}
}
