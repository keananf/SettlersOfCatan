package AI;

import client.Turn;

/**
 * Created by 140002949 on 19/03/17.
 */
public class EasyAI extends AICore
{

    public EasyAI(AIClient client)
    {
        super(client);
    }

    @Override
    public int rankMove(Turn turn)
    {
        // Switch on turn type and rank move
        switch(turn.getChosenMove())
        {
            case BUYDEVCARD:
                // return rankBuyDevCard();
            case BUILDROAD:
                // return rankNewRoad(turn.getChosenRoad());
            case BUILDSETTLEMENT:
                // return rankNewSettlement(turn.getChosenNode());
            case BUILDCITY:
                // return rankNewCity(turn.getChosenNode());
            case MOVEROBBER:
                // return rankNewRobberLocation(turn.getChosenHex());
            case PLAYDEVCARD:
                // return rankPlayDevCard(turn.getChosenCard());
            case INITIATETRADE:
                // return rankInitiateTrade(); TODO ensure this field set
            case SUBMITTRADERESPONSE:
                // return rankNewSettlement(turn.getTradeResponse, turn.getCurrentTrade());
            case DISCARDRESOURCES:
                // return rankDiscard(turn.getChosenResources()); TODO ensure this field set
            case SUBMITTARGETPLAYER:
                // return rankTargetPlayer(turn.getTarget());
            case CHOOSERESOURCE:
                // return rankChosenResource(turn.getChosenResource);

            // Rank does not apply here
            case CHATMESSAGE:
            case ROLLDICE:
            case ENDTURN:
            case JOINLOBBY:
            case BODY_NOT_SET:
            default:
                break;
        }

        return 0;
    }

}
