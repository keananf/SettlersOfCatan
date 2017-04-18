package client;

import game.CurrentTrade;
import intergroup.Requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TurnState extends Turn
{
	private List<Requests.Request.BodyCase> expectedMoves;
	private boolean turnStarted, initialPhase, hasTraded;
	private int roll;
	private CurrentTrade currentTrade;

	TurnState()
	{
		super();
	}

	@Override
	protected void setUp()
	{
		expectedMoves = new ArrayList<>();
		chosenResources = new HashMap<>();
		reset();
	}

	@Override
	public void reset()
	{
		super.reset();
		roll = 0;
		turnStarted = false;
		hasTraded = false;
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
		hasTraded = false;
		turnStarted = false;
	}

	public List<Requests.Request.BodyCase> getExpectedMoves()
	{
		return expectedMoves;
	}

	boolean hasTurnStarted()
	{
		return turnStarted;
	}

	void setTurnStarted()
	{
		this.turnStarted = true;
	}

	boolean isInitialPhase()
	{
		return initialPhase;
	}

	void setInitialPhase(boolean initialPhase)
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

	void setCurrentTrade(CurrentTrade currentTrade)
	{
		this.currentTrade = currentTrade;
		hasTraded = true;
	}

	public CurrentTrade getCurrentTrade()
	{
		return currentTrade;
	}

	public boolean hasTraded()
	{
		return hasTraded;
	}

	public void setHasTraded(boolean hasTraded)
	{
		this.hasTraded = hasTraded;
	}
}
