package client;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.Bank;
import game.CurrentTrade;
import game.players.Player;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.Requests;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class which determines move possibilities based upon a game state
 *
 * @author 140001596
 */
public class MoveProcessor
{
	private final Client client;

	public MoveProcessor(Client client)
	{
		this.client = client;
	}

	/**
	 * Retrieve a list of all building possibilities, regardless of game context
	 *
	 * @return a list of all possible building turns
	 */
	private List<Turn> getBuildingPossibilities()
	{
		List<Turn> moves = new ArrayList<>();

		// This player
		Player p = getGame().getPlayer();
		if (p.getSettlements().size() == 1 && p.getRoads().size() == 1
				&& !getGame().getCurrentPlayer().equals(p.getColour())) { return moves; }

		// For each node
		for (Node n : getGame().getGrid().nodes.values())
		{
			Turn turn = new Turn();
			if (n.getBuilding() == null)
			{
				turn.setChosenNode(n);
				turn.setChosenMove(Requests.Request.BodyCase.BUILDSETTLEMENT);

			}
			else if (n.getBuilding() != null)
			{
				turn.setChosenNode(n);
				turn.setChosenMove(Requests.Request.BodyCase.BUILDCITY);
			}

			if (checkBuild(turn))
			{
				moves.add(turn);
			}
		}

		// For each edge
		for (Edge e : getGame().getGrid().getEdgesAsList())
		{
			Turn turn = new Turn();
			turn.setChosenEdge(e);
			turn.setChosenMove(Requests.Request.BodyCase.BUILDROAD);
			if (checkBuildRoad(turn))
			{
				moves.add(turn);
			}
		}

		return moves;
	}

	/**
	 * Processes a turn and ascertains all possible moves
	 *
	 * @return list of possible choices from this proposed turn
	 */
	public List<Turn> getPossibleMoves()
	{

		List<Turn> possibilities = new ArrayList<>();

		if (getExpectedMoves().contains(Requests.Request.BodyCase.JOINLOBBY))
		{
			possibilities.add(new Turn(Requests.Request.BodyCase.JOINLOBBY));
			return possibilities;
		}
		if (getGame() == null)
			return possibilities;
		else
		{
			// Add initial possibilities
			possibilities.add(new Turn(Requests.Request.BodyCase.CHATMESSAGE));
			possibilities.addAll(getBuildingPossibilities());
		}

		if (getTurn().isInitialPhase()) { return possibilities; }

		// If the turn hasn't started, then the player can roll the dice
		if (getExpectedMoves().contains(Requests.Request.BodyCase.ROLLDICE))
		{
			possibilities.add(new Turn(Requests.Request.BodyCase.ROLLDICE));
		}
		// Check other possibilities
		if (checkBuyDevCard())
		{
			possibilities.add(new Turn(Requests.Request.BodyCase.BUYDEVCARD));
		}
		if (getExpectedMoves().isEmpty() && checkTurn())
		{
			// So as to not spam requests
			if (getTurn().getCurrentTrade() == null)
			{
				possibilities.add(new Turn(Requests.Request.BodyCase.INITIATETRADE));
				possibilities.add(new Turn(Requests.Request.BodyCase.ENDTURN));
			}
		}
		if (getExpectedMoves().contains(Requests.Request.BodyCase.DISCARDRESOURCES))
		{
			possibilities.add(new Turn(Requests.Request.BodyCase.DISCARDRESOURCES));
		}

		// Check owned dev cards
		for (DevelopmentCardType type : getGame().getPlayer().getDevelopmentCards().keySet())
		{
			Turn turn = new Turn();
			turn.setChosenMove(Requests.Request.BodyCase.PLAYDEVCARD);
			turn.setChosenCard(type);
			if (checkPlayDevCard(turn))
			{
				possibilities.add(turn);
			}
		}

		// For every owned resource type
		for (ResourceType r : ResourceType.values())
		{
			Turn turn = new Turn();
			turn.setChosenMove(Requests.Request.BodyCase.CHOOSERESOURCE);
			turn.setChosenResource(r);

			if (r.equals(ResourceType.Generic)) continue;
			if (checkChosenResource(turn))
			{
				possibilities.add(turn);
			}
		}

		// For every hex
		for (Hex h : getGame().getGrid().grid.values())
		{
			Turn turn = new Turn();
			turn.setChosenHex(h);
			turn.setChosenMove(Requests.Request.BodyCase.MOVEROBBER);

			if (checkHex(turn))
			{
				possibilities.add(turn);
			}
		}

		// For every node of the robber hex
		boolean valid = false;
		for (Node n : getGame().getGrid().getHexWithRobber().getNodes())
		{
			if (n.getBuilding() == null) continue;

			Colour c = n.getBuilding().getPlayerColour();
			Turn turn = new Turn();
			turn.setTarget(c);
			turn.setChosenMove(Requests.Request.BodyCase.SUBMITTARGETPLAYER);

			if (checkTarget(turn))
			{
				possibilities.add(turn);
				valid = true;
			}
		}

		// The chosen hex and resource from earlier doesn't yield any valid
		// target.
		// Randomly choose a move which the server will handle and toss away,
		// noticing the player
		// has no valid options anyway
		if (!valid && getExpectedMoves().contains(Requests.Request.BodyCase.SUBMITTARGETPLAYER))
		{
			Turn turn = new Turn();
			turn.setTarget(Colour.BLUE.equals(getGame().getPlayer().getColour()) ? Colour.ORANGE : Colour.BLUE);
			turn.setChosenMove(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
			possibilities.add(turn);
		}

		// For each response type
		for (Trade.Response resp : Trade.Response.values())
		{
			Turn turn = new Turn();
			turn.setChosenMove(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
			turn.setTradeResponse(resp);

			if (checkSendResponse(turn))
			{
				turn.setPlayerTrade(getTurn().getCurrentTrade().getTrade());
				possibilities.add(turn);
			}
		}
		return possibilities;
	}

	/**
	 * Checks that a MOVEROBBER move is valid
	 *
	 * @param turn the chosen hex
	 * @return whether or not the move is valid
	 */
	private boolean checkHex(Turn turn)
	{
		boolean val = false;
		Hex hex = turn.getChosenHex();

		// Check there is indeed a foreign settlement on one of the hexes nodes
		for (Node n : hex.getNodes())
		{
			if (n.getBuilding() != null && !n.getBuilding().getPlayerColour().equals(getGame().getPlayer().getColour())
					&& getGame().getPlayerResources(n.getBuilding().getPlayerColour()) > 0)
			{
				val = getExpectedMoves().contains(Requests.Request.BodyCase.MOVEROBBER);
			}
		}

		// Ensure this hex doesn't already have the robber, and that the move is
		// expected
		return checkTurn() && !hex.equals(getGame().getGrid().getHexWithRobber()) && val && isExpected(turn);
	}

	/**
	 * Checks that a MOVEROBBER move is valid
	 *
	 * @param turn the chosen resources
	 * @return whether or not the move is valid
	 */
	private boolean checkDiscard(Turn turn)
	{
		int sum = 0;
		Map<ResourceType, Integer> resources = turn.getChosenResources();
		for (ResourceType r : resources.keySet())
		{
			sum += resources.get(r);
		}
		client.log("Client Play", String.format("%d - %d for %s", getGame().getPlayer().getNumResources(), sum,
				getGame().getPlayer().getId().name()));

		// Ensure that a discard is expected, and that the discard can be
		// afforded and that it brings the user
		// into a safe position having 7 or less resources.
		return !getExpectedMoves().isEmpty() && isExpected(turn) && getGame().getPlayer().canAfford(resources)
				&& sum * 2 <= getGame().getPlayer().getNumResources();
	}

	/**
	 * Checks that a SUBMITTARGETPLAYER move is valid
	 *
	 * @param turn the target
	 * @return whether or not the move is valid
	 */
	private boolean checkTarget(Turn turn)
	{
		Colour target = turn.getTarget();

		// Ensure that a SUBMITTARGETPLAYER move is expected, and that the
		// player has resources
		return (checkTurn() && isExpected(turn)
				&& getExpectedMoves().contains(Requests.Request.BodyCase.SUBMITTARGETPLAYER)
				&& !target.equals(getGame().getPlayer().getColour()));
	}

	/**
	 * Checks that a CHOOSERESOURCE move is valid
	 *
	 * @param turn the resource in question
	 * @return whether or not the move is valid
	 */
	private boolean checkChosenResource(Turn turn)
	{
		ResourceType r = turn.getChosenResource();

		// Ensure that a CHOOSE RESOURCE move is expected, and that the bank
		// has the requested resource available
		return isExpected(turn) && getExpectedMoves().contains(Requests.Request.BodyCase.CHOOSERESOURCE)
				&& getGame().getBank().getAvailableResources().get(r) > 0;
	}

	/**
	 * Checks the player can play the given card
	 *
	 * @param turn the type of card wanting to be played
	 */
	private boolean checkPlayDevCard(Turn turn)
	{
		DevelopmentCardType type = turn.getChosenCard();
		if (type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University)) return false;

		// If player's turn and no other moves are expected, or it is the start
		// of their turn
		if (isExpected(turn))
		{
			Player player = getGame().getPlayer();

			// If the player owns the provided card
			if (player.getDevelopmentCards().containsKey(type) && player.getDevelopmentCards().get(type) > 0)
			{
				Bank bank = getGame().getBank();
				if(type.equals(DevelopmentCardType.RoadBuilding) && bank.getAvailableRoads(player.getColour()) < 2)
				{
					return false;
				}

				// If you didn't just buy this card / these cards
				Map<DevelopmentCardType, Integer> recentCards = player.getRecentBoughtDevCards();
				int num = recentCards.getOrDefault(type, 0);
				return player.getDevelopmentCards().get(type) > num;
			}
		}

		return false;
	}

	/**
	 * Checks to see if the player can buy a dev card
	 */
	private boolean checkBuyDevCard()
	{
		Turn turn = new Turn(Requests.Request.BodyCase.BUYDEVCARD);

		// If its the user's turn, they have no expected moves, and
		return checkTurn() && isExpected(turn) && getGame().getPlayer().canAfford(DevelopmentCardType.getCardCost());
	}

	/**
	 * Checks if a city or settlement can be built on the given nodeS
	 *
	 * @param turn the desired settlement / city location
	 */
	private boolean checkBuild(Turn turn)
	{
		Player p = getGame().getPlayer();
		Node node = turn.getChosenNode();
		Bank bank = getGame().getBank();
		return checkTurn() && (isExpected(turn) && (p.canBuildSettlement(node, bank) || p.canBuildCity(node, bank)));
	}

	/**
	 * @return whether or not it is the player's turn
	 */
	private boolean checkTurn()
	{
		return getGame().getCurrentPlayer() == getGame().getPlayer().getColour();
	}

	/**
	 * Checks to see if the player can build a road
	 *
	 * @param turn the desired road location
	 */
	private boolean checkBuildRoad(Turn turn)
	{
		Edge edge = turn.getChosenEdge();
		Bank bank = getGame().getBank();
		return checkTurn() && getGame().getPlayer().canBuildRoad(edge, bank) && isExpected(turn);
	}

	/**
	 * Checks to see if the player can submit a trade response
	 *
	 * @return whether or not this is a valid move
	 * @param turn the response
	 */
	private boolean checkSendResponse(Turn turn)
	{
		boolean val = false;
		Trade.Response response = turn.getTradeResponse();

		CurrentTrade t = getTurn().getCurrentTrade();
		if (t != null && getTurn().isTradePhase() && t.getTrade().getOther().getId() == getGame().getPlayer().getId())
		{
			Trade.WithPlayer trade = t.getTrade();
			Resource.Counts cost = trade.getOther().getId() == (getGame().getPlayer().getId()) ? trade.getWanting()
					: trade.getOffering();
			Map<ResourceType, Integer> costMap = getGame().processResources(cost);

			// If the move is expected, and the response is accept AND the
			// player can afford it.
			// OR if it is reject.
			val = isExpected(turn)
					&& ((response.equals(Trade.Response.ACCEPT) && getGame().getPlayer().canAfford(costMap))
							|| (response.equals(Trade.Response.REJECT)));
		}

		return val;
	}

	/**
	 * Checks whether or not the player can trade the following trade
	 *
	 * @param turn the initiate trade request
	 * @return whether or not the trade is valid
	 */
	public boolean checkInitiateTrade(Turn turn)
	{
		Map<ResourceType, Integer> cost = new HashMap<>(), wanting = new HashMap<>();

		Trade.Kind.Builder builder = Trade.Kind.newBuilder();
		Trade.Kind initiateTrade = turn.getPlayerTrade() != null ? builder.setPlayer(turn.getPlayerTrade()).build()
				: builder.setBank(turn.getBankTrade()).build();

		switch (initiateTrade.getTradeCase())
		{
		case BANK:
			cost = getGame().processResources(initiateTrade.getBank().getOffering());
			wanting = getGame().processResources(initiateTrade.getBank().getWanting());
			break;
		case PLAYER:
			cost = getGame().processResources(initiateTrade.getPlayer().getOffering());
			wanting = getGame().processResources(initiateTrade.getPlayer().getWanting());
			break;
		}

		// If both the player and the bank can afford the given trade
		return isExpected(turn) && getGame().getPlayer().canAfford(cost) && getGame().getBank().canAfford(wanting);
	}

	/**
	 * Checks that the given player is currently able to make a move of the
	 * given type
	 *
	 * @param turn the message
	 * @return true / false depending on legality of move
	 */
	private boolean isExpected(Turn turn)
	{
		Requests.Request.BodyCase type = turn.getChosenMove();
		if (type == null) return false;

		if (getExpectedMoves().contains(type))
		{
			return true;
		}
		else if(getExpectedMoves().contains(Requests.Request.BodyCase.ROLLDICE)
				&& type.equals(Requests.Request.BodyCase.PLAYDEVCARD))
		{
			return true;
		}

		// If the move is not expected
		else if (!getExpectedMoves().isEmpty()) return false;

		// If in trade phase and the given message isn't a trade
		if (getTurn().isTradePhase() && ((checkTurn() && !(type.equals(Requests.Request.BodyCase.INITIATETRADE)
				|| type.equals(Requests.Request.BodyCase.ENDTURN)))
				|| (!type.equals(Requests.Request.BodyCase.SUBMITTRADERESPONSE) && !checkTurn()))) { return false; }

		// If it's not your turn and there are no expected moves from you
		return !(!checkTurn() && getExpectedMoves().isEmpty());
	}

	/**
	 * Ensures the request is valid at this current stage of the game
	 *
	 * @param turn the request polled from the queue
	 * @return a boolean indicating success or not
	 */
	public boolean validateMsg(Turn turn)
	{
		boolean val = false;

		// If it is not the player's turn, the message type is unknown OR the
		// given request is NOT expected
		// send error and return false
		if (!isExpected(turn)) { return false; }

		switch (turn.getChosenMove())
		{
		case BUILDCITY:
		case BUILDSETTLEMENT:
			val = checkBuild(turn);
			break;
		case BUILDROAD:
			val = checkBuildRoad(turn);
			break;
		case BUYDEVCARD:
			val = checkBuyDevCard();
			break;
		case PLAYDEVCARD:
			val = checkPlayDevCard(turn);
			break;
		case SUBMITTARGETPLAYER:
			val = checkTarget(turn);
			break;
		case DISCARDRESOURCES:
			val = checkDiscard(turn);
			break;
		case MOVEROBBER:
			val = checkHex(turn);
			break;
		case CHOOSERESOURCE:
			val = checkChosenResource(turn);
			break;
		case SUBMITTRADERESPONSE:
			val = checkSendResponse(turn);
			break;
		case INITIATETRADE:
			val = checkInitiateTrade(turn);
			break;

		case JOINLOBBY:
			val = getExpectedMoves().contains(Requests.Request.BodyCase.JOINLOBBY);
			break;
		case ENDTURN:
			val = getExpectedMoves().isEmpty();
			break;

		case ROLLDICE:
			val = getExpectedMoves().contains(Requests.Request.BodyCase.ROLLDICE);
			break;

		case CHATMESSAGE:
			val = true;
			break;
		}

		return val;
	}

	private ClientGame getGame()
	{
		return client.getState();
	}

	private TurnState getTurn()
	{
		return client.getTurn();
	}

	private List<Requests.Request.BodyCase> getExpectedMoves()
	{
		return getTurn().getExpectedMoves();
	}
}
