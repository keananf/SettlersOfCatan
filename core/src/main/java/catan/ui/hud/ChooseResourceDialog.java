package catan.ui.hud;

import catan.SettlersOfCatan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.ResourceType;
import intergroup.Requests;

import java.util.ArrayList;
import java.util.List;

public class ChooseResourceDialog extends Dialog
{
	private final Client client;
	private ResourceType chosenResource;
	private List<CheckBox> checkBoxes;

	public ChooseResourceDialog(Skin skin, Client client, HeadsUpDisplay hud)
	{
		super("Choose Resource", skin);
		this.client = client;
		checkBoxes = new ArrayList<>();

		VerticalGroup vert = new VerticalGroup();
		final Table root = new Table();
		root.setFillParent(true);
		hud.getResources();
		addActor(root);

		// Add label
		TextField offering = new TextField("Choose Resource", SettlersOfCatan.getSkin());
		offering.setTextFieldListener((textField, c) -> textField.setText("Choose Resource"));
		vert.addActor(offering);

		for (ResourceType r : ResourceType.values())
		{
			if (r.equals(ResourceType.Generic)) continue;

			addCheckBoxes(r, vert);
		}

		root.add(vert);
		addConfirmButtons();
	}

	private void addCheckBoxes(ResourceType r, VerticalGroup vert)
	{
		CheckBox checkBox = new CheckBox(r.name(), SettlersOfCatan.getSkin());
		checkBox.addListener(new InputListener()
		{
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				checkBox.toggle();
				chosenResource = r;

				System.out.println("Chosen Resource: " + chosenResource.name());
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				checkBox.toggle();
				chosenResource = r;

				System.out.println("Chosen Resource: " + chosenResource.name());
				return true;
			}
		});

		checkBoxes.add(checkBox);
		vert.addActor(checkBox);
	}

	private void addConfirmButtons()
	{
		TextButton button = new TextButton("Submit", SettlersOfCatan.getSkin());
		button.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);

				// Set up choose
				Turn turn = new Turn(Requests.Request.BodyCase.CHOOSERESOURCE);
				turn.setChosenResource(chosenResource);
				client.acquireLocksAndSendTurn(turn);
			}
		});
		button(button, true).button("Cancel", false);
	}
}