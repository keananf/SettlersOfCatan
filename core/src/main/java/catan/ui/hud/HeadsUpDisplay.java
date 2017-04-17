package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssetMan;
import client.Client;
import client.ClientGame;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
		root.debug();
		addActor(root);

		// Victory points counter (includes hidden VP cards)
		final Counter vps = new Counter("victory-points", me::getVp);
		root.add(vps).left().top().pad(10).uniform();

		// Outlet for miscellaneous messages
		messageBox = new Label("", SettlersOfCatan.getSkin());
		messageBox.setVisible(false);
		root.add(messageBox).top();

		// Last dice roll
		final Counter diceRoll = new Counter("dice-roll", state::getDice);
		root.add(diceRoll).right().top().pad(10).uniform();

		/* ******* */ root.row().expand(); /* ************************************************************************ */

		root.add(getDevCards(isAI)).left();
		root.add(/* empty cell */);
		root.add(getPlayerBars()).right();

		/* ******* */ root.row().expand(); /* ************************************************************************ */

		root.add(/* empty cell */);
		root.add(getResources()).bottom();

		// Buttons Stacked on top of one another
		if(!isAI)
		{
			VerticalGroup buttons = new VerticalGroup();
			buttons.space(1f);
			buttons.addActor(getBuyDevCardButton());
			buttons.addActor(getBankTradeButton());
			buttons.addActor(getEndTurnButton());
			buttons.addActor(showChatButton());
			root.add(buttons).right();
		}
	}

	private VerticalGroup getDevCards(boolean isAi) {
		final VerticalGroup developmentCards = new VerticalGroup();
		for (DevelopmentCardType type : DevelopmentCardType.values()) {
			// Skip victory point cards as they will be listed under one thing
			if (type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University)
					|| type.equals(DevelopmentCardType.Chapel) || type.equals(DevelopmentCardType.Palace)
					|| type.equals(DevelopmentCardType.Market))
				continue;

			ImageButton i = new ImageButton(AssetMan.getDrawable(String.format("%sCardButton.png", type.name())));
			Actor a = new Counter(type.toString().toLowerCase(),

					() -> me.getDevelopmentCards().getOrDefault(type, 0));
			developmentCards.addActor(counter);

			// Make buttons non-functional if an AI is playing
			if (!isAi) {
				counter.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						Turn turn = new Turn(Requests.Request.BodyCase.PLAYDEVCARD);
						turn.setChosenCard(type);
						client.acquireLocksAndSendTurn(turn);
					}
				});
			}
		}

		return developmentCards;
	}

	private VerticalGroup getPlayerBars()
	{
		final VerticalGroup players = new VerticalGroup();
		players.space(5);
		for (Player player : state.getPlayersAsList())
		{
			if (player != state.getPlayer()) {
				players.addActor(new PlayerBar(player, client, this));
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
				resources.addActor(new Counter(type.toString().toLowerCase(),
						() -> me.getResources().getOrDefault(type, 0)));
			}
		}
		return resources;
	}

	private ImageButton getBuyDevCardButton()
	{
		ImageButton buyDevCardBtn = AssetMan.getImageButton("BuyDevelopmentCard.png");
		buyDevCardBtn.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.BUYDEVCARD));
			}
		});
		return buyDevCardBtn;
	}

	private ImageButton getEndTurnButton()
	{
		ImageButton endTurnBtn = AssetMan.getImageButton("EndTurn.png");
		endTurnBtn.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.ENDTURN));
			}
		});
		return endTurnBtn;
	}

	private Actor showChatButton()
	{
		HeadsUpDisplay hud = this;
		ImageButton endTurnBtn = new ImageButton(AssetMan.getDrawable("Chat.png"));
		endTurnBtn.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.log("UI", "Chat Button Clicked");
				ChatDialog dialog = new ChatDialog("Chat", SettlersOfCatan.getSkin(), client);
				dialog.show(hud);
			}
		});
		return endTurnBtn;
	}

	private ImageButton getBankTradeButton()
	{
		HeadsUpDisplay hud = this;
		ImageButton bankTradeBtn = AssetMan.getImageButton("TradeWithBank.png");
		bankTradeBtn.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				TradeDialog dialog = new TradeDialog("Resources", SettlersOfCatan.getSkin(),
						null, client, hud);
				dialog.show(hud);
			}
		});
		return bankTradeBtn;
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
	}

	public void sendMessage(final String message)
	{
		messageBox.setText(message);
		messageTimeLeft = MESSAGE_DURATION;
		messageBox.setVisible(true);
	}

	public void showDiscardDialog()
	{
		DiscardDialog dialog = new DiscardDialog("Discard", SettlersOfCatan.getSkin(), client, this);
		dialog.show(this);
	}

    public void showResponse()
	{
		TradeResponseDialog dialog = new TradeResponseDialog("Trade", SettlersOfCatan.getSkin(), client, this);
		dialog.show(this);
    }

	public void showChooseResource()
	{
		ChooseResourceDialog dialog = new ChooseResourceDialog("Choose Resource", SettlersOfCatan.getSkin(), client, this);
		dialog.show(this);
	}
}