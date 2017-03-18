package client;

import enums.Colour;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for
 *
 * @author 140001596
 */
public class ChatBoard
{
	private List<ChatMessage> chatMessages;

	public ChatBoard()
	{
		chatMessages = new ArrayList<ChatMessage>();
	}

	/**
	 * @return the contents of the chatboard
	 */
	public List<ChatMessage> getMessages()
	{
		return chatMessages;
	}

	/**
	 * Writes the given message from the given player to the chatboard
	 *
	 * @param contents the message
	 * @param senderName the sender's username (for display purposes)
	 * @param senderColour
	 */
	public void writeMessage(String contents, String senderName, Colour senderColour)
	{
		chatMessages.add(new ChatMessage(contents, new Date(System.currentTimeMillis()), senderName, senderColour));
	}

	/**
	 * Class for a single message
	 *
	 * @author 140001596
	 */
	private class ChatMessage
	{
		private String contents;
		private Date date;
		private String senderName;
		private Colour senderColour;

		private ChatMessage(String contents, Date date, String senderName, Colour senderColour)
		{
			this.contents = contents;
			this.date = date;
			this.senderName = senderName;
			this.senderColour = senderColour;
		}
	}
}
