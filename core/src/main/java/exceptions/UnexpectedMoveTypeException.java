package exceptions;

import intergroup.Messages.*;

@SuppressWarnings("serial")
public class UnexpectedMoveTypeException extends Exception
{
	private final Message msg;

	public UnexpectedMoveTypeException(Message msg)
	{
		this.msg = msg;
	}

	@Override
	public String getMessage()
	{
		String str = "";
		switch (msg.getTypeCase())
		{
		case EVENT:
			str = "Event";
			break;
		case REQUEST:
			str = "Request: " + msg.getRequest().getBodyCase().name();
			break;
		}

		return String.format("Unexpected Message Type: %s", str);
	}

	public Message getOriginalMessage()
	{
		return msg;
	}
}
