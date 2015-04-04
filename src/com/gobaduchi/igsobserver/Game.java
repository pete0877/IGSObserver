package com.gobaduchi.igsobserver;

import java.util.ArrayList;

import com.gobaduchi.igsobserver.util.LogProxy;

public class Game {

	private static LogProxy log = new LogProxy(Game.class.getName());

	// IGS game ID
	public int gameID = 0;

	// Username of the white player
	public String whiteName = "";

	// Username of the black player
	public String blackName = "";

	// Zero means unknown. 1 means 1dan. -1 means 1kyu.
	public int whiteRank = 0;

	// Zero means unknown. 1 means 1dan. -1 means 1kyu.
	public int blackRank = 0;

	// Moves observed so far
	public int moveCount = 0;

	// Array of moves
	public ArrayList<Move> moves = new ArrayList<Move>();

	// Handicap. Zero means even game.
	public int handicap = 0;

	// Number of white stones that the black player captured
	public int whiteStonesCaptured = 0;

	// Number of black stones that the white player captured
	public int blackStonesCaptured = 0;

	// Game board
	public Board board = new Board();

	// Game outcome (null until game is over)
	public GameOutcome outcome;

	// Board size. E.g. 19 for 19x19 board.
	public int size = -1;

	// Returns null if parsing fails for an reason
	public static Game parse(String s) {

		log.debug("parse called with: " + s);

		if (s == null || s.length() == 0)
			return null;

		try {
			Game result = new Game();

			// Format: [id] white [r] vs. black [r] (moves size handicap komi
			// ...)
			String[] a = s.split("\\]");

			if (a.length != 4) {
				log.debug("cannot parse: a.length expected to be 4 but is "
						+ a.length);
				return null;
			}

			// Try to parse out the first part to extract game ID:
			String gameIDString = a[0];
			String[] gameIDStringArray = gameIDString.split("\\[");
			if (gameIDStringArray.length != 2) {
				log.debug("cannot parse: gameIDStringArray.length expected to be 2 but is "
						+ gameIDStringArray.length);
				return null;
			}

			Integer id = 0;
			try {
				id = Integer.parseInt(gameIDStringArray[1].trim());
			} catch (Exception error2) {
			}

			if (id == 0) {
				log.debug("cannot parse: bad game id: '" + gameIDStringArray[1]
						+ "'");

				return null;
			}

			log.debug("game ID: " + id);
			result.gameID = id;

			// Determine white player name and rank:
			String whitePlayerNameRankString = a[1];
			String[] whitePlayerNameRankArray = whitePlayerNameRankString
					.split("\\[");

			if (whitePlayerNameRankArray.length != 2)
				return null;

			String whitePlayerName = whitePlayerNameRankArray[0].trim();
			String whitePlayerRankString = whitePlayerNameRankArray[1].trim();

			log.debug("white: " + whitePlayerName);
			log.debug("white rank: " + whitePlayerRankString);

			result.whiteName = whitePlayerName;
			result.whiteRank = parseRank(whitePlayerRankString);

			// Determine black player name and rank:
			String blackPlayerNameRankString = a[2];
			String[] blackPlayerNameRankArray = blackPlayerNameRankString
					.split("\\[");

			if (blackPlayerNameRankArray.length != 2)
				return null;

			String blackPlayerName = blackPlayerNameRankArray[0].trim();

			// Remove the 'vs.' from the black player's name:
			if (blackPlayerName.indexOf("vs.") >= 0)
				blackPlayerName = blackPlayerName.substring(
						blackPlayerName.indexOf("vs.") + 3,
						blackPlayerName.length()).trim();

			String blackPlayerRankString = blackPlayerNameRankArray[1].trim();

			log.debug("black: " + blackPlayerName);
			log.debug("black rank: " + blackPlayerRankString);

			result.blackName = blackPlayerName;
			result.blackRank = parseRank(blackPlayerRankString);

			// Other game parameters:
			String gameParamsString = a[3];

			gameParamsString = gameParamsString.replaceAll("\\(", " ");
			gameParamsString = gameParamsString.replaceAll("\\)", " ");
			gameParamsString = gameParamsString.replaceAll("\t", " ");

			while (gameParamsString.indexOf("  ") >= 0)
				gameParamsString = gameParamsString.replaceAll("  ", " ");

			log.debug("game info params: '" + gameParamsString + "'");

			String[] gameParamsArray = gameParamsString.split(" ");
			if (gameParamsArray.length < 4) {
				log.debug("cannot parse: gameParamsArray.length expected to at last 3 but is "
						+ gameParamsArray.length);
				return null;
			}

			try {
				result.moveCount = Integer.parseInt(gameParamsArray[1]);
			} catch (Exception error) {
				log.debug("cannot parse: move parse error: '"
						+ gameParamsArray[1] + "'");
				return null;
			}

			try {
				result.size = Integer.parseInt(gameParamsArray[2]);
			} catch (Exception error) {
				log.debug("cannot parse: size parse error: '"
						+ gameParamsArray[2] + "'");
				return null;
			}

			try {
				result.handicap = Integer.parseInt(gameParamsArray[3]);
			} catch (Exception error) {
				log.debug("cannot parse: handicap parse error: '"
						+ gameParamsArray[3] + "'");
				return null;
			}

			return result;

		} catch (Exception error) {
			log.debug("could not parse: " + s);
			return null;
		}
	}

	private static int parseRank(String s) {

		log.debug("trying to parse rank: '" + s + "'");

		int result = 0;
		int rankTypePosition = 0;

		if (s.indexOf("d") > 0) {
			rankTypePosition = s.indexOf("d");
			result = 1;
		}
		if (s.indexOf("k") > 0) {
			rankTypePosition = s.indexOf("k");
			result = -1;
		}

		// Neither Dan or Kyu player so we just return zero:
		if (result == 0)
			return result;

		String numberString = s.substring(0, rankTypePosition).trim();
		try {
			result = result * Integer.parseInt(numberString);
		} catch (Exception error) {
			result = 0;
		}

		return result;

	}

	@Override
	public String toString() {
		String result = "";
		result += "GAME ID: " + this.gameID;
		result += "   MOVES: " + this.moveCount;
		result += "   SIZE: " + this.size;
		result += "   HANDICAP: " + this.handicap;
		result += "   WHITE: " + this.whiteName + " (" + this.whiteRank + ")";
		result += "   BLACK: " + this.blackName + " (" + this.blackRank + ")";
		result += "   WHITE STONES CAPTURED: " + this.whiteStonesCaptured;
		result += "   BLACK STONES CAPTURED: " + this.blackStonesCaptured;

		return result;
	}

	public String getSummary() {
		String result = "";
		result += "W: "
				+ Math.abs(whiteRank)
				+ (whiteRank > 0 ? " dan" : " kyu")
				+ " - "
				+ whiteName
				+ (blackStonesCaptured > 0 ? " (cap "
						+ blackStonesCaptured + ")" : "");
		result += "\n";
		result += "B: "
				+ Math.abs(blackRank)
				+ (blackRank > 0 ? " dan" : " kyu")
				+ " - "
				+ blackName
				+ (whiteStonesCaptured > 0 ? " (cap "
						+ whiteStonesCaptured + ")" : "");
		result += "\n";
		
		if (moveCount > 0)
			result += moveCount + " moves";
		
		return result;
	}
}
