package server;

import connection.LocalClientConnection;
import enums.Colour;
import exceptions.GameFullException;
import game.players.Player;

/**
 * Class representing a locally hosted server
 * @author 140001596
 */
public class LocalServer extends Server
{
    private Player localPlayer;

    public LocalServer(LocalClientConnection connection)
    {
        super();
        Colour c = joinGame(connection);
        localPlayer = game.getPlayer(c);
    }

    /**
     * Adds the given local connection to the game
     * @param connection
     */
    private Colour joinGame(LocalClientConnection connection)
    {
        Colour c = null;
        try
        {
            c = game.joinGame();
        }
        catch (GameFullException e) {}
        connections.put(c, new ListenerThread(connection, c,  this));
        System.out.println(String.format("Player %d connected", numConnections));
        numConnections++;

        return c;
    }

    /**
     * Adds the given number of local AI players
     * @param num the number of AIs to add
     */
    private void addAIs(int num) throws GameFullException
    {
        if(ServerGame.NUM_PLAYERS == numConnections)
        {
            throw new GameFullException();
        }

        //TODO
        /*
        LocalAiClient ai = new LocalAiClient();

         */
    }
}
