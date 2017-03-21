package connection;

import client.Client;
import intergroup.Messages;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class representing a local server connection
 * @author 140001596
 */
public class LocalServerConnection implements IServerConnection
{
    private final Client client;
    private LocalClientConnection conn;
    protected ConcurrentLinkedQueue<Messages.Message> fromServer;

    public LocalServerConnection(Client client)
    {
        this.client = client;
        fromServer = new ConcurrentLinkedQueue<Messages.Message>();
    }

    public void setConn(LocalClientConnection conn)
    {
        this.conn = conn;
    }

    @Override
    public Messages.Message getMessageFromServer()
    {
        // Block until message is in
        while(fromServer.isEmpty()) {}

        Messages.Message m = fromServer.poll();
        client.log("Client Conn", String.format("Received %s", m.getEvent().getTypeCase().name()));
        return m;
    }

    @Override
    public void sendMessageToServer(Messages.Message message)
    {
        conn.fromClient.add(message);
    }

    @Override
    public void shutDown()
    {
        conn = null;
        fromServer = null;
    }

    public LocalClientConnection getConn()
    {
        return conn;
    }
}
