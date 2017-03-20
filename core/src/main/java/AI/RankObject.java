package AI;

import grid.Hex;
import grid.Node;

import java.util.ArrayList;

/**
 * Created by 140002949 on 19/03/17.
 */

public class RankObject {
    Node node;
    int ranking = 0;

    ArrayList<Hex> surroundingHexes  = new ArrayList<>();

    public RankObject(Node node){
        this.node = node;
        surroundingHexes = (ArrayList<Hex>) node.getHexes();
    }

    public void rank(){
        addHexesRank();
        addResourceRank();
        addLikelihoodRank();
    }

    public void addHexesRank(){
        ranking += surroundingHexes.size();
    }

    public void addResourceRank(){
        for(Hex hex:surroundingHexes){
            //priority of resource, with a focus on building settlements and roads
            switch (hex.getResource()){
                case Generic:
                    ranking--;
                    break;
                case Grain:
                    ranking+=4;
                    break;
                case Brick:
                    ranking+=3;
                    break;
                case Lumber:
                    ranking+=3;
                    break;
                case Wool:
                    ranking+=2;
                    break;
                case Ore:
                    ranking+=1;
                    break;
            }
        }
    }

    public void addLikelihoodRank(){
        for (Hex hex: surroundingHexes) {
            int likelihood = Math.abs(hex.getChit()-7); //difference between diceRoll on hex and 7
            int rank = 7 - likelihood;                  //the lower the difference, the higher the rank
            ranking += rank;
        }
    }

    public int getRanking(){
        return ranking;
    }

    public Node getNodeOnBoard(ArrayList<Node> nodes){
        for (Node n:nodes) {
            if (this.node.equals(n)){
                return n;
            }
        }
        return null;
    }
}
