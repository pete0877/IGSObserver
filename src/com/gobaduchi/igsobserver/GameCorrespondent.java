package com.gobaduchi.igsobserver;

import com.gobaduchi.igsobserver.util.LogProxy;
import com.gobaduchi.igsobserver.util.SocketCommunicator;

public class GameCorrespondent {

	private static LogProxy log = new LogProxy(
			GameCorrespondent.class.getName());

	public final static int MAX_MOVES = 20;
	public final static int MIN_WHITE_RANK = 1;
	public final static int ERROR_SLEEP_TIME = 3000;
	public final static int THREAD_SIGNAL_WAIT_TIME = 250;

	private SocketCommunicator communicator;
	private GamePublisher tracker;
	private IGSObserverThread thread;

	private boolean isRunning = false;
	private boolean shutDownRequested = false;

	public boolean isRunning() {
		return isRunning;
	}

	public SocketCommunicator getCommunicator() {
		return communicator;
	}

	public void setCommunicator(SocketCommunicator communicator) {
		this.communicator = communicator;
	}

	public GamePublisher getTracker() {
		return tracker;
	}

	public void setTracker(GamePublisher tracker) {
		this.tracker = tracker;
	}

	public void requestNewGame() {

		// This will hopefully cause an IO exception and unblock the thread:
		communicator.closeCommunication();

		thread.interrupt();

	}

	class IGSObserverThread extends Thread {

		@Override
		public void run() {

			isRunning = true;

			log.debug("thread starting");

			// Temporary variable used to read / write lines from the communicator
			String line;

			while (!shutDownRequested) {

				try {

					communicator.openCommunication();

					log.debug("sending guest login");
					communicator.sendLine("guest");

					log.debug("sending lots of flags");
					communicator.sendLine("toggle client on");
					communicator.sendLine("toggle looking off");
					communicator.sendLine("toggle open off");
					communicator.sendLine("toggle quiet on");
					communicator.sendLine("toggle chatter off");
					communicator.sendLine("toggle bell off");
					communicator.sendLine("toggle kibitz off");
					communicator.sendLine("toggle shout off");

					// Clear the output until we see acknowledgment from the
					// server
					// that shout is off:

					log.debug("waiting for last flag toggle notification...");
					while (!(line = communicator.receiveLine())
							.equals("9 Set shout to be False.")) {
					}

					log.debug("waiting for '1 5'");
					while (!(line = communicator.receiveLine()).equals("1 5")) {
					}

					Game selectedGame = null;
					while (!shutDownRequested && selectedGame == null) {

						// keep invoking the game-listing command until we get
						// at least one game record out:
						log.debug("sending games command");

						communicator.sendLine("games");

						int lineCount = 0;
						while (!shutDownRequested
								&& !(line = communicator.receiveLine())
										.equals("1 5")) {

							lineCount++;

							// Parse the game records only if we haven't found a
							// game we like yet:
							if (selectedGame == null) {

								Game gameInfo = Game.parse(line);
								if (gameInfo == null)
									continue;

								if (gameInfo.moveCount <= MAX_MOVES
										&& gameInfo.whiteRank >= MIN_WHITE_RANK) {
									selectedGame = gameInfo;
									log.debug("selected " + lineCount
											+ " th game: " + gameInfo);
								}

							}

							if (lineCount % 25 == 0)
								log.debug("processed line count: " + lineCount);

						}

						// If we didn't find a game, we retry:
						if (!shutDownRequested && selectedGame == null)
							log.debug("could not select a proper game. going to try again...");

					}

					if (!shutDownRequested) {

						// Reset the number of moves to zero because we haven't
						// actually observed any moves yet:
						selectedGame.moveCount = 0;

						// At this point selectedGame is definitely non-null and
						// we
						// can start observing it:
						tracker.startNewGame(selectedGame);

						communicator.sendLine("observe " + selectedGame.gameID);
						communicator.sendLine("moves " + selectedGame.gameID);

						MoveSequence moveSequence = new MoveSequence();
						while (!shutDownRequested) {

							line = communicator.receiveLine();

							Move m = Move.parse(line);
							if (m != null) {

								log.debug("got move: " + m);

								moveSequence.recordMove(m);

								if (moveSequence.isRecordingComplete())
									tracker.processMoves(moveSequence);
							}
						}

					}

				} catch (Exception error) {
					log.error("error found", error);

					try {
						Thread.sleep(ERROR_SLEEP_TIME);
					} catch (Exception error2) {
					}
				}

			}

			log.debug("thread quitting");

			isRunning = false;
		}
	}

	public void start() {

		log.debug("start called");

		if (thread != null) {
			stop();
		}

		shutDownRequested = false;

		thread = new IGSObserverThread();
		thread.start();

	}

	public void stop() {

		log.debug("stop called");

		if (thread == null)
			return;

		// Notify the thread to stop:
		shutDownRequested = true;
		
		// This will hopefully cause an IO exception and unblock the thread:
		communicator.closeCommunication();

		thread.interrupt();
		
		// Wait till the thread quits:
		boolean retry = true;
		while (retry) {
			try {

				log.debug("trying to stop the thread ... ");

				thread.join();
				retry = false;

			} catch (Exception e) {
				try {
					Thread.sleep(THREAD_SIGNAL_WAIT_TIME);
				} catch (Exception e2) {
				}
			}
		}

		thread = null;

		log.debug("thread stopped successfully");
	}
}
