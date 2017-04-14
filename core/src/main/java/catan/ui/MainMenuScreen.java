package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import catan.SettlersOfCatan;

public class MainMenuScreen implements Screen
{
	final private Stage ui = new Stage(new ScreenViewport());

	MainMenuScreen(final SettlersOfCatan game)
	{
		Gdx.input.setInputProcessor(ui);

		// Background
		Image background = new Image(new Texture(Gdx.files.internal("splash.jpg")));
		background.setScaling(Scaling.fill);
		background.setFillParent(true);
		ui.addActor(background);

		// Menu setup
		VerticalGroup body = new VerticalGroup();
		body.setFillParent(true);
		body.padTop(50);
		body.space(60);
		ui.addActor(body);

		// Title
		body.addActor(new Label("Settlers of Catan", SettlersOfCatan.getSkin(), "title"));

		// Buttons
		{
			TextButton button = new TextButton("New Single Player Game", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{

					game.startNewServer();
					game.setScreen(new GameScreen(game));
				}
			});
			body.addActor(button);
		}

		{
			TextButton button = new TextButton("Host New Multiplayer Game", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{
					Gdx.app.debug("UI", "Button click: Host New Multiplayer Game");

					game.startNewServer();
					game.setScreen(new GameScreen(game));
				}
			});
			body.addActor(button);
		}

		{
			TextButton button = new TextButton("Join Remote Game", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{
					Gdx.app.debug("UI", "Button click: Join Remote Game");

					game.setScreen(new RemoteGameScreen(game));
				}
			});
			body.addActor(button);
		}

		{
			TextButton button = new TextButton("Quit", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{
					Gdx.app.exit();
				}
			});
			body.addActor(button);
		}
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(delta);
		ui.draw();
	}

	@Override
	public void resize(final int width, final int height)
	{
		ui.getViewport().update(width, height, true);
	}

	@Override
	public void dispose()
	{
		ui.dispose();
	}

	// Required but unused
	@Override
	public void pause()
	{}

	@Override
	public void resume()
	{}

	@Override
	public void hide()
	{}

	@Override
	public void show()
	{}
}
