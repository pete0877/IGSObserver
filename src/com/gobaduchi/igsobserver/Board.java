package com.gobaduchi.igsobserver;

import com.gobaduchi.igsobserver.util.LogProxy;

public class Board {

	private static LogProxy log = new LogProxy(Board.class.getName());

	public final static int BOARD_SIZE = 19;

	public enum SPOT {
		EMPTY, WHITE, BLACK
	};

	public SPOT[][] spots = new SPOT[BOARD_SIZE][BOARD_SIZE];

	public Board() {
		clear();
	}

	public void clear() {
		for (int x = 0; x < BOARD_SIZE; x++)
			for (int y = 0; y < BOARD_SIZE; y++)
				spots[x][y] = SPOT.EMPTY;
	}

	public void placeHandicapStones(int howMany) throws Exception {

		log.debug("placing " + howMany + " handical stones");

		if (howMany < 0 || howMany > 9)
			throw new Exception("Cannot place this many handicap stones: "
					+ howMany);

		if (howMany == 0)
			return;

		if (howMany >= 1) {
			spots[15][3] = SPOT.BLACK;
		}
		if (howMany >= 2) {
			spots[3][15] = SPOT.BLACK;
		}
		if (howMany >= 3) {
			spots[15][15] = SPOT.BLACK;
		}
		if (howMany >= 4) {
			spots[3][3] = SPOT.BLACK;
		}
		if (howMany == 5 || howMany == 7 || howMany == 9) {
			spots[9][9] = SPOT.BLACK;
		}
		if (howMany >= 6) {
			spots[3][9] = SPOT.BLACK;
			spots[15][9] = SPOT.BLACK;
		}
		if (howMany >= 8) {
			spots[9][3] = SPOT.BLACK;
			spots[9][15] = SPOT.BLACK;
		}

	}

	@Override
	public String toString() {

		StringBuilder result = new StringBuilder();

		for (int y = 0; y < BOARD_SIZE; y++) {
			for (int x = 0; x < BOARD_SIZE; x++) {
				if (spots[x][y].equals(SPOT.EMPTY))
					result.append("+");
				else if (spots[x][y].equals(SPOT.WHITE))
					result.append("#");
				else if (spots[x][y].equals(SPOT.BLACK))
					result.append("0");

			}
			result.append("\n");

		}

		return result.toString();
	}

}
