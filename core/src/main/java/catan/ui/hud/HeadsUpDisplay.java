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
	private final Client client;
	private float messageTimeLeft = 0;
	private final Label messageBox;

	private final Image currentTurn;
	private final Label diceRoll;
	private final ClientGame state;
	private final Player me;

	public HeadsUpDisplay(final Client client, SettlersOfCatan catan)
	{
		super(new ScreenViewport());
		this.state = client.getState();
		this.client = client;
		me = state.getPlayer();

		final Table root = new Table();
		root.setFillParent(true);
		addActor(root);

		final Image bground = new Image(AssetMan.getTexture("icons/player.png"));
		bground.setColor(state.getPlayer().getColour().getDisplayColor());
		final Counter vps = new Counter(bground, me::getVp);
		root.add(vps).left();


		//Outlet for miscellaneous messages
		messageBox = new Label("", SettlersOfCatan.getSkin());
		messageBox.setVisible(false);
		root.add(messageBox).center();

		// Add current turn and dice roll
		currentTurn = new Image(AssetMan.getDrawable("icons/player.png"));
		diceRoll = new Label("Dice: 0", SettlersOfCatan.getSkin());
		root.add(diceRoll).right();
		root.add(currentTurn).right();
		root.row();

		// Add player's HUD info
		addDevelopmentCards(root, catan);
		addPlayerBars(root);
		addResources(root);

		// Buttons Stacked on top of one another
		if(!catan.isAI)
		{
			VerticalGroup buttons = new VerticalGroup();
			buttons.space(1f);
			buttons.addActor(addBuyDevCardButton());
			buttons.addActor(addDiceRollButton());
			buttons.addActor(addBankTradeButton());
			buttons.addActor(addEndTurnButton());
			root.add(buttons).right();
		}
	}

	private void addDevelopmentCards(Table root, SettlersOfCatan catan)
	{

		final VerticalGroup developmentCards = new VerticalGroup();
		developmentCards.space(5f);
		for (DevelopmentCardType type : DevelopmentCardType.values())
		{
			// Skip victory point cards as they will be listed under one thing
			if(type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University)
					|| type.equals(DevelopmentCardType.Chapel) || type.equals(DevelopmentCardType.Palace)
					|| type.equals(DevelopmentCardType.Market))
				continue;

			ImageButton i = new ImageButton(AssetMan.getDrawable(String.format("%sCardButton.png", type.name())));
			Actor a = new Counter(type.toString().toLowerCase(),
					() -> me.getDevelopmentCards().getOrDefault(type, 0));
			a.scaleBy(0.5f);
			a.setPosition(i.getX() + i.getWidth(), i.getY());
			i.addActor(a);

			// Make buttons non-functional if an AI is playing
			if(!catan.isAI)
			{
				i.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						super.clicked(event, x, y);
						client.log("UI", String.format("%s Button Clicked", type.name()));
						Turn turn = new Turn(Requests.Request.BodyCase.PLAYDEVCARD);
						turn.setChosenCard(type);
						client.acquireLocksAndSendTurn(turn);
					}
				});
			}
			developmentCards.addActor(i);
		}

		// Add VP
		ImageButton i = new ImageButton(AssetMan.getDrawable("VictoryPointCardButton.png"));
		Actor a = new Counter("victory-points", () ->
		{
			int sum = 0;
			for(DevelopmentCardType type : DevelopmentCardType.values())
			{
				// Skip non VP cards
				if(!(type.equals(DevelopmentCardType.Library) || type.equals(DevelopmentCardType.University)
						|| type.equals(DevelopmentCardType.Chapel) || type.equals(DevelopmentCardType.Palace)
						|| type.equals(DevelopmentCardType.Market)))
					continue;
				sum += me.getDevelopmentCards().getOrDefault(type, 0);
			}
			return sum;
		});
		a.scaleBy(0.5f);
		a.setPosition(i.getX() + i.getWidth(), i.getY());
		i.addActor(a);
		developmentCards.addActor(i);

		root.add(developmentCards).top().expandY().left();

		root.add(); // blank centre middle cell
	}

	private void addPlayerBars(Table root)
	{
		/*
		 * PLAYERS
		 */
		final VerticalGroup players = new VerticalGroup();
		players.space(5);
		for (Player player : state.getPlayersAsList())
		{
			players.addActor(new PlayerBar(player, client, this));
		}
		root.add(players).right().pad(10);

		root.row();
	}

	protected void addResources(Table root)
	{
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
	}

	private ImageButton addBuyDevCardButton()
	{
		ImageButton buyDevCardBtn = new ImageButton(AssetMan.getDrawable("BuyDevelopmentCard.png"));
		buyDevCardBtn.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.log("UI", "Buy Dev Card Button Clicked");
				client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.BUYDEVCARD));
			}
		});
		return buyDevCardBtn;
	}

	private ImageButton addDiceRollButton()
	{
		ImageButton diceRoll = new ImageButton(AssetMan.getDrawable("dice.png"));
		diceRoll.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.log("UI", "Roll Dice Button Clicked");
				client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.ROLLDICE));
			}
		});

		return diceRoll;
	}

	private ImageButton addEndTurnButton()
	{
		ImageButton endTurnBtn = new ImageButton(AssetMan.getDrawable("EndTurn.png"));
		endTurnBtn.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.log("UI", "End Turn Button Clicked");
				client.acquireLocksAndSendTurn(new Turn(Requests.Request.BodyCase.ENDTURN));
			}
		});
		return endTurnBtn;
	}

	public ImageButton addBankTradeButton()
	{
		HeadsUpDisplay hud = this;
		ImageButton bankTradeBtn = new ImageButton(AssetMan.getDrawable("TradeWithBank.png"));
		bankTradeBtn.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.log("UI", String.format("Bank Trade Button Clicked"));

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

		currentTurn.setColor(state.getCurrentPlayer().getDisplayColor());
		diceRoll.setText(String.format("Dice: %d", client.getState().getDice()));
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