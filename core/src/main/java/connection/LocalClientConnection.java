package connection;

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

    public LocalClientConnection()
    {
        fromClient = new ConcurrentLinkedQueue<Messages.Message>();
    }

    @Override
    public void sendMessageToClient(Messages.Message message)
    {
        conn.fromServer.add(message);
    }

    @Override
    public Messages.Message getMessageFromClient()
    {
        // Block until message is received
        while(fromClient.isEmpty()) {}

        return fromClient.poll();
    }
}
