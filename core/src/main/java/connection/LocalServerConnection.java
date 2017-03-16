package connection;

import intergroup.Messages;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class representing a local server connection
 * @author 140001596
 */
public class LocalServerConnection implements IServerConnection
{
    private LocalClientConnection conn;
    protected ConcurrentLinkedQueue<Messages.Message> fromServer;

    public LocalServerConnection()
    {
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

        return fromServer.poll();
    }

    @Override
    public void sendMessageToServer(Messages.Message message)
    {
        conn.fromClient.add(message);
    }

    public LocalClientConnection getConn()
    {
        return conn;
    }
}
