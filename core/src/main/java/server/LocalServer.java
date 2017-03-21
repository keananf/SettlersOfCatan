package server;

import AI.LocalAIClientOnServer;
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
        ServerGame.NUM_PLAYERS = 4;
        Colour c = joinGame(connection);
        localPlayer = game.getPlayer(c);
        addAIs(3);
    }

    public LocalServer()
    {
        super();
        ServerGame.NUM_PLAYERS = 4;
        addAIs(4);
        localPlayer = game.getPlayers().get(0);
    }

    public static void main(String[] args)
    {
        LocalServer s = new LocalServer();
        Thread t = new Thread(s);
        t.start();
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

        connection.setServer(this);
        connections.put(c, new ListenerThread(connection, c,  this));
        log("Server Setup", String.format("Player %d connected", numConnections));
        numConnections++;

        return c;
    }

    /**
     * Adds the given number of local AI players
     * @param num the number of AIs to add
     */
    private void addAIs(int num)
    {
        // Don't add more if game is full
        if(ServerGame.NUM_PLAYERS == numConnections)
        {
            return;
        }

        // Add 'num' AIs
        for(int i = 0; i < num; i++)
        {
            LocalAIClientOnServer ai = new LocalAIClientOnServer();
            LocalClientConnection conn = ai.getConn().getConn();
            joinGame(conn);
        }
        log("Server SetUp", String.format("Number of AIs: %d. Connections: %d", num, numConnections));
    }
}
