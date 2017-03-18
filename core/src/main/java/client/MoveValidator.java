package client;

import enums.DevelopmentCardType;
import game.build.Building;
import game.build.Settlement;
import game.players.Player;
import grid.Edge;
import grid.Node;
import intergroup.Requests;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keanan on 3/14/17.
 */
public class MoveValidator
{
	private ClientGame game;
	private Turn turn;
	private List<Requests.Request.BodyCase> possibilities;

	public MoveValidator(ClientGame game, Turn turn)
	{
		this.game = game;
		this.turn = turn;
		possibilities = new ArrayList<Requests.Request.BodyCase>();
	}

	/**
	 * Processes the last user click
	 */
	public void processChoices()
	{
		// Perform action based upon what type of object was last selected
		checkBuyDevCard();
		checkPlayDevCard(turn.getChosenCard());
		checkBuildRoad(turn.getChosenEdge());
		checkBuild(turn.getChosenNode());
	}

	/**
	 * Checks the player can play the given card
	 * 
	 * @param type the type of card wanting to be played
	 */
	public boolean checkPlayDevCard(DevelopmentCardType type)
	{
		// If player's turn
		if (checkTurn())
		{
			Player player = game.getPlayer();

			// If the player owns the provided card
			if (player.getDevelopmentCards().containsKey(type) && player.getDevelopmentCards().get(type) > 0)
			{
				if (!possibilities.contains(Requests.Request.BodyCase.PLAYDEVCARD))
				{
					possibilities.add(Requests.Request.BodyCase.PLAYDEVCARD);
				}
				return true;
			}
		}

		// Remove possibility if there previously
		if (possibilities.contains(Requests.Request.BodyCase.PLAYDEVCARD))
		{
			possibilities.remove(Requests.Request.BodyCase.PLAYDEVCARD);
		}
		return false;
	}

	/**
	 * Checks to see if the player can buy a dev card
	 */
	public boolean checkBuyDevCard()
	{
		// If player's turn
		if (checkTurn())
		{
			Player player = game.getPlayer();

			if (player.canAfford(DevelopmentCardType.getCardCost()))
			{
				if (!possibilities.contains(Requests.Request.BodyCase.BUYDEVCARD))
				{
					possibilities.add(Requests.Request.BodyCase.BUYDEVCARD);
				}
				return true;
			}
		}

		// Remove possibility if there previously
		if (possibilities.contains(Requests.Request.BodyCase.BUYDEVCARD))
		{
			possibilities.remove(Requests.Request.BodyCase.BUYDEVCARD);
		}
		return false;
	}

	/**
	 * Checks if a city or settlement can be built on the given nodeS
	 * 
	 * @param node the desired settlement / city location
	 */
	public boolean checkBuild(Node node)
	{
		// If player's turn
		if (checkTurn())
		{
			Building building = node.getSettlement();

			// If there is a settlement present
			if (building != null && building instanceof Settlement)
			{

				// Check if city can be built
				if (game.getPlayer().canBuildCity(node))
				{
					if (!possibilities.contains(Requests.Request.BodyCase.BUILDCITY))
					{
						possibilities.add(Requests.Request.BodyCase.BUILDCITY);
					}
					return true;
				}
			}

			// If can build a settlement
			else if (game.getPlayer().canBuildSettlement(node))
			{
				if (!possibilities.contains(Requests.Request.BodyCase.BUILDSETTLEMENT))
				{
					possibilities.add(Requests.Request.BodyCase.BUILDSETTLEMENT);
				}
				return true;
			}
		}

		// Remove possibilities if there previously
		for (Requests.Request.BodyCase r : new Requests.Request.BodyCase[] { Requests.Request.BodyCase.BUILDSETTLEMENT,
				Requests.Request.BodyCase.BUILDCITY })
		{
			if (possibilities.contains(r)) possibilities.remove(r);
		}
		return false;
	}

	/**
	 * @return whether or not it is the player's turn
	 */
	private boolean checkTurn()
	{
		return game.getCurrentPlayer() == game.getPlayer().getColour();
	}

	/**
	 * Checks to see if the player can build a road
	 * 
	 * @param edge the desired road location
	 */
	private boolean checkBuildRoad(Edge edge)
	{
		boolean val = game.getPlayer().canBuildRoad(edge);

		// If user's turn
		if (checkTurn() && val)
		{
			if (!possibilities.contains(Requests.Request.BodyCase.BUILDROAD))
			{
				possibilities.add(Requests.Request.BodyCase.BUILDROAD);
			}
			return true;
		}

		// Remove possibility if there
		if (possibilities.contains(Requests.Request.BodyCase.BUILDROAD))
		{
			possibilities.remove(Requests.Request.BodyCase.BUILDROAD);
		}
		return false;
	}
}
