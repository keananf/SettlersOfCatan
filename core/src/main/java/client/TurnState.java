package client;

import enums.ResourceType;
import game.CurrentTrade;
import intergroup.Requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TurnState extends Turn
{
	private List<Requests.Request.BodyCase> expectedMoves;
	private boolean tradePhase, turnStarted, initialPhase;
	private int roll;
	private CurrentTrade currentTrade;

	public TurnState()
	{
		super();
	}

	@Override
	protected void setUp()
	{
		expectedMoves = new ArrayList<Requests.Request.BodyCase>();
		chosenResources = new HashMap<ResourceType, Integer>();
		reset();
	}

	@Override
	public void reset()
	{
		super.reset();
		roll = 0;
		tradePhase = false;
		turnStarted = false;
		currentTrade = null;
		expectedMoves.clear();
	}

	/**
	 * Same as above but leaves expected moves and current trade in tact
	 */
	public void resetInfo()
	{
		super.resetInfo();
		roll = 0;
		tradePhase = false;
		turnStarted = false;
	}

	public List<Requests.Request.BodyCase> getExpectedMoves()
	{
		return expectedMoves;
	}

	public boolean isTradePhase()
	{
		return tradePhase;
	}

	public void setTradePhase(boolean tradePhase)
	{
		this.tradePhase = tradePhase;
	}

	public boolean hasTurnStarted()
	{
		return turnStarted;
	}

	public void setTurnStarted(boolean turnStarted)
	{
		this.turnStarted = turnStarted;
	}

	public boolean isInitialPhase()
	{
		return initialPhase;
	}

	public void setInitialPhase(boolean initialPhase)
	{
		this.initialPhase = initialPhase;
	}

	public int getRoll()
	{
		return roll;
	}

	public void setRoll(int roll)
	{
		this.roll = roll;
	}

	public void setCurrentTrade(CurrentTrade currentTrade)
	{
		this.currentTrade = currentTrade;
	}

	public CurrentTrade getCurrentTrade()
	{
		return currentTrade;
	}
}
