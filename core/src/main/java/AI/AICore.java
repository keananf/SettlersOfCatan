package AI;

import client.ClientGame;
import client.Turn;
import game.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public abstract class AICore implements IAI, Runnable
{
	private AIClient client;
	private Random rand;

	public AICore(AIClient client)
	{
		this.client = client;
		this.rand = new Random();
	}

	@Override
	public void run()
	{
		// Loop performing turns when needed
		while(true)
		{
			try
			{
				getGameLock().acquire();
				if(getGame() == null)
				{
					continue;
				}
				if(getGame().isOver())
				{
					break;
				}
				try
				{
					getTurnLock().acquire();

					// If it is the player's turn, OR they have an expected move
					if(getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()) ||
							!getTurn().getExpectedMoves().isEmpty())
					{
						try
						{
							performMove();
						}
						finally
						{
							getTurnLock().release();
						}
					}
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				finally
				{
					getGameLock().release();
				}
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			// Sleep for 1 second
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void performMove()
	{
		Turn turn = selectAndPrepareMove();
		updateTurn(turn);
		client.sendTurn();
	}

	/**
	 * Top-level method for choosing move out of available choices.
	 */
	private Turn selectAndPrepareMove()
	{
		List<Turn> optimalMoves = new ArrayList<Turn>();
		int maxRank = -1;

		// Filter out poor moves, based upon rank
		for(Turn entry : getMoves())
		{
			// Implementation-defined
			int rank = rankMove(entry);

			if(rank > maxRank)
			{
				maxRank = rank;
				optimalMoves.clear();
				optimalMoves.add(entry);
			}
			else if(rank == maxRank)
			{
				optimalMoves.add(entry);
			}
		}

		// Prepare turn object
		return optimalMoves != null && optimalMoves.size() > 0 ? selectMove(optimalMoves) : null;
	}

	/**
	 * Chooses the turn with the highest rank
	 * @param optimalMoves the list of turns that share the highest rank.
	 * @return the turn with the highest rank
	 */
	@Override
	public Turn selectMove(List<Turn> optimalMoves)
	{
		// Randomly choose one of the highest rank
		return optimalMoves != null && optimalMoves.size() > 0
				? optimalMoves.get(rand.nextInt(optimalMoves.size())) : null;
	}

	/**
	 * @return a list of Turn objects, entailing move type and additional info.
	 */
	protected List<Turn> getMoves()
	{
		return client.getMoveProcessor().getPossibleMoves();
	}

	/**
	 * Updates the client's turn object
	 * @param selectedMove this move and corresponding information
	 */
	private void updateTurn(Turn selectedMove)
	{
		// Reset and set chosen field
		getTurn().reset();
		getTurn().setChosenMove(selectedMove.getChosenMove());

		// Set additional fields
		switch (selectedMove.getChosenMove())
		{
			case SUBMITTRADERESPONSE:
				getTurn().setTradeResponse(selectedMove.getTradeResponse());
				break;
			case CHOOSERESOURCE:
				getTurn().setChosenResource(selectedMove.getChosenResource());
				break;
			case MOVEROBBER:
				getTurn().setChosenHex(selectedMove.getChosenHex());
				break;
			case PLAYDEVCARD:
				getTurn().setChosenCard(selectedMove.getChosenCard());
				break;
			case BUILDROAD:
				getTurn().setChosenEdge(selectedMove.getChosenEdge());
				break;
			case CHATMESSAGE:
				getTurn().setChatMessage(selectedMove.getChatMessage());
				break;
			case DISCARDRESOURCES:
				getTurn().setChosenResources(selectedMove.getChosenResources());
				break;
			case INITIATETRADE:
				getTurn().setPlayerTrade(selectedMove.getPlayerTrade());
				break;
			case SUBMITTARGETPLAYER:
				getTurn().setTarget(selectedMove.getTarget());
			case BUILDSETTLEMENT:
			case BUILDCITY:
				getTurn().setChosenNode(selectedMove.getChosenNode());
				break;

			// Empty request bodies
			case JOINLOBBY:
			case ROLLDICE:
			case ENDTURN:
			case BUYDEVCARD:
			default:
				break;
		}
	}
	
	protected Player getPlayer()
	{
		return getGame().getPlayer();
	}

	protected ClientGame getGame()
	{
		return client.getState();
	}

	public Turn getTurn()
	{
		return client.getTurn();
	}

	protected Semaphore getGameLock()
	{
		return client.getStateLock();
	}

	protected Semaphore getTurnLock()
	{
		return client.getTurnLock();
	}
}
