package connection;

import intergroup.Messages;

/**
 * Class representing a connection with a server
 * @author 140001596
 */
public interface IServerConnection
{
    Messages.Message getMessageFromServer() throws Exception;
    void sendMessageToServer(Messages.Message message) throws Exception;

    void shutDown();
}
