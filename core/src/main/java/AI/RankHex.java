package AI;

import client.ClientGame;
import enums.Colour;
import game.build.Building;
import grid.Hex;
import grid.Node;

/**
 * Created by 140002949 on 25/03/17.
 */
public class RankHex {

    Hex hex;
    int ranking;
    ClientGame game;

    public RankHex(Hex hex, ClientGame game){
        this.hex = hex;
        this.game = game;
    }

    public void rank(){
        rankRichest();
        rankProbability();
    }

    private void rankProbability() {
        int likelihood = Math.abs(hex.getChit()-7); //difference between diceRoll on hex and 7
        int rank = 7 - likelihood;                  //the lower the difference, the higher the rank
        ranking += rank;
    }

    private void rankRichest(){
        Building build = null;
        for (Node node: hex.getNodes()) {

            if((build = node.getSettlement()) != null){
                Colour c = build.getPlayerColour();
                ranking += game.getPlayerResources(c);
            }

        }
    }

    public int getRanking(){
        return ranking;
    }
}
