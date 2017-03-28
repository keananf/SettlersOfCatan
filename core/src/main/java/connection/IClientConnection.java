package connection;

import intergroup.Messages;

/**
 * Class representing a connection with a client
 * 
 * @author 140001596
 */
public interface IClientConnection
{
	void sendMessageToClient(Messages.Message message) throws Exception;

	Messages.Message getMessageFromClient() throws Exception;

	void shutDown();
}
