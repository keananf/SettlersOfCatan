package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class MenuScreen implements Screen
{
	private final Stage stage = new Stage(new ScreenViewport());
	private final VerticalGroup primaryGroup = new VerticalGroup();
	private final HorizontalGroup secondaryGroup = new HorizontalGroup();

	MenuScreen(final String title)
	{
		// delegate input processing to stage
		Gdx.input.setInputProcessor(stage);

		// full-window background image
		final Image background = AssetMan.getImage("splash.jpg");
		background.setScaling(Scaling.fill);
		background.setFillParent(true);
		stage.addActor(background);

		final Table rootTable = new Table();
		rootTable.setFillParent(true);
		stage.addActor(rootTable);

		// primary input group
		primaryGroup.space(20);
		rootTable.add(primaryGroup);

		primaryGroup.addActor(new Label(title, SettlersOfCatan.getSkin(), "title"));

		// secondary input group
		rootTable.row();
		secondaryGroup.padTop(40);
		secondaryGroup.space(60);
		rootTable.add(secondaryGroup);
	}

	void addPrimary(final Actor actor)
	{
		primaryGroup.addActor(actor);
	}

	void addSecondary(final Actor actor)
	{
		secondaryGroup.addActor(actor);
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(final int width, final int height)
	{
		stage.getViewport().update(width, height, true);
		primaryGroup.setSize(primaryGroup.getPrefWidth(), primaryGroup.getPrefHeight());
		secondaryGroup.setSize(secondaryGroup.getPrefWidth(), secondaryGroup.getPrefHeight());
	}

	@Override
	public void dispose()
	{
		stage.dispose();
	}

	@Override
	public void show()
	{}

	@Override
	public void pause()
	{}

	@Override
	public void resume()
	{}

	@Override
	public void hide()
	{}
}
