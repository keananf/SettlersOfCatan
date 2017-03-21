package connection;

import intergroup.Messages;
import server.Server;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class representing a local connection with a client
 * @author 140001596
 */
public class LocalClientConnection implements IClientConnection
{
    private Server server;
    private LocalServerConnection conn;
    protected ConcurrentLinkedQueue<Messages.Message> fromClient;

    public LocalClientConnection(LocalServerConnection conn)
    {
        this.conn = conn;
        fromClient = new ConcurrentLinkedQueue<Messages.Message>();
    }

    public void setServer(Server server)
    {
        this.server = server;
    }

    @Override
    public void sendMessageToClient(Messages.Message message)
    {
        if(conn != null || this.server == null)
        {
            conn.fromServer.add(message);
            server.log("Server Conn", String.format("Sent. %s", message.getEvent().getTypeCase().name()));
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
}
