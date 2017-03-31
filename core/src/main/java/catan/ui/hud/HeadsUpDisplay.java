package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssMan;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.players.Player;

public class HeadsUpDisplay extends Stage {
	private final Skin skin;
	private final AssMan assets;
	private final Player me;

	public HeadsUpDisplay(SettlersOfCatan game)
	{
		this.skin = game.skin;
		this.assets = game.assets;
		Counter.setup(skin, assets);
		this.me = game.getState().getPlayer();

		final Table root = new Table();
		root.setFillParent(true);
		addActor(root);
		root.debug();

		/*
		 * DEVELOPMENT CARDS
		 */
		VerticalGroup developmentCards = new VerticalGroup();
		for (DevelopmentCardType type : DevelopmentCardType.values())
		{
			developmentCards.addActor(new Counter<>(type, () -> me.getDevelopmentCards().get(type)));
		}
		developmentCards.addActor(
				new Counter<>(
						PlayedCard.PlayedKnight,
						() -> me.getPlayedDevCards().get(DevelopmentCardType.Knight)));
		root.add(developmentCards);

		/*
		 * PLAYERS
		 */
		final VerticalGroup players = new VerticalGroup();
		players.space(5);
		for (Player player : game.getState().getPlayersAsList())
		{
			players.addActor(new PlayerBar(player));
		}
		root.add(players).expand().right().top().pad(10);

		root.row();

		/*
		 * RESOURCES
		 */
		HorizontalGroup resources = new HorizontalGroup();
		for (ResourceType type : ResourceType.values())
		{
			if (type == ResourceType.Generic) continue;
			resources.addActor(new Counter<>(type, () -> me.getResources().get(type)));
		}
		root.add(resources);
	}

	private enum PlayedCard { PlayedKnight }

	public void render()
	{
		act();
		draw();
	}
}