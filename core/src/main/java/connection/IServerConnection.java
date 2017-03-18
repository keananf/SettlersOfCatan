package connection;

import intergroup.Messages;

/**
 * Class representing a connection with a server
 * @author 140001596
 */
public interface IServerConnection
{
    Messages.Message getMessageFromServer();
    void sendMessageToServer(Messages.Message message);

    void shutDown();
}
