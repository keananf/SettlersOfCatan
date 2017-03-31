package AI;

import client.ClientGame;
import client.Turn;
import client.TurnState;
import game.players.Player;
import intergroup.Requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import com.badlogic.gdx.Gdx;

public abstract class AICore implements IAI
{
	protected AIClient client;
	private Random rand;
	protected boolean hasTraded;
	Semaphore semaphore = new Semaphore(1);

	public AICore(AIClient client)
	{
		this.client = client;
		this.rand = new Random();
		hasTraded = false;
	}

	@Override
	public void performMove()
	{
		/*if (getPlayer() != null && getPlayer().getId() != null)
			client.log("Client Play", String.format("Making move for %s", getPlayer().getId().name()));*/
		Turn turn = selectAndPrepareMove();
		if (turn != null)
		{
			if (getPlayer() != null && getPlayer().getId() != null)
			{
				client.log("Client Play", String.format("%s expected moves %s", getPlayer().getId().name(),
						getTurn().getExpectedMoves().toString()));
				client.log("Client Play",
						String.format("%s Chose move %s", getPlayer().getId().name(), turn.getChosenMove().name()));
			}
			client.sendTurn(turn);
		}
		/*else
		{
			client.log("Client Play", "No move");
		}*/

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

		if (moves == null) return null;

		// Filter out the best moves, based upon assigned rank
		for (Turn entry : moves)
		{
			// Implementation-defined
			int rank = rankMove(entry);
			Gdx.app.debug(entry.getChosenMove().toString(), ""  + rank);
			if (rank > maxRank)
			{
				maxRank = rank;
				optimalMoves.clear();
				optimalMoves.add(entry);
			}
			else if (rank == maxRank)
			{
				optimalMoves.add(entry);
			}
		}

		return optimalMoves;
	}

	@Override
	public int rankMove(Turn turn)
	{
		int rank = -999;
		Gdx.app.debug("MessageType", turn.getChosenMove().toString());
		// Switch on turn type and rank move

		switch (turn.getChosenMove())
		{
		case BUYDEVCARD:
			rank = rankBuyDevCard();
			break;
		case BUILDROAD:
			rank = rankNewRoad(turn.getChosenEdge());
			break;
		case BUILDSETTLEMENT:
			rank = rankNewSettlement(turn.getChosenNode());
			break;
		case BUILDCITY:
			rank = rankNewCity(turn.getChosenNode());
			break;
		case MOVEROBBER:
			rank = rankNewRobberLocation(turn.getChosenHex());
			break;
		case PLAYDEVCARD:
			rank = rankPlayDevCard(turn.getChosenCard());
			break;
		case INITIATETRADE:
			// Set the player or bank trade in 'turn' as well
			Gdx.app.debug("INITIATETRADE", "Found");
			Gdx.app.debug("RankBefore", "" + rank);
			rank = rankInitiateTrade(turn);
			Gdx.app.debug("RankAfter", "" + rank);
			break;
		case SUBMITTRADERESPONSE:
			rank = rankTradeResponse(turn.getTradeResponse(), turn.getPlayerTrade());
			break;
		case DISCARDRESOURCES:
			// If a discard move has gotten this for, then we know it is
			// an expected move.
			// Set the chosenResources in 'turn' to be a valid discard as well
			// as rank.
			rank = rankDiscard(turn);
			break;
		case SUBMITTARGETPLAYER:
			rank = rankTargetPlayer(turn.getTarget());
			break;
		case CHOOSERESOURCE:
			rank = rankChosenResource(turn.getChosenResource());
			break;

			// Should rank apply for ENDTURN / ROLLDICE? Maybe sometimes..
		case ENDTURN:
			rank = rankEndTurn();
			break;
		case ROLLDICE:
			rank = 6;
			break;

			// ai will never chat
		case CHATMESSAGE:
			rank = -1;
			break;

			// If Join Lobby, then the ai has to join a lobby and the rest of the
			// list will be empty
			// So, it's rank doesn't matter
		case JOINLOBBY:
			rank = 6;
			break;
		case BODY_NOT_SET:
		default:
			break;
		}
		
		
		return rank;
	}

	@Override
	public Turn selectMove(List<Turn> optimalMoves)
	{
		// Randomly choose one of the highest rank
		if (optimalMoves == null) return null;
		if (optimalMoves.size() < 1) return null;
		return optimalMoves.get(rand.nextInt(optimalMoves.size()));
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
		for (Turn t : options)
		{
			if (t.getChosenMove().equals(Requests.Request.BodyCase.CHATMESSAGE))
				ret.remove(t);
		}

		return ret;
	}

	protected Player getPlayer()
	{
		return client.getPlayer() == null ? null : client.getPlayer();
	}

	protected ClientGame getState()
	{
		return client.getState();
	}

	public TurnState getTurn()
	{
		return client.getTurn();
	}
	
	private void cleanup()
	{
		hasTraded = false;
	}
}
