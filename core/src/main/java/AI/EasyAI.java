package AI;

import enums.Difficulty;
import grid.Node;

import java.util.ArrayList;

/**
 * Created by 140002949 on 19/03/17.
 */
public class EasyAI extends AICore {

    public EasyAI(Difficulty difficulty, AIClient client) {
        super(difficulty, client);
    }

    public Node getInitialSettlementNode(){
        Node node = null;

        ArrayList<Node> nodes = (ArrayList<Node>) game.getGrid().getNodesAsList();
        ArrayList<RankObject> rankedNodes = new ArrayList<>();

        for(Node n : nodes){

            RankObject rankObject = new RankObject(n);
            rankObject.rank();

            rankedNodes.add(rankObject.getRanking(), rankObject);
        }
        //gets the rankobject with the highest ranking and returns the node it has been given
        return rankedNodes.get(rankedNodes.size()-1).getNodeOnBoard(nodes);
    }
    //TODO: work on heuristic ranking of moves and heuristic placement of robber

}
