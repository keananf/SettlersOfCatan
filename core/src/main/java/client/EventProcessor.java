package client;

import intergroup.Events.Event;
import intergroup.Messages.Message;
import server.Logger;

import java.io.IOException;
import java.net.Socket;

/**
 * Class which continuously listens for updates from the server Created by
 * 140001596
 */
public class EventProcessor implements Runnable
{
	private ClientGame game;
	private Thread thread;
	private Socket socket;
	private Logger logger;

	public EventProcessor(Socket socket, ClientGame game)
	{
		this.socket = socket;
		logger = new Logger();
		this.game = game;

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        // Continuously wait for new messages from the server
        while(!game.isOver())
        {
            try
            {
                processMessage();
            }
            catch(IOException e)
            {
                // Fatal error
                break;
            }
            catch (Exception e)
            {
                // Error. Invalid event.
                // TODO request state from server? Or fail?
                e.printStackTrace();
            }

        }
    }

    /**
     * Processes the event received from the server and updates the game state
     * @param ev the event
     */
    private void processEvent(Event ev) throws Exception
    {
        // Switch on type of event
        switch(ev.getTypeCase())
		{
			case GAMEWON:
				game.setGameOver();
				break;
			case TURNENDED:
				game.setCurrentPlayer(game.getNextPlayer());
				break;
			case CITYBUILT:
				game.processNewCity(ev.getCityBuilt(),ev.getInstigator(),false);
				break;
			case SETTLEMENTBUILT:
				game.processNewSettlement(ev.getSettlementBuilt(),ev.getInstigator(),false);
				break;
			case ROADBUILT:
				game.processRoad(ev.getRoadBuilt(),ev.getInstigator());
				break;
			case ROLLED:
				game.processDice(ev.getRolled().getA()+ev.getRolled().getB(), ev.getRolled().getResourceAllocationList());
				break;
			case ROBBERMOVED:
				game.moveRobber(ev.getRobberMoved());
				break;
			case DEVCARDBOUGHT:
				game.recordDevCard(ev.getDevCardBought(),ev.getInstigator());
				break;
			case DEVCARDPLAYED:
				game.processPlayedDevCard(ev.getDevCardPlayed(),ev.getInstigator());
				break;
			case BEGINGAME:
				game.setBoard(ev.getBeginGame());
				break;
			case CHATMESSAGE:
				game.writeMessage(ev.getChatMessage(), ev.getInstigator());
				break;
			case BANKTRADE:
				game.processBankTrade(ev.getBankTrade(), ev.getInstigator());
				break;
			case PLAYERTRADE:
				game.processPlayerTrade(ev.getPlayerTrade(), ev.getInstigator());
				break;
			case LOBBYUPDATE:
				game.processPlayers(ev.getLobbyUpdate(), ev.getInstigator());
				break;
			case CARDSDISCARDED:
				game.processDiscard(ev.getCardsDiscarded(), ev.getInstigator());
				break;
			case MONOPOLYRESOLUTION:
				game.processMonopoly(ev.getMonopolyResolution(), ev.getInstigator());
				break;
			case RESOURCECHOSEN:
				game.processResourceChosen(ev.getResourceChosen(), ev.getInstigator());
				break;
			case RESOURCESTOLEN:
				game.processResourcesStolen(ev.getResourceStolen(), ev.getInstigator());
				break;
			case ERROR:
				// TODO display error?
				break;
		}
    }
	/**
	 * Process the next message.
	 * 
	 * @throws IOException
	 */
	private void processMessage() throws Exception
	{
		Message msg = Message.parseFrom(socket.getInputStream());
		logger.logReceivedMessage(msg);

		// switch on message type
		switch (msg.getTypeCase())
		{

			// Extract and process event
			case EVENT:
				processEvent(msg.getEvent());
				break;
		}
	}
}
