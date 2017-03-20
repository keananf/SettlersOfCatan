package AI;

import client.Client;
import client.ClientGame;
import client.TurnInProgress;
import connection.LocalClientConnection;
import connection.LocalServerConnection;
import connection.RemoteServerConnection;
import enums.AIDifficulty;
import grid.Edge;
import grid.Node;
import server.LocalServer;
import server.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class
AIClient extends Client
{
	private AICore AI;
	//remote client fields
	private String host;
	private RemoteServerConnection rConn;
	//Local client fields
	private LocalServerConnection lConn;
    private Server server;
    private Thread serverThread;

	public AIClient(AIDifficulty difficulty)
	{
		setUpConnection();
		
		switch(difficulty)
		{
			case VERYEASY:
				AI = new VeryEasyAI(this);
				break;
			case EASY:
				AI = new EasyAI(this);
				break;
			default:
				AI = new VeryEasyAI(this);
				break;
		}
	}
	
	public AIClient(AIDifficulty difficulty, String host)
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
	
	private void move()
	{
		ArrayList<MoveEntry> moves = AI.getMoves();
		
		ArrayList<MoveEntry> ranked = AI.rankMoves(moves);
		
		MoveEntry chosenMove = AI.selectMove(ranked);
		
		if(chosenMove.getMove() == null)
		{
			AI.getPlayer().cleanup();
		}
		
		TurnInProgress t = getTurn();
		
		t.setChosenMove(chosenMove.getMove());
		
		if(t.getChosenMove() == Requests.Request.BodyCase.PLAYDEVCARD)
		{
			t.setChosenCard(chosenMove.getCardType());
		}
		else if(t.getChosenMove() == Requests.Request.BodyCase.BUILDSETTLEMENT || t.getChosenMove() == Requests.Request.BodyCase.BUILDSETTLEMENT)
		{
			t.setChosenNode((Node) chosenMove.getElement());
		}
		else if(t.getChosenMove() == Requests.Request.BodyCase.BUILDROAD)
		{
			t.setChosenEdge((Edge) chosenMove.getElement()); 
		}
		
		sendTurn();
		
		
	}

}
