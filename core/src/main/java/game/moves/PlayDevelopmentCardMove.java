package main.java.game.moves;

import main.java.game.build.DevelopmentCard;

public class PlayDevelopmentCardMove extends Move
{
	private DevelopmentCard card;
	private String moveAsJson;

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

	/**
	 * @return the moveAsJson
	 */
	public String getMoveAsJson()
	{
		return moveAsJson;
	}

	/**
	 * @param moveAsJson the moveAsJson to set
	 */
	public void setMoveAsJson(String moveAsJson)
	{
		this.moveAsJson = moveAsJson;
	}
}
