package server;

import com.badlogic.gdx.Gdx;
import intergroup.Events;
import intergroup.Messages.Message;

/**
 * Created by 140001596 on 1/10/17.
 */
public class Logger
{
	public void logReceivedMessage(Message msg)
	{
		if(msg == null) return;

		String str = String.format("RECEIVED: Message of type %s.\n", msg.getTypeCase().name());

		switch (msg.getTypeCase())
		{
			case REQUEST:
				str = String.format(str + "Type of Request: %s\n", msg.getRequest().getBodyCase().name());
				break;

			case EVENT:
				Events.Event ev = msg.getEvent();
				str = String.format(str + "Type of Event: %s. From player: %s\n", ev.getTypeCase().name(), ev.getInstigator().getId().name());
				break;
		}

		if(Gdx.app != null)
			Gdx.app.log("Server", str);
	}
}
