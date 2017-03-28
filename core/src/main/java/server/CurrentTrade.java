package server;

import intergroup.trade.Trade;

/**
 * Class representing the current trade
 * @author 140001596
 */
public class CurrentTrade
{
    private Trade.WithPlayer trade;
    private long time = System.currentTimeMillis();
    private static int EXPIRY = 30 * 1000; // 30 seconds

    public CurrentTrade(Trade.WithPlayer trade)
    {
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
        return System.currentTimeMillis() - time > EXPIRY;
    }
}
