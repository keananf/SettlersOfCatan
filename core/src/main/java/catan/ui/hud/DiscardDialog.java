package catan.ui.hud;

import catan.SettlersOfCatan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.ResourceType;
import intergroup.Requests;

import java.util.HashMap;
import java.util.Map;

public class DiscardDialog extends Dialog
{
    private final Client client;
    Map<ResourceType, Integer> resources;

    public DiscardDialog(String title, Skin skin, Client client, HeadsUpDisplay hud)
    {
        super(title, skin);
        this.client = client;
        resources = new HashMap<ResourceType, Integer>();

        VerticalGroup vert  = new VerticalGroup();
        final Table root = new Table();
        root.setFillParent(true);
        hud.getResources();
        addActor(root);

        // Add label
        TextField offering = new TextField("Discard", SettlersOfCatan.getSkin());
        offering.setTextFieldListener((textField, c) -> textField.setText("Discard"));
        vert.addActor(offering);

        for(ResourceType r : ResourceType.values())
        {
            if(r.equals(ResourceType.Generic)) continue;

            resources.put(r, 0);
            addOptions(r, vert);
        }

        root.add(vert);
        addConfirmButtons();
    }

    private void addOptions(ResourceType r, VerticalGroup vert)
    {
        TextField text = new TextField(r.name(), SettlersOfCatan.getSkin());
        text.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c)
            {
                textField.setText(String.valueOf(c));
                int num = 0;
                try
                {
                    num = Integer.parseInt(textField.getText());
                }
                catch(NumberFormatException e)
                {
                    textField.setText(r.name());
                }
                resources.put(r, num);
            }
        });

        vert.addActor(text);
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

                // Set up Discard
                Turn turn = new Turn(Requests.Request.BodyCase.DISCARDRESOURCES);
                turn.setChosenResources(resources);
                client.acquireLocksAndSendTurn(turn);
            }
        });
        button(button, true).button("Cancel", false);
    }
}