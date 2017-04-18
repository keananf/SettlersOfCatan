package AI;

import client.Turn;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.trade.Trade;

import java.util.List;

interface IAI
{
	/**
	 * Rank an individual turn based upon its assigned values
	 * 
	 * @param turn
	 * @return
	 */
	int rankMove(Turn turn);

	/**
	 * Rank the list of all possible moves for this turn
	 * 
	 * @param moves the list of move possibilities
	 * @return the list of 'optimal' moves that share the highest rank.
	 */
	List<Turn> rankMoves(List<Turn> moves);

	/**
	 * Chooses the turn with the highest rank
	 * 
	 * @param optimalMoves the list of turns that share the highest rank.
	 * @return the turn with the highest rank
	 */
	Turn selectMove(List<Turn> optimalMoves);

	/**
	 * Top level ai function for getting moves, ranking them, choosing one,
	 * updating the client's Turn object, and sending it to the client's
	 * TurnProcessor to send to the server. Every ai function must be called
	 * from within the call hierarchy that his function is the root of.
	 */
	boolean performMove();

	/**
	 * Given this being a valid move, analyse if this is good based on the game
	 * state.
	 *
	 * if the player is close to largestArmy, it perhaps yields a higher rank?
	 * 
	 * @return a rank for buying a dev card
	 */
	int rankBuyDevCard();

	/**
	 * Given this valid edge placement, analyse if this is a good road placement
	 * based on the game state.
	 * 
	 * @param chosenEdge the given edge
	 * @return a rank for this road placement
	 */
	int rankNewRoad(Edge chosenEdge);

	/**
	 * Given this valid node placement, analyse if this is a good settlement
	 * placement based on the game state.
	 * 
	 * @param chosenNode the given node
	 * @return a rank for this settlement placement
	 */
	int rankNewSettlement(Node chosenNode);

	/**
	 * Given this valid node placement, analyse if this is a good city placement
	 * based on the game state.
	 * 
	 * @param chosenNode the given node
	 * @return a rank for this city placement
	 */
	int rankNewCity(Node chosenNode);

	/**
	 * Given this valid hex placement, analyse if this is a good robber
	 * placement based on the game state.
	 *
	 * Will it give higher ranks to hexes which will penalise wealthy players?
	 * Higher rank based on chit?
	 * 
	 * @param chosenHex the given hex
	 * @return a rank for this robber placement
	 */
	int rankNewRobberLocation(Hex chosenHex);

	/**
	 * Given this valid dev card, analyse if this is a good play based on the
	 * game state.
	 *
	 * Will it give higher rank if you're close to largestArmy?
	 * 
	 * @param chosenCard the given card
	 * @return a rank for this robber placement
	 */
	int rankPlayDevCard(DevelopmentCardType chosenCard);

	/**
	 * Given this valid trade possibility, analyse if this is a good trade based
	 * on the game state.
	 *
	 * Likewise, assign a good player or bank trade to this turn object.
	 * 
	 * @param turn the given trade turn, with no trade information
	 * @return a rank for the assigned trade
	 */
	int rankInitiateTrade(Turn turn);

	/**
	 * If a discard request has gotten here, it is necessary. Assign the
	 * resources in turn.
	 *
	 * @param turn the given trade turn, with no trade information
	 * @return an arbitrary rank (as the remaining list of moves will be empty).
	 */
	int rankDiscard(Turn turn);

	/**
	 * If a submit target player request has gotten here, it is necessary.
	 *
	 * Rank based on which player the ai wants to harass.
	 * 
	 * @param target the given player to target
	 * @return a rank based on if stealing from this player is a good move.
	 */
	int rankTargetPlayer(Colour target);

	/**
	 * If a choose resource request has gotten here, it is necessary.
	 *
	 * Rank based on which resource the player has the least of (or least access
	 * to?).
	 * 
	 * @param chosenResource the chosen Resource to request
	 * @return a rank based on if this resource is desired.
	 */
	int rankChosenResource(ResourceType chosenResource);

	/**
	 * If this method is called, a trade response is required. Rank the
	 * potential response to this trade.
	 *
	 * The list of potential moves this turn will ONLY be two trade responses:
	 * this and another with the opposite response.
	 * 
	 * @param tradeResponse the potential trade response
	 * @param trade the trade associated with this potential trade response
	 * @return a rank ranking this trade response
	 */
	int rankTradeResponse(Trade.Response tradeResponse, Trade.WithPlayer trade);
	
	/**
	 * This method will set a baseline rank that other move ranks must
	 * surpass in order to be performed.
	 * @return a rank ranking ending the turn
	 */
	int rankEndTurn();
}
