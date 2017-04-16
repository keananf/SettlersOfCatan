package catan.ui;

import AI.LocalAIClient;
import catan.SettlersOfCatan;
import client.Client;
import client.LocalClient;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.Difficulty;

public class GameSettingsDialog extends Dialog
{
    private final SettlersOfCatan catan;
    private boolean isAi;
    private int numAIs;
    private Difficulty difficulty;
    private String userName = "Player";

    public GameSettingsDialog(String title, Skin skin, SettlersOfCatan catan)
    {
        super(title, skin);
        this.catan = catan;

        VerticalGroup vert  = new VerticalGroup();
        final Table root = new Table();
        root.setFillParent(true);
        addActor(root);

        // Add label
        TextField offering = new TextField("Settings", SettlersOfCatan.getSkin());
        offering.setTextFieldListener((textField, c) -> textField.setText("Settings"));
        vert.addActor(offering);

        addOptions(vert);
        root.add(vert);
        addConfirmButtons();
    }

    private void addOptions(VerticalGroup vert)
    {
        // Username
        TextField text = new TextField("Name", SettlersOfCatan.getSkin());
        text.setTextFieldListener((textField, c) -> userName = textField.getText());

        // If the user is an AI
        CheckBox checkBox = new CheckBox("Play as AI", SettlersOfCatan.getSkin());
        checkBox.addListener(new InputListener()
        {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                super.touchUp(event, x, y, pointer, button);
                checkBox.toggle();
                isAi = checkBox.isChecked();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                super.touchUp(event, x, y, pointer, button);
                checkBox.toggle();
                isAi = checkBox.isChecked();
                return true;
            }
        });

        // Number of AIs to play with. Remaining are remote players implicitly
        TextField numAIsInput = new TextField("Number of AIs", SettlersOfCatan.getSkin());
        numAIsInput.setTextFieldListener(new TextField.TextFieldListener()
        {
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
                    textField.setText("Number of AIs");
                }
                numAIs = num;
            }
        });

        HorizontalGroup horiz = new HorizontalGroup();
        CheckBox randomSetting = new CheckBox("Easy (Random)", SettlersOfCatan.getSkin());
        checkBox.addListener(new InputListener()
        {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                super.touchUp(event, x, y, pointer, button);
                checkBox.toggle();
                difficulty = Difficulty.VERYEASY;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                super.touchUp(event, x, y, pointer, button);
                checkBox.toggle();
                difficulty = Difficulty.VERYEASY;
                return true;
            }
        });
        CheckBox easySetting = new CheckBox("Medium", SettlersOfCatan.getSkin());
        checkBox.addListener(new InputListener()
        {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                super.touchUp(event, x, y, pointer, button);
                checkBox.toggle();
                difficulty = Difficulty.EASY;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                super.touchUp(event, x, y, pointer, button);
                checkBox.toggle();
                difficulty = Difficulty.EASY;
                return true;
            }
        });

        horiz.addActor(numAIsInput);
        horiz.addActor(randomSetting);
        horiz.addActor(easySetting);

        vert.addActor(text);
        vert.addActor(checkBox);
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
                Client c;

                // If player is AI
                if(isAi)
                {
                    c = new LocalAIClient(difficulty, catan, userName, numAIs);
                }
                else
                {
                    c = new LocalClient(catan,userName, numAIs);
                }
                catan.startNewServer(c);
                catan.setScreen(new GameScreen(catan));
            }
        });
        button(button, true).button("Cancel", false);
    }
}