package client;

import enums.DevelopmentCardType;
import game.players.Player;
import grid.BoardElement;
import grid.Edge;
import grid.Node;
import intergroup.Requests;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which determines move possibilities based upon a game state
 * @author 140001596
 */
public class MoveProcessor
{
    private ClientGame game;

    public MoveProcessor(ClientGame game)
    {
        this.game = game;
    }

    /**
     * Retrieve a list of all building possibilities
     * @return
     */
    public List<BoardElement> getBuildingPossibilities()
    {
        List<BoardElement> moves = new ArrayList<BoardElement>();

        // This player
        Player p = game.getPlayer();

        // For each node
        for(Node n : game.getGrid().nodes.values())
        {
            if(p.canBuildSettlement(n))
            {
                moves.add(n);
            }
            else if(p.canBuildCity(n))
            {
                moves.add(n);
            }
        }

        // For each edge
        for(Edge e : game.getGrid().getEdgesAsList())
        {
            if(p.canBuildRoad(e))
            {
                moves.add(e);
            }
        }

        return moves;
    }

    /**
     * Processes a turn
     * @param turn the turn
     * @return list of possible choices from this proposed turn
     */
    public List<Requests.Request.BodyCase> processTurn(Turn turn)
    {
        List<Requests.Request.BodyCase> possibilities = new ArrayList<Requests.Request.BodyCase>();

        // Check if the recently selected move
        if(checkBuyDevCard())
        {
            possibilities.add(Requests.Request.BodyCase.BUYDEVCARD);
        }
        if(checkPlayDevCard(turn.getChosenCard()))
        {
            possibilities.add(Requests.Request.BodyCase.PLAYDEVCARD);
        }
        if(checkBuildRoad(turn.getChosenEdge()))
        {
            possibilities.add(Requests.Request.BodyCase.BUILDROAD);
        }
        if(checkBuild(turn.getChosenNode()))
        {
            if(turn.getChosenNode().getSettlement() != null)
            {
                possibilities.add(Requests.Request.BodyCase.BUILDCITY);
            }
            else possibilities.add(Requests.Request.BodyCase.BUILDSETTLEMENT);
        }

        return possibilities;
    }

    /**
     * Checks the player can play the given card
     * @param type the type of card wanting to be played
     */
    private boolean checkPlayDevCard(DevelopmentCardType type)
    {
        if(type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University))
            return false;

        // If player's turn
        if(checkTurn())
        {
            Player player = game.getPlayer();

            // If the player owns the provided card
            if(player.getDevelopmentCards().containsKey(type) && player.getDevelopmentCards().get(type) > 0)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if the player can buy a dev card
     */
    private boolean checkBuyDevCard()
    {
        return checkTurn() && game.getPlayer().canAfford(DevelopmentCardType.getCardCost());
    }

    /**
     * Checks if a city or settlement can be built on the given nodeS
     * @param node the desired settlement / city location
     */
    private boolean checkBuild(Node node)
    {
        Player p = game.getPlayer();
        return checkTurn() && (p.canBuildSettlement(node) || p.canBuildCity(node));
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
     * @param edge the desired road location
     */
    private boolean checkBuildRoad(Edge edge)
    {
        return checkTurn() && game.getPlayer().canBuildRoad(edge);
    }
}
