package catan.ui;

import catan.SettlersOfCatan;
import client.Client;

public class Updater implements Runnable
{
    private SettlersOfCatan catan;
    private GameScreen screen;
    private Client client;

    public Updater(SettlersOfCatan catan, GameScreen screen)
    {
        this.catan = catan;
        this.screen = screen;
        this.client = catan.getClient();
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                client.getStateLock().acquire();
                try
                {
                    if(client.getState() == null) continue;

                    screen.updateNodes(client.getState().getGrid().nodes);
                }
                finally
                {
                    client.getStateLock().release();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
