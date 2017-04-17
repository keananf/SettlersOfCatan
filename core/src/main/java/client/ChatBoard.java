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
		chatMessages = new ArrayList<>();
	}

	/**
	 * @return the contents of the chatboard
	 */
	public List<ChatMessage> getMessages()
	{
		List<ChatMessage> reverse = new ArrayList<ChatMessage>();
		for(int i = chatMessages.size() - 1; i >= 0; i--)
		{
			reverse.add(chatMessages.get(i));
		}
		return reverse;
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
	public class ChatMessage
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

		public String getMessage()
		{
			return String.format("%s: %s", senderColour.name(), contents);
		}
	}
}
