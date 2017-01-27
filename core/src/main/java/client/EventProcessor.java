package client;

import enums.Colour;
import protocol.EventProtos.Event;
import protocol.MessageProtos.Message;
import protocol.RequestProtos;
import server.Logger;

import java.io.IOException;
import java.net.Socket;

/**
 * Class which continuously listens for updates from the server
 * Created by 140001596
 */
public class EventProcessor implements Runnable
{
    private ClientGame game;
    private Thread thread;
    private Socket socket;
    private Logger logger;

    public EventProcessor(Socket socket, ClientGame game)
    {
        this.socket = socket;
        logger = new Logger();
        this.game = game;

        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run()
    {
        try
        {
            // Continuously wait for new messages from the server
            while(!game.isOver())
            {
                processMessage();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Process the next message.
     * @throws IOException
     */
    private void processMessage() throws IOException
    {
        Message msg = Message.parseFrom(socket.getInputStream());
        logger.logReceivedMessage(msg);

        // switch on message type
        switch (msg.getTypeCase())
        {
            // User request
            case REQUEST:
                processRequest(msg.getRequest(), Colour.fromProto(msg.getPlayerColour()));
                break;

            // Extract and process event
            case EVENT:
                processEvent(msg.getEvent(), Colour.fromProto(msg.getPlayerColour()));
                break;

            // Process new board, otherwise ignore
            case RESPONSE:
                if(msg.getResponse().hasCurrentBoardResponse())
                {
                    game.setBoard(msg.getResponse().getCurrentBoardResponse().getBoard());
                }
                break;
        }
    }

    /**
     * Process the request relayed by the server
     * @param req the request
     * @param colour the player who made the request
     */
    private void processRequest(RequestProtos.Request req, Colour colour)
    {
        // Switch on type of event
        switch(req.getTypeCase())
        {
            case TRADEREQUEST:
                break;

            // Error.
            default:
                break;
        }
    }

    /**
     * Processes the event received from the server and updates the game state
     * @param ev the event
     * @param colour the colour pertaining to the event
     */
    private void processEvent(Event ev, Colour colour)
    {
        // Switch on type of event
        switch(ev.getTypeCase())
        {
            case GAMEOVER:
                game.setGameOver();
                break;
            case NEWTURN:
                game.setTurn(Colour.fromProto(ev.getNewTurn()));
                break;
            case NEWBUILDING:
                game.processNewBuilding(ev.getNewBuilding());
                break;
            case DICEROLL:
                game.setDice(ev.getDiceRoll().getDice());
                break;
            case NEWROAD:
                game.processRoad(ev.getNewRoad());
                break;
            case ROBBERMOVE:
                game.moveRobber(ev.getRobberMove());
                break;
            case BOUGHTDEVCARD:
                game.recordDevCard(ev.getBoughtDevCard());
                break;
            //TODO complete
        }
    }
}
