package catan.ui;

import catan.SettlersOfCatan;
import client.ClientGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
	import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import enums.ResourceType;
import game.players.Player;

import java.util.EnumSet;

class Hud implements Disposable {
    private final Stage stage;
    private final ClientGame state;
    private final AssMan assets;
    private final Player me;
    private final Skin skin;

    private static final int SPRITE_DIM = 64;

    private final TextureRegion[] resourceSprites = new TextureRegion[5];

    Hud(SpriteBatch batch, SettlersOfCatan game) {
        this.state = game.getState();
        this.assets = game.assets;
        this.skin = game.skin;
        Texture resourcesSheet = assets.getTexture("resources.png");
        me = state.getPlayer();

        Viewport viewport = new FitViewport(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight(),
                new OrthographicCamera());
        stage = new Stage(viewport, batch);

        // Set up table for layout
        Table table = new Table();
        table.bottom();
        table.setDebug(true);
        table.setFillParent(true); // table fills entire stage
        stage.addActor(table);

        // Add resource counts to table
        for (int i = 0; i < 5; i++)
        {
            resourceSprites[i] = new TextureRegion(resourcesSheet, i * SPRITE_DIM, 0, SPRITE_DIM, SPRITE_DIM);
        }
        final EnumSet<ResourceType> resources = EnumSet.allOf(ResourceType.class);
        resources.remove(ResourceType.Generic);
        resources.forEach(type -> addResourceCount(type, table));
    }

    private void addResourceCount(final ResourceType type, final Table table)
    {
        final Stack stack = new Stack();
        stack.add(new Image(resourceSprites[type.ordinal()]));
        Integer quantity = me.getResources().get(type);
        stack.add(new Label(quantity+"", this.skin));
        table.add(stack);
    }

    public void update()
    {
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose()
    {
        stage.dispose();
    }
}
