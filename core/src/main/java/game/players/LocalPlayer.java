package game.players;

import grid.Edge;
import enums.Colour;
import exceptions.CannotBuildRoadException;
import exceptions.RoadExistsException;
import game.build.Road;

import java.util.ArrayList;
import java.util.List;

public class LocalPlayer extends Player
{
    public LocalPlayer(Colour colour, String username)
    {
        super(colour, username);
    }

    /**
     * Builds the road for this player (client-side)
     * @param edge the edge to build the road on
     */
    public Road addRoad(Edge edge) throws RoadExistsException, CannotBuildRoadException
    {
        boolean valid = false;
        List<Integer> listsAddedTo = new ArrayList<Integer>();
        Road r = new Road(edge, colour);

        if(edge.getRoad() != null)
        {
            throw new RoadExistsException(r);
        }

        // Find out where this road is connected
        valid = checkRoadsAndAdd(r, listsAddedTo);

        // Check the location is valid for building and that the player can afford it
        if(r.getEdge().hasSettlement() || valid)
        {
            edge.setRoad(r);

            // If not connected to any other roads
            if (listsAddedTo.size() == 0)
            {
                List<Road> newList = new ArrayList<Road>();
                newList.add(r);
                roads.add(newList);
            }

            // merge lists if necessary
            else if(listsAddedTo.size() > 1)
                mergeRoads(r, listsAddedTo);
        }
        else throw new CannotBuildRoadException(r);

        return r;
    }
}
