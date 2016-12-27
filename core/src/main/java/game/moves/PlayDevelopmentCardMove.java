package main.java.game.moves;

import main.java.game.build.DevelopmentCard;

public class PlayDevelopmentCardMove extends Move
{
	private DevelopmentCard card;

	/**
	 * @return the card
	 */
	public DevelopmentCard getCard()
	{
		return card;
	}

	/**
	 * @param card the card to set
	 */
	public void setCard(DevelopmentCard card)
	{
		this.card = card;
	}
}
