package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class RemoteGameScreen implements Screen
{

	final private SettlersOfCatan game;
	final private Stage ui = new Stage(new ScreenViewport());

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

		body.addActor(new Label("Remote Game ", game.skin, "title"));
		TextField text = new TextField("Type Here", game.skin);
		body.addActor(text);

		addSubmitButton(body, text);
		addBackButton(body);
	}

	private void addSubmitButton(VerticalGroup body, TextField text)
	{
		TextButton button = new TextButton("Submit", game.skin);
		button.addListener(new ChangeListener()
		{
			public void changed(ChangeEvent event, Actor actor)
			{

				boolean valid = game.startNewRemoteClient(text.getText());
				if(valid)
				{
					game.setScreen(new GameScreen(game));
				}
			};

		});

		body.addActor(button);
	}

	private void addBackButton(VerticalGroup body)
	{
		TextButton button = new TextButton("Main Menu", game.skin);
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
