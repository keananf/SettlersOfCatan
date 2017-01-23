package server;

import enums.Colour;
import protocol.EnumProtos;
import protocol.MessageProtos.*;
import protocol.RequestProtos;
import protocol.ResponseProtos.*;

import java.io.IOException;
import java.net.Socket;

/**
 * Class which simply listens to a socket.
 * The successfully received message is added to a ConcurrentLinkedQueue.
 * Created by 140001596.
 */
public class ListenerThread implements Runnable
{
    protected Socket socket;
    private Colour colour;
    private Server server;

    public ListenerThread(Socket socket, Colour colour, Server server)
    {
        this.socket = socket;
        this.colour = colour;
        this.server = server;
    }

    @Override
    public void run()
    {
        // Continually poll for new messages
        try
        {
            receiveMoves();
        }
        catch (IOException e)
        {
            // TODO replace with AI
            e.printStackTrace();
        }
    }


    /**
     * Listens for moves from the current player
     * @return the bytes received from the current player
     * @throws IOException
     */
    private void receiveMoves() throws IOException
    {
        // Receive and process moves until the end one is received
        while(true)
        {
            // Parse message and add to queue
            Message msg = Message.parseFrom(socket.getInputStream());
            server.addMessageToProcess(msg);

        }
    }

    /**
     * If an unknown or invalid message is received, then this message sends an error back
     * @param originalMsg the original message
     */
    protected void sendError(Message originalMsg) throws IOException
    {
        // Set up result message
        Response.Builder response = Response.newBuilder();
        SuccessFailResponse.Builder result = SuccessFailResponse.newBuilder();
        result.setResult(EnumProtos.ResultProto.FAILURE);
        result.setReason("Invalid message type");

        // Set up wrapper response object
        response.setSuccessFailResponse(result);
        response.build().writeTo(socket.getOutputStream());

    }

    /**
     * Sends response out to the client
     * @param response the response message from the last action
     * @throws IOException
     */
    protected void sendResponse(Response response) throws IOException
    {
        response.writeTo(socket.getOutputStream());
        socket.getOutputStream().flush();
    }

    /**
     * Sends the message out to the client
     * @param msg the message
     * @throws IOException
     */
    public void sendMessage(Message msg) throws IOException
    {
        // Serialise and Send
        msg.writeTo(socket.getOutputStream());
        socket.getOutputStream().flush();
    }
}
