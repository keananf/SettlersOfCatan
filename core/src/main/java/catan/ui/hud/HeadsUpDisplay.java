package catan.ui.hud;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.players.Player;

public class HeadsUpDisplay extends Stage {
	private enum PlayedCard { PlayedKnight }

	public HeadsUpDisplay(final Player me)
	{
		super(new ScreenViewport());

		final Table root = new Table();
		root.setFillParent(true);
		addActor(root);

		/*
		 * DEVELOPMENT CARDS
		 */
		final VerticalGroup developmentCards = new VerticalGroup();
		developmentCards.space(5f);
		for (DevelopmentCardType type : DevelopmentCardType.values())
		{
			developmentCards.addActor(new Counter<>(type, () -> me.getDevelopmentCards().getOrDefault(type, 0)));
		}
		developmentCards.addActor(
				new Counter<>(
						PlayedCard.PlayedKnight,
						() -> me.getPlayedDevCards().getOrDefault(DevelopmentCardType.Knight, 0)));
		root.add(developmentCards).expand().left();

		root.row();

		/*
		 * RESOURCES
		 */
		final HorizontalGroup resources = new HorizontalGroup();
		resources.space(5);
		for (ResourceType type : ResourceType.values())
		{
			if (type == ResourceType.Generic) continue;
			resources.addActor(new Counter<>(type, () -> me.getResources().getOrDefault(type, 0)));
		}
		root.add(resources).expand().bottom().colspan(2);
	}

	public void render()
	{
		act();
		draw();
	}
}