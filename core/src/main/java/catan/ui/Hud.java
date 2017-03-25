package catan.ui;

import client.ClientGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import enums.ResourceType;
import game.players.Player;

class Hud implements Disposable {
    private final static String RESOURCES_FMT = "Brick(%d) Lumber(%d) Wool(%d) Grain(%d) Ore(%d)";
    private final Stage stage;
    private final Viewport viewport;
    private final ClientGame state;

    private Label resources;

    Hud(SpriteBatch batch, ClientGame state, Skin skin) {
        this.state = state;
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera());
        stage = new Stage(viewport, batch);

        Table table = new Table();
        table.top(); // top-align table
        table.setFillParent(true); // table fills entire stage

        resources = new Label(resourceText(), skin);
        table.add(resources).expandX().padTop(10);
        stage.addActor(table);
    }

    public void update()
    {
        resources.setText(resourceText());
        stage.act();
        stage.draw();
    }

    private String resourceText()
    {
        final Player me = state.getPlayer();
        final int brick = me.getResources().get(ResourceType.Brick);
        final int lumber = me.getResources().get(ResourceType.Lumber);
        final int wool = me.getResources().get(ResourceType.Wool);
        final int grain = me.getResources().get(ResourceType.Grain);
        final int ore = me.getResources().get(ResourceType.Ore);

        return String.format(RESOURCES_FMT, brick, lumber, wool, grain, ore);
    }

    @Override
    public void dispose()
    {
        stage.dispose();
    }
}