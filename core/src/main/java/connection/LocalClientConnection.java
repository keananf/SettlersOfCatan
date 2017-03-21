package connection;

import com.badlogic.gdx.Gdx;
import intergroup.Messages;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class representing a local connection with a client
 * @author 140001596
 */
public class LocalClientConnection implements IClientConnection
{
    private LocalServerConnection conn;
    protected ConcurrentLinkedQueue<Messages.Message> fromClient;

    public LocalClientConnection(LocalServerConnection conn)
    {
        this.conn = conn;
        fromClient = new ConcurrentLinkedQueue<Messages.Message>();
    }

    @Override
    public void sendMessageToClient(Messages.Message message)
    {
        if(conn != null)
        {
            conn.fromServer.add(message);
            log("Server Conn", String.format("Sent. %s", message.getEvent().getTypeCase().name()));
        }
    }

    @Override
    public Messages.Message getMessageFromClient()
    {
        if(fromClient == null) return null;

        // Block until message is received
        while(fromClient.isEmpty()) {}
        return fromClient.poll();
    }

    @Override
    public void shutDown()
    {
        conn = null;
        fromClient = null;
    }
    
    /**
     * Logs the message depending on whether or not this is a local or remote server
     * @param tag the tag (for Gdx)
     * @param msg the msg to log
     */
    public void log(String tag, String msg)
    {
        if(Gdx.app == null)
        {
            System.out.println(msg);
        }
        else Gdx.app.log(tag, msg);
    }
}
