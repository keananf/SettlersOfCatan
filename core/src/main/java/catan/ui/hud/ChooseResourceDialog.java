package catan.ui.hud;

import catan.SettlersOfCatan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.ResourceType;
import intergroup.Requests;

class ChooseResourceDialog extends SaneDialog
{
	ChooseResourceDialog(final Client client)
	{
		super("Choose Resource");

		final ButtonGroup<ResourceCheckBox> buttons = new ButtonGroup<>();

		for (ResourceType r : ResourceType.values())
		{
			if (r.equals(ResourceType.Generic)) continue;

			final ResourceCheckBox cb = new ResourceCheckBox(r);
			buttons.add(cb);
			getContentTable().add(cb);
			getContentTable().row();
		}

		addButton("Submit", new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				Turn turn = new Turn(Requests.Request.BodyCase.CHOOSERESOURCE);
				turn.setChosenResource(buttons.getChecked().getResource());
				client.acquireLocksAndSendTurn(turn);
			}

		});
		addButton("Cancel");
	}

	class ResourceCheckBox extends CheckBox
	{
		private final ResourceType resource;

		ResourceCheckBox(final ResourceType resource)
		{
			super(resource.name(), SettlersOfCatan.getSkin());

			this.resource = resource;
		}

		public ResourceType getResource()
		{
			return resource;
		}
	}
}