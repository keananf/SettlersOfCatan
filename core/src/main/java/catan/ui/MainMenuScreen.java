package catan.ui;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Actor;

import catan.SettlersOfCatan;

public class MainMenuScreen implements Screen
{
	final private SettlersOfCatan game;
	final private Stage ui = new Stage(new ScreenViewport());

	public MainMenuScreen(final SettlersOfCatan game)
	{
		this.game = game;
		Gdx.input.setInputProcessor(ui);

		// Background
		Image background = new Image(new Texture(Gdx.files.internal("splash.jpg")));
		background.setScaling(Scaling.fill);
		ui.addActor(background);

		// Menu setup
		VerticalGroup body = new VerticalGroup();
		body.setFillParent(true);
		body.padTop(50);
		body.space(60);
		ui.addActor(body);

		// Title
		body.addActor(new Label("Settlers of Catan", game.skin, "title"));

		{
			TextButton button = new TextButton("New single player game", game.skin);
			button.addListener(new ChangeListener() {
				public void changed(ChangeEvent event, Actor actor) {
					Gdx.app.debug("UI", "New single player game");
				}
			});
			body.addActor(button);
		}

		{
			TextButton button = new TextButton("Host new multiplayer game", game.skin);
			button.addListener(new ChangeListener() {
				public void changed(ChangeEvent event, Actor actor) {
					Gdx.app.debug("UI", "Host new multiplayer game");
				}
			});
			body.addActor(button);
		}

		{
			TextButton button = new TextButton("Join existing multiplayer game", game.skin);
			button.addListener(new ChangeListener() {
				public void changed(ChangeEvent event, Actor actor) {
					Gdx.app.debug("UI", "Join existing multiplayer game");
				}
			});
			body.addActor(button);
		}

		{
			TextButton button = new TextButton("Quit", game.skin);
			button.addListener(new ChangeListener() {
				public void changed(ChangeEvent event, Actor actor) {
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

	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
