package game;

import intergroup.board.Board;
import intergroup.trade.Trade;

/**
 * Class representing the current trade
 * 
 * @author 140001596
 */
public class CurrentTrade
{
	private final Board.Player instigator;
	private final Trade.WithPlayer trade;
	private long time = System.currentTimeMillis();
	private static final int EXPIRY = 30 * 1000; // 30 seconds

	public CurrentTrade(Trade.WithPlayer trade, Board.Player instigator)
	{
		this.instigator = instigator;
		this.trade = trade;
	}

	public Trade.WithPlayer getTrade()
	{
		return trade;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public boolean isExpired()
	{
		long diff = System.currentTimeMillis() - time;
		return diff > EXPIRY;
	}

	public Board.Player getInstigator()
	{
		return instigator;
	}
}
