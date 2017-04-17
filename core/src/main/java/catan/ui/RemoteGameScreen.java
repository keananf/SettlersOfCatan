package catan.ui;

import AI.RemoteAIClient;
import catan.SettlersOfCatan;
import client.Client;
import client.RemoteClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import enums.Difficulty;

public class RemoteGameScreen implements Screen
{

	final private SettlersOfCatan game;
	final private Stage ui = new Stage(new ScreenViewport());
	private boolean isAi;
	private Difficulty difficulty = Difficulty.VERYEASY;

	RemoteGameScreen(final SettlersOfCatan game)
	{
		this.game = game;
		Gdx.input.setInputProcessor(ui);

		Image background = new Image(new Texture(Gdx.files.internal("splash.jpg")));
		background.setScaling(Scaling.fill);
		background.setFillParent(true);
		ui.addActor(background);

		VerticalGroup body = new VerticalGroup();
		body.setFillParent(true);
		body.padTop(50);
		body.space(60);
		ui.addActor(body);

		body.addActor(new Label("Remote Game ", SettlersOfCatan.getSkin(), "title"));
		TextField host = new TextField("Host", SettlersOfCatan.getSkin());
		TextField user = new TextField("Player", SettlersOfCatan.getSkin());
		body.addActor(user);
		body.addActor(host);

		
		addAIOptions(body);
		addSubmitButton(body, host, user);
		addBackButton(body);
	}
	
	private void addAIOptions(VerticalGroup body)
	{
		HorizontalGroup AISelection = new HorizontalGroup();
		CheckBox aiCheckBox = new CheckBox("Play as AI", SettlersOfCatan.getSkin());
		aiCheckBox.addListener(new InputListener(){
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				super.touchUp(event, x, y, pointer, button);
				aiCheckBox.toggle();
				isAi=aiCheckBox.isChecked();
			}   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				aiCheckBox.toggle();
				isAi = aiCheckBox.isChecked();
				return true;
			}
		});

		// DIfficulty
		CheckBox isRandom = new CheckBox("Easy(Random)",SettlersOfCatan.getSkin());
		isRandom.addListener(new InputListener(){
			public void touchUp(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				isRandom.toggle();
				difficulty = Difficulty.VERYEASY;
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				isRandom.toggle();
				difficulty = Difficulty.VERYEASY;
				return true;
			}
		});
		CheckBox isEasy = new CheckBox("Medium", SettlersOfCatan.getSkin());
		isEasy.addListener(new InputListener()
		{
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				isEasy.toggle();
				difficulty = Difficulty.EASY;
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				isEasy.toggle();
				difficulty = Difficulty.EASY;
				return true;
			}
		});

		AISelection.addActor(aiCheckBox);
		AISelection.addActor(isRandom);
		AISelection.addActor(isEasy);
		body.addActor(AISelection);
	}

	private void addSubmitButton(VerticalGroup body, TextField host, TextField user)
	{
		TextButton button = new TextButton("Submit", SettlersOfCatan.getSkin());
		button.addListener(new ChangeListener()
		{
			public void changed(ChangeEvent event, Actor actor)
			{
				boolean valid = false;
				Client client;
				String name = user.getText() == null || user.getText().equals("") ? "Default" : user.getText();

				// Create proper type of remote client
				if(isAi)
				{
					client = new RemoteAIClient(host.getText(), difficulty, name, game);
					valid = ((RemoteAIClient)client).isInitialised();
				}
				else
				{
					client = new RemoteClient(host.getText(), name, game);
					valid = ((RemoteClient)client).isInitialised();
				}

				// Start up new thread for the newly created client
				if (valid)
				{
				    game.startNewRemoteClient(client);
					game.setScreen(new GameScreen(game));
				}
			}

		});

		body.addActor(button);
	}

	private void addBackButton(VerticalGroup body)
	{
		TextButton button = new TextButton("Main Menu", SettlersOfCatan.getSkin());
		button.addListener(new ChangeListener()
		{
			public void changed(ChangeEvent event, Actor actor)
			{
				game.setScreen(new MainMenuScreen(game));
			}
		});

		body.addActor(button);
	}

	@Override
	public void show()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta)
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(delta);
		ui.draw();

	}

	@Override
	public void resize(int width, int height)
	{
		ui.getViewport().update(width, height, true);

	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void hide()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose()
	{
		ui.dispose();

	}
}
