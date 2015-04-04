package com.gobaduchi.igsobserver;

import java.util.ArrayList;

import com.gobaduchi.igsobserver.util.LogProxy;

public class GamePublisherImpl implements GamePublisher {

	private static LogProxy log = new LogProxy(
			GamePublisherImpl.class.getName());
	
	private Game game;
	private int movesProcessed;
	private boolean newGame;

	ArrayList<GameSubscriber> subscribers = new ArrayList<GameSubscriber>();

	@Override
	public void startNewGame(Game game) throws Exception {
		this.game = game;
		this.movesProcessed = 0;
		this.newGame = true;

		if (game.handicap > 0)
			this.game.board.placeHandicapStones(this.game.handicap);

	}

	// The given move sequence is guaranteed to be complete (no holes in moves)
	@Override
	public void processMoves(MoveSequence moves) throws Exception {

		// Check if we processed all the moves we are being given:
		if (moves.getSequence().size() <= movesProcessed)
			return;

		for (int n = movesProcessed + 1; n <= moves.getSequence().size(); n++) {
			processMove(moves.getSequence().get(n));
		}
		
		movesProcessed = moves.getSequence().size();

		announceGameUpdate();

	}

	private void processMove(Move move) throws Exception {
		
		game.moves.add(move);
		game.moveCount++;
		
		// If player passed, there is nothing more to do here
		if (move.pass) {
			log.debug("pass-move detected");
			return;
		}
		
		// Check which color stone was placed:
		if (move.player == Move.PLAYER.BLACK) {
			game.board.spots[move.position.x][move.position.y] = Board.SPOT.BLACK;
			log.debug("black captured " + move.captures.size() +  " stones");
			for (Position capturePosition : move.captures) {
				game.whiteStonesCaptured++;
				game.board.spots[capturePosition.x][capturePosition.y] = Board.SPOT.EMPTY;
			}
		} else {
			game.board.spots[move.position.x][move.position.y] = Board.SPOT.WHITE;
			log.debug("white captured " + move.captures.size() +  " stones");			
			for (Position capturePosition : move.captures) {
				game.blackStonesCaptured++;
				game.board.spots[capturePosition.x][capturePosition.y] = Board.SPOT.EMPTY;
			}			
		}
	}

	@Override
	public void processResult(GameOutcome outcome) throws Exception {

		game.outcome = outcome;
		announceGameUpdate();
	}

	@Override
	public void addGameSubscriber(GameSubscriber subscriber) {
		if (subscriber != null)
			subscribers.add(subscriber);
	}

	private void announceGameUpdate() {
		for (GameSubscriber subscriber : subscribers) {
			subscriber.update(game, newGame);
		}
		newGame = false;
	}

	@Override
	public void removeAllSubscribers() {
		subscribers.clear();
	}
}
