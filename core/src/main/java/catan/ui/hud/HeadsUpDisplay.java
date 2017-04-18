package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssetMan;
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

import static intergroup.Requests.Request.BodyCase.BUYDEVCARD;
import static intergroup.Requests.Request.BodyCase.ENDTURN;

public class HeadsUpDisplay extends Stage
{
	private static final float MESSAGE_DURATION = 5; // seconds
	private float messageTimeLeft = 0;
	private final Label messageBox;

	private final Client client;
	private final ClientGame state;
	private final Player me;

	public HeadsUpDisplay(final Client client, final boolean isAI)
	{
		super(new ScreenViewport());
		this.state = client.getState();
		this.client = client;
		me = state.getPlayer();

		final Table root = new Table();
		root.setFillParent(true);
		root.pad(10);
		addActor(root);

		/* ******** Start of table ********************************************************************************** */

		// Victory points counter (includes hidden VP cards)
		final Counter vps = new Counter("victory-points", me::getVp);
		root.add(vps).left().top().uniform();

		// Outlet for miscellaneous messages
		messageBox = new Label("", SettlersOfCatan.getSkin());
		messageBox.setVisible(false);
		root.add(messageBox).top();

		// Last dice roll
		final Counter diceRoll = new Counter("dice", state::getDice);
		diceRoll.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.ROLLDICE));
			}
		});
		root.add(diceRoll).right().top().uniform();

		/* ********************************************************************************************************** */
		root.row().expand();

		root.add(getDevCards(isAI)).left();
		root.add(/* empty cell */);
		root.add(getPlayerBars(isAI)).right();

		/* ********************************************************************************************************** */
		root.row().expand();

		if (!isAI)
		{
			VerticalGroup buttons = new VerticalGroup();
			buttons.space(5);
			buttons.addActor(AssetMan.getButton(
					"BuyDevelopmentCard.png",
					() -> client.acquireLocksAndSendTurn(new Turn(BUYDEVCARD))));
			buttons.addActor(AssetMan.getButton(
					"Chat.png",
					() -> new ChatDialog(client).show(this)));
			root.add(buttons).left().bottom();
		}
		else
		{
			root.add();
		}

		root.add(getResources()).bottom();

		// Buttons Stacked on top of one another
		if (!isAI)
		{
			VerticalGroup buttons = new VerticalGroup();
			buttons.space(5);

			buttons.addActor(AssetMan.getButton(
					"TradeWithBank.png",
					() -> new TradeDialog(null, client).show(this)));
			buttons.addActor(AssetMan.getButton(
					"EndTurn.png",
					() -> this.client.acquireLocksAndSendTurn(new Turn(ENDTURN))));
			root.add(buttons).right().bottom();
		}
		else
		{
			root.add();
		}

		/* ******** End of table ************************************************************************************ */
	}

	private VerticalGroup getDevCards(boolean isAi)
	{
		final VerticalGroup developmentCards = new VerticalGroup();
		developmentCards.space(5);
		for (DevelopmentCardType type : DevelopmentCardType.values())
		{
			// Skip victory point cards as they will be listed under one thing
			if (type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University)
					|| type.equals(DevelopmentCardType.Chapel) || type.equals(DevelopmentCardType.Palace)
					|| type.equals(DevelopmentCardType.Market))
				continue;

			Counter counter = new Counter(type.toString().toLowerCase(),
					() -> me.getDevelopmentCards().getOrDefault(type, 0));
			developmentCards.addActor(counter);

			// Make buttons non-functional if an AI is playing
			if (!isAi)
			{
				counter.addListener(new ClickListener()
				{
					@Override
					public void clicked(InputEvent event, float x, float y)
					{
						Turn turn = new Turn(Requests.Request.BodyCase.PLAYDEVCARD);
						turn.setChosenCard(type);
						client.acquireLocksAndSendTurn(turn);
					}
				});
			}
		}

		return developmentCards;
	}

	private VerticalGroup getPlayerBars(final boolean isAI)
	{
		final VerticalGroup players = new VerticalGroup();
		players.space(5);
		for (Player player : state.getPlayersAsList())
		{
			if (player != state.getPlayer())
			{
				players.addActor(new PlayerBar(player, client, this, isAI));
			}
		}
		return players;
	}

	HorizontalGroup getResources()
	{
		final HorizontalGroup resources = new HorizontalGroup();
		resources.space(5);
		for (ResourceType type : ResourceType.values())
		{
			if (type != ResourceType.Generic)
			{
				final String typeName = type.toString().toLowerCase();
				resources.addActor(
						new Counter(typeName, () -> me.getResources().getOrDefault(type, 0)));
			}
		}
		return resources;
	}

	public void render(final float delta)
	{
		act();
		draw();

		if (messageTimeLeft > 0)
		{
			messageTimeLeft -= delta;
		}
		else
		{
			messageBox.setVisible(false);
		}
	}

	public void sendMessage(final String message)
	{
		messageBox.setText(message);
		messageTimeLeft = MESSAGE_DURATION;
		messageBox.setVisible(true);
	}

	public void showDiscardDialog()
	{
		new DiscardDialog(client).show(this);
	}

	public void showResponse()
	{
		new TradeResponseDialog(client).show(this);
	}

	public void showChooseResource()
	{
		new ChooseResourceDialog(client).show(this);
	}
}