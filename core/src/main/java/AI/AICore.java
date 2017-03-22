package AI;

import client.ClientGame;
import client.Turn;
import game.players.Player;
import intergroup.Requests;

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
		boolean val = false;
		// Loop performing turns when needed
		while(true)
		{
			if(val) break;
			try
			{
				getTurnLock().acquire();
				try
				{
					getGameLock().acquire();
					try
					{
						// If it is the player's turn, OR they have an expected move
						if(getGame() != null && !getGame().isOver())
						{
							client.log("Client Play", String.format("Acquired locks for move for player %s", getGame().getPlayer().getId().name()));
							performMove();
						}
						else if(getGame() != null && getGame().isOver())
						{
							val = true;
						}
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
				finally
				{
					getTurnLock().release();
				}
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			// Sleep for at least 2 seconds
			try
			{
				do
				{
					Thread.sleep(2000);
				}
				// Sleep while it is NOT your turn and while you do not have expected moves
				while(getGame() == null || (!getGame().getCurrentPlayer().equals(getGame().getPlayer().getColour()) &&
						getTurn().getExpectedMoves().isEmpty()));
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void performMove()
	{
		Turn turn = selectAndPrepareMove();
		if(turn != null)
		{
			client.log("Client Play", String.format("Chose move %s", turn.getChosenMove().name()));
			client.updateTurn(turn);
			client.sendTurn();
		}
		else
		{
			client.log("Client Play", String.format("No move"));
		}
	}

	/**
	 * Top-level method for choosing move out of available choices.
	 */
	private Turn selectAndPrepareMove()
	{
		List<Turn> optimalMoves = rankMoves(getMoves());

		// Prepare turn object
		return optimalMoves != null && optimalMoves.size() > 0 ? selectMove(optimalMoves) : null;
	}

	@Override
	public List<Turn> rankMoves(List<Turn> moves)
	{
		List<Turn> optimalMoves = new ArrayList<Turn>();
		int maxRank = -1;

		// Filter out the best moves, based upon assigned rank
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

		return optimalMoves;
	}

	@Override
	public int rankMove(Turn turn)
	{
		// Switch on turn type and rank move
		switch(turn.getChosenMove())
		{
			case BUYDEVCARD:
				return rankBuyDevCard();
			case BUILDROAD:
				return rankNewRoad(turn.getChosenEdge());
			case BUILDSETTLEMENT:
				return rankNewSettlement(turn.getChosenNode());
			case BUILDCITY:
				return rankNewCity(turn.getChosenNode());
			case MOVEROBBER:
				return rankNewRobberLocation(turn.getChosenHex());
			case PLAYDEVCARD:
				return rankPlayDevCard(turn.getChosenCard());
			case INITIATETRADE:
				// Set the player or bank trade in 'turn' as well
				return rankInitiateTrade(turn);
			case SUBMITTRADERESPONSE:
				return rankTradeResponse(turn.getTradeResponse(), turn.getPlayerTrade());
			case DISCARDRESOURCES:
				// If a discard move has gotten this for, then we know it is
				// an expected move.
				// Set the chosenResources in 'turn' to be a valid discard as well as rank.
				return rankDiscard(turn);
			case SUBMITTARGETPLAYER:
				return rankTargetPlayer(turn.getTarget());
			case CHOOSERESOURCE:
				return rankChosenResource(turn.getChosenResource());

			// Should rank apply for ENDTURN / ROLLDICE? Maybe sometimes..
			case ENDTURN:
			case ROLLDICE:
				break;

			// AI will never chat
			case CHATMESSAGE:
				break;

			// If Join Lobby, then the AI has to join a lobby and the rest of the list will be empty
			// So, it's rank doesn't matter
			case JOINLOBBY:
				break;
			case BODY_NOT_SET:
			default:
				break;
		}

		return 0;
	}

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
		List<Turn> options = client.getMoveProcessor().getPossibleMoves();
		List<Turn> ret = new ArrayList<Turn>();
		ret.addAll(options);

		// Eliminate trades and chats
		for(Turn t : options)
		{
			if(t.getChosenMove().equals(Requests.Request.BodyCase.CHATMESSAGE) ||
					t.getChosenMove().equals(Requests.Request.BodyCase.INITIATETRADE))
				ret.remove(t);
		}

		return ret;
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
