package AI;

import client.ClientGame;
import client.Turn;
import client.TurnState;
import game.players.Player;
import intergroup.Events;
import intergroup.Requests;

import java.util.*;

public abstract class AICore implements IAI, Runnable
{
	protected AIClient client;
	private Random rand;
	private boolean waiting;
	private ArrayList<Events.Event.TypeCase> expectedEventPossibilities;
	private Map<Requests.Request.BodyCase, ArrayList<Events.Event.TypeCase>> expectedEvents;

	public AICore(AIClient client)
	{
		this.client = client;
		this.rand = new Random();
		expectedEvents = setUpExpectedEvents();
	}

	@Override
	public void run()
	{
		client.log("Client Play", "Starting AI client loop");
		waiting = false;

		// Loop sending turns
		while (client.isActive() && (getState() == null || !getState().isOver()))
		{
			try
			{
				// Attempt to make a move and send a turn
				if (!waiting)
				{
					waiting = client.acquireLocksAndPerformMove();
				}
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				client.shutDown();
			}
		}
		client.log("Client Play", "Ending AI client loop");
	}

	@Override
	public boolean performMove()
	{
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

			expectedEventPossibilities = expectedEvents.get(turn.getChosenMove());
			return client.sendTurn(turn);
		}
		return false;
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
		// Switch on turn type and rank move
		switch (turn.getChosenMove())
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
			// Set the chosenResources in 'turn' to be a valid discard as well
			// as rank.
			return rankDiscard(turn);
		case SUBMITTARGETPLAYER:
			return rankTargetPlayer(turn.getTarget());
		case CHOOSERESOURCE:
			return rankChosenResource(turn.getChosenResource());

		// Should rank apply for ENDTURN / ROLLDICE? Maybe sometimes..
		case ENDTURN:
		case ROLLDICE:
			break;

		// ai will never chat
		case CHATMESSAGE:
			break;

		// If Join Lobby, then the ai has to join a lobby and the rest of the
		// list will be empty
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
		return optimalMoves != null && optimalMoves.size() > 0 ? optimalMoves.get(rand.nextInt(optimalMoves.size()))
				: null;
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
			if (t.getChosenMove().equals(Requests.Request.BodyCase.CHATMESSAGE)) ret.remove(t);
		}

		return ret;
	}

	/**
	 * @return a map of request types to expected event types
	 */
	private Map<Requests.Request.BodyCase, ArrayList<Events.Event.TypeCase>> setUpExpectedEvents()
	{
		Map<Requests.Request.BodyCase, ArrayList<Events.Event.TypeCase>> evs = new HashMap<Requests.Request.BodyCase, ArrayList<Events.Event.TypeCase>>();
		for (Requests.Request.BodyCase c : Requests.Request.BodyCase.values())
		{
			evs.put(c, new ArrayList<Events.Event.TypeCase>());
		}

		evs.get(Requests.Request.BodyCase.BUILDCITY).add(Events.Event.TypeCase.CITYBUILT);
		evs.get(Requests.Request.BodyCase.BUILDROAD).add(Events.Event.TypeCase.ROADBUILT);
		evs.get(Requests.Request.BodyCase.BUILDSETTLEMENT).add(Events.Event.TypeCase.SETTLEMENTBUILT);
		evs.get(Requests.Request.BodyCase.BUYDEVCARD).add(Events.Event.TypeCase.DEVCARDBOUGHT);
		evs.get(Requests.Request.BodyCase.PLAYDEVCARD).add(Events.Event.TypeCase.DEVCARDPLAYED);
		evs.get(Requests.Request.BodyCase.ENDTURN).add(Events.Event.TypeCase.TURNENDED);
		evs.get(Requests.Request.BodyCase.INITIATETRADE).add(Events.Event.TypeCase.PLAYERTRADEINITIATED);
		evs.get(Requests.Request.BodyCase.ROLLDICE).add(Events.Event.TypeCase.ROLLED);
		evs.get(Requests.Request.BodyCase.MOVEROBBER).add(Events.Event.TypeCase.ROBBERMOVED);
		evs.get(Requests.Request.BodyCase.DISCARDRESOURCES).add(Events.Event.TypeCase.CARDSDISCARDED);
		evs.get(Requests.Request.BodyCase.SUBMITTRADERESPONSE).add(Events.Event.TypeCase.PLAYERTRADEACCEPTED);
		evs.get(Requests.Request.BodyCase.SUBMITTRADERESPONSE).add(Events.Event.TypeCase.PLAYERTRADEREJECTED);
		evs.get(Requests.Request.BodyCase.CHATMESSAGE).add(Events.Event.TypeCase.CHATMESSAGE);
		evs.get(Requests.Request.BodyCase.JOINLOBBY).add(Events.Event.TypeCase.LOBBYUPDATE);
		evs.get(Requests.Request.BodyCase.CHOOSERESOURCE).add(Events.Event.TypeCase.RESOURCECHOSEN);
		evs.get(Requests.Request.BodyCase.SUBMITTARGETPLAYER).add(Events.Event.TypeCase.RESOURCESTOLEN);

		return evs;
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

	public void resume()
	{
		waiting = false;
	}

	public ArrayList<Events.Event.TypeCase> getExpectedEvents()
	{
		return expectedEventPossibilities;
	}
}
