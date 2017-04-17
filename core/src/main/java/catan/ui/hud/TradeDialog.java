package catan.ui.hud;

import catan.SettlersOfCatan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.ResourceType;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.trade.Trade;

import java.util.HashMap;
import java.util.Map;


public class TradeDialog extends Dialog
{
    private final Board.Player player;
    private final Client client;
    Map<ResourceType, Integer> resources, otherResources;

    public TradeDialog(String title, Skin skin, Board.Player player, Client client, HeadsUpDisplay hud)
    {
        super(title, skin);
        this.player = player;
        this.client = client;
        resources = new HashMap<ResourceType, Integer>();
        otherResources = new HashMap<ResourceType, Integer>();

        VerticalGroup vert  = new VerticalGroup();
        final Table root = new Table();
        hud.addResources(root);
        root.setFillParent(true);
        addActor(root);

        // Add labels
        HorizontalGroup horiz = new HorizontalGroup();
        TextField offering = new TextField("Offering", SettlersOfCatan.getSkin());
        offering.setTextFieldListener((textField, c) -> textField.setText("Offering"));
        TextField wanting = new TextField("Wanting", SettlersOfCatan.getSkin());
        wanting.setTextFieldListener((textField, c) -> textField.setText("Wanting"));
        horiz.addActor(offering);
        horiz.addActor(wanting);
        vert.addActor(horiz);

        for(ResourceType r : ResourceType.values())
        {
            if(r.equals(ResourceType.Generic)) continue;

            otherResources.put(r, 0);
            resources.put(r, 0);
            addOptions(r, vert);
        }

        root.add(vert);
        addConfirmButtons();
    }

    private void addOptions(ResourceType r, VerticalGroup vert)
    {
        HorizontalGroup horiz = new HorizontalGroup();
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
        TextField text2 = new TextField(r.name(), SettlersOfCatan.getSkin());
        text2.setTextFieldListener(new TextField.TextFieldListener() {
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
                otherResources.put(r, num);
            }
        });
        horiz.addActor(text);
        horiz.addActor(text2);

        vert.addActor(horiz);
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

                // If a player trade
                if(player != null)
                {
                    // Set up trade
                    Turn turn = new Turn(Requests.Request.BodyCase.INITIATETRADE);
                    Trade.WithPlayer.Builder builder = Trade.WithPlayer.newBuilder();
                    builder.setOther(player).build();
                    builder.setOffering(client.getState().processResources(resources));
                    builder.setWanting(client.getState().processResources(otherResources));

                    // Set Trade
                    turn.setPlayerTrade(builder.build());
                    client.acquireLocksAndSendTurn(turn);
                }
                else
                {
                    // Set up trade
                    Turn turn = new Turn(Requests.Request.BodyCase.INITIATETRADE);
                    Trade.WithBank.Builder builder = Trade.WithBank.newBuilder();
                    builder.setOffering(client.getState().processResources(resources));
                    builder.setWanting(client.getState().processResources(otherResources));

                    // Set Trade
                    turn.setBankTrade(builder.build());
                    client.acquireLocksAndSendTurn(turn);
                }
            }
        });
        button(button, true).button("Cancel", false);
    }
}
