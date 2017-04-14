package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssMan;
import client.ClientGame;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.players.Player;

public class HeadsUpDisplay extends Stage
{
	private static final float MESSAGE_DURATION = 5; // seconds
	private float messageTimeLeft = 0;
	private final Label messageBox;

	private final Image currentTurn;
	private final ClientGame state;

	public HeadsUpDisplay(final ClientGame state)
	{
		super(new ScreenViewport());
		this.state = state;
		final Player me = state.getPlayer();

		final Table root = new Table();
		root.setFillParent(true);
		addActor(root);

		// ======================================================================================

		{
			final Counter vps = new Counter("victory-points", me::getVp);
			root.add(vps).left();
		}

		/*
		 * Outlet for miscilanious messages
		 */
		messageBox = new Label("", SettlersOfCatan.getSkin());
		messageBox.setVisible(false);
		root.add(messageBox).center();

		{
			currentTurn = new Image(AssMan.getDrawable("turn.png"));
			root.add(currentTurn).right();
		}

		root.row(); // ==========================================================================

		/*
		 * DEVELOPMENT CARDS
		 */
		final VerticalGroup developmentCards = new VerticalGroup();
		developmentCards.space(5f);
		for (DevelopmentCardType type : DevelopmentCardType.values())
		{
			developmentCards.addActor(new Counter(type.toString().toLowerCase(),
					() -> me.getDevelopmentCards().getOrDefault(type, 0)));
		}
		developmentCards.addActor(new Counter("playedknight",
				() -> me.getPlayedDevCards().getOrDefault(DevelopmentCardType.Knight, 0)));
		root.add(developmentCards).left();

		root.add(); // blank centre middle cell

		/*
		 * PLAYERS
		 */
		final VerticalGroup players = new VerticalGroup();
		players.space(5);
		for (Player player : state.getPlayersAsList())
		{
			players.addActor(new PlayerBar(player));
		}
		root.add(players).right().pad(10);


		root.row(); // ==========================================================================

		/*
		 * RESOURCES
		 */
		final HorizontalGroup resources = new HorizontalGroup();
		resources.space(5);
		for (ResourceType type : ResourceType.values())
		{
			if (type == ResourceType.Generic) continue;
			resources.addActor(new Counter(type.toString().toLowerCase(),
					() -> me.getResources().getOrDefault(type, 0)));
		}
		root.add(resources).expand().bottom().colspan(2);

		// ======================================================================================
	}

	public void render(final float delta)
	{
		act();
		draw();

		if (messageTimeLeft > 0)
		{
			messageTimeLeft -= delta;
		} else {
			messageBox.setVisible(false);
		}

		currentTurn.setColor(state.getCurrentPlayer().getDisplayColor());
	}

	public void sendMessage(final String message)
	{
		messageBox.setText(message);
		messageTimeLeft = MESSAGE_DURATION;
		messageBox.setVisible(true);
	}
}