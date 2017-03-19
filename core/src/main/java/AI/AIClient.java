package AI;

import client.Client;
import client.ClientGame;
import connection.LocalClientConnection;
import connection.LocalServerConnection;
import connection.RemoteServerConnection;
import server.LocalServer;
import server.Server;

import java.io.IOException;
import java.net.Socket;

public class
AIClient extends Client
{
	//remote client fields
	private String host;
	private RemoteServerConnection rConn;
	//Local client fields
	private LocalServerConnection lConn;
    private Server server;
    private Thread serverThread;

	public AIClient()
	{
		setUpConnection();
	}
	
	public AIClient(String host)
	{
		this.host = host;
		
		setUpRemoteConnection();
	}
	
	private void setUpRemoteConnection() 
	{
		try
        {
            Socket socket = new Socket(host, PORT);
            rConn = new RemoteServerConnection(socket);
            setUp(rConn);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }	
	}

	@Override
	protected void setUpConnection() 
	{
		 lConn = new LocalServerConnection();
	        lConn.setConn(new LocalClientConnection(lConn));
	        server = new LocalServer(lConn.getConn());
	        serverThread = new Thread(server);
	        serverThread.start();


	        // TODO eliminate:
	        state = new ClientGame();
		
	}
	
	private void turn()
	{
		
	}

}
