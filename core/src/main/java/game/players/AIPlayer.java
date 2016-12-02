package main.java.game.players;

import main.java.enums.Colour;
import main.java.game.moves.*;

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
