package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.SaneTextField;
import client.ChatBoard;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import intergroup.Requests;

import java.util.Collections;
import java.util.List;

class ChatDialog extends SaneDialog
{
	ChatDialog(final Client client)
	{
		super("Chat");

		final ChatBoard chatBoard = client.getState().getChatBoard();
		final VerticalGroup verticalGroup = new VerticalGroup();
		verticalGroup.columnAlign(Align.bottomLeft);
		final ScrollPane scrollPane = new ScrollPane(verticalGroup);
		getContentTable().bottom().left();
		getContentTable().add(scrollPane);

		List<ChatBoard.ChatMessage> messages = chatBoard.getMessages().subList(0, chatBoard.getMessages().size());
		Collections.reverse(messages);
		for (ChatBoard.ChatMessage msg : messages)
		{
			final Label label = new Label(msg.getMessage(), SettlersOfCatan.getSkin());
			label.setColor(msg.getSenderColour().displayColor);
			verticalGroup.addActor(label);
		}

		// Add label
		SaneTextField chat = new SaneTextField("Message", "dialog");
		getContentTable().row();
		getContentTable().add(chat).left().bottom();

		addButton("Submit", new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				Turn turn = new Turn(Requests.Request.BodyCase.CHATMESSAGE);
				turn.setChatMessage(chat.getText());
				client.acquireLocksAndSendTurn(turn);
			}
		});

		addButton("Cancel");
	}
}
