package AI;

import client.ClientGame;
import grid.Edge;

/**
 * Created by 140002949 on 23/03/17.
 */
public class RankEdge {


    ClientGame game;
    Edge edge;

    public RankEdge(Edge edge, ClientGame game){
        this.edge = edge;
        this.game = game;
    }
}
