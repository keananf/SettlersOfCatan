package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class MenuScreen implements Screen
{
	private final Stage ui = new Stage(new ScreenViewport());
	protected final VerticalGroup body = new VerticalGroup();

	MenuScreen()
	{
		// delegate input processing to stage
		Gdx.input.setInputProcessor(ui);

		// add full-window background image
		final Image background = AssetMan.getImage("splash.jpg");
		background.setScaling(Scaling.fill);
		background.setFillParent(true);
		ui.addActor(background);

		// add centered group for all elements
		body.setFillParent(true);
		body.padTop(100);
		body.space(50);
		ui.addActor(body);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(delta);
		ui.draw();
	}

	@Override
	public void resize(int width, int height) {
		ui.getViewport().update(width, height, true);
		body.setSize(body.getPrefWidth(), body.getPrefHeight());
	}

	@Override
	public void dispose() {
		ui.dispose();
	}

	@Override public void show() {}
	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}

}
