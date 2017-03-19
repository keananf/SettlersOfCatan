package AI;

import client.Client;
import connection.LocalServerConnection;
import connection.RemoteServerConnection;
import server.Server;

public class AIClient extends Client 
{
	//remote client fields
	private String host;
	private RemoteServerConnection conn;
	//Local client fields
	private LocalServerConnection conn;
    private Server server;
    private Thread serverThread;

	public AIClient()
	{
		setUpConnection();
	}
	
	public AIClient(String host)
	{
		this.host = host;
	}
	
	@Override
	protected void setUpConnection() 
	{
		
		
	}

}
