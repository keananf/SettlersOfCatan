package game.players;

import enums.Colour;
import exceptions.CannotBuildRoadException;
import exceptions.RoadExistsException;
import game.build.Road;
import grid.Edge;

import java.util.ArrayList;
import java.util.List;

public class ClientPlayer extends Player
{
	public ClientPlayer(Colour colour, String username)
	{
		super(colour, username);
	}

	/**
	 * Builds the road for this player (client-side)
	 * 
	 * @param edge the edge to build the road on
	 */
	public Road addRoad(Edge edge) throws RoadExistsException, CannotBuildRoadException
	{
		List<Integer> listsAddedTo = new ArrayList<Integer>();
		Road r = new Road(edge, colour);

		// Road already here. Cannot build
		if (edge.getRoad() != null) throw new RoadExistsException(r);

		// Check the location is valid for building and that the player can
		// afford it
		if (canBuildRoad(edge))
		{
			edge.setRoad(r);

			// Find out where this road is connected
			checkRoadsAndAdd(r, listsAddedTo);

			// If not connected to any other roads
			if (listsAddedTo.size() == 0)
			{
				List<Road> newList = new ArrayList<Road>();
				newList.add(r);
				roads.add(newList);
			}

			// merge lists if necessary
			else if (listsAddedTo.size() >= 1) mergeRoads(r, listsAddedTo);

			return r;
		}
		else
			throw new CannotBuildRoadException(r);
	}
}