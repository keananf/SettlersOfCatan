package catan.ui.hud;

import catan.SettlersOfCatan;
import client.ChatBoard;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import intergroup.Requests;

public class ChatDialog extends Dialog
{
	private final Client client;
	private final ChatBoard chatBoard;

	public ChatDialog(String title, Skin skin, Client client)
	{
		super(title, skin);
		this.client = client;
		this.chatBoard = client.getState().getChatBoard();

		VerticalGroup vert = new VerticalGroup();
		final Table root = new Table();
		root.setFillParent(true);
		addActor(root);

		// Add last 15 Chat messages
		int i = 0;
		for (ChatBoard.ChatMessage msg : chatBoard.getMessages())
		{
			if (++i == 15) break;
			Label l = new Label(msg.getMessage(), SettlersOfCatan.getSkin());
			vert.addActor(l);
		}

		// Add label
		TextField chat = new TextField("New", SettlersOfCatan.getSkin());
		vert.addActor(chat);

		root.add(vert).bottom();
		addConfirmButtons(chat);
	}

	private void addConfirmButtons(TextField chat)
	{
		TextButton button = new TextButton("Submit", SettlersOfCatan.getSkin());
		button.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);

				// Set up choose
				Turn turn = new Turn(Requests.Request.BodyCase.CHATMESSAGE);
				turn.setChatMessage(chat.getText());
				client.acquireLocksAndSendTurn(turn);
			}
		});
		button(button).button("Cancel", false);
	}

}
