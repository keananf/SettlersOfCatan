package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssMan;
import client.Client;
import client.ClientGame;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.players.Player;
import intergroup.Requests;

public class HeadsUpDisplay extends Stage
{
	private static final float MESSAGE_DURATION = 5; // seconds
	private final Client client;
	private float messageTimeLeft = 0;
	private final Label messageBox;

	private final Image currentTurn;
	private final ClientGame state;

	public HeadsUpDisplay(final Client client)
	{
		super(new ScreenViewport());
		this.state = client.getState();
		this.client = client;
		final Player me = state.getPlayer();

		final Table root = new Table();
		root.setFillParent(true);
		addActor(root);

		// ======================================================================================

		{
			final Image bground = new Image(AssMan.getTexture("icons/player.png"));
			bground.setColor(state.getPlayer().getColour().getDisplayColor());
			final Counter vps = new Counter(bground, me::getVp);
			root.add(vps).left();
		}

		/*
		 * Outlet for miscilanious messages
		 */
		messageBox = new Label("", SettlersOfCatan.getSkin());
		messageBox.setVisible(false);
		root.add(messageBox).center();

		{
			currentTurn = new Image(AssMan.getDrawable("icons/player.png"));
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
		root.add(developmentCards).expandY().left();

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

		{
			ImageButton buyDevCardBtn = new ImageButton(AssMan.getDrawable("BuyDevelopmentCard.png"));
			buyDevCardBtn.addListener(new ClickListener()
			{
				@Override
				public void clicked(InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					client.log("UI", "Buy Dev Card Button Clicked");
					client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.BUYDEVCARD));
				}
			});
			root.add(buyDevCardBtn).left();
		}

		/*
		 * RESOURCES
		 */
		final HorizontalGroup resources = new HorizontalGroup();
		resources.space(5);
		for (ResourceType type : ResourceType.values())
		{
			if (type != ResourceType.Generic)
			{
				resources.addActor(new Counter(type.toString().toLowerCase(),
						() -> me.getResources().getOrDefault(type, 0)));
			}
		}
		root.add(resources).expandX().center();

		{

			ImageButton diceRoll = new ImageButton(AssMan.getDrawable("rollDice.png"));
			diceRoll.addListener(new ClickListener()
			{
				@Override
				public void clicked(InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					client.log("UI", "Roll Dice Button Clicked");
					client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.ROLLDICE));
				}
			});
			ImageButton endTurnBtn = new ImageButton(AssMan.getDrawable("EndTurn.png"));
			endTurnBtn.addListener(new ClickListener()
			{
				@Override
				public void clicked(InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					client.log("UI", "End Turn Button Clicked");
					client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.ENDTURN));
				}
			});
			ImageButton bankTradeBtn = new ImageButton(AssMan.getDrawable("TradeWithBank.png"));
			root.add(diceRoll).right();
			root.add(endTurnBtn).right();
			root.add(bankTradeBtn).left();
		}

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