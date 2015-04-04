package com.gobaduchi.igsobserver;

import java.util.ArrayList;

import com.gobaduchi.igsobserver.util.LogProxy;

public class Move {

	public enum PLAYER {
		WHITE, BLACK
	};

	private static LogProxy log = new LogProxy(Move.class.getName());

	// Starts at 1 being the first move
	public int moveNumber;

	public PLAYER player;

	// Position of the move (null if passing move);
	public Position position;

	public ArrayList<Position> captures = new ArrayList<Position>();

	// If user passed, position is null
	public boolean pass = false;

	// Returns null if parsing fails for an reason
	public static Move parse(String s) {

		log.debug("parse called with: " + s);

		if (s == null || s.length() == 0)
			return null;

		try {
			Move result = new Move();

			// Format: 7 move-number(B/W): XY
			String dataString = new String(s);
			while (dataString.indexOf("  ") >= 0)
				dataString = dataString.replaceAll("  ", " ");

			String[] dataArray = dataString.split(" ");

			if (dataArray.length < 3) {
				log.debug("could not parse: dataArray.length is less than 3");
				return null;
			}

			String responseCodeString = dataArray[0].trim();
			String numberPlayerString = dataArray[1].trim();
			String moveXYString = dataArray[2].trim();

			// All move lines come with response code of '15':
			if (!responseCodeString.equals("15")) {
				log.debug("could not parse: response code is not 15: '"
						+ responseCodeString + "'");
				return null;
			}

			// Try to parse the move number and player indicator (black vs
			// white).
			// example numberPlayerString is '7(B):'
			if (numberPlayerString.length() < 5) {
				log.debug("could not parse: move number + player substring less than 5 in length");
				return null;
			}

			String last4CharOfPlayer = numberPlayerString.substring(
					numberPlayerString.length() - 4,
					numberPlayerString.length());

			if (last4CharOfPlayer.equals("(B):")) {
				result.player = PLAYER.BLACK;
			} else if (last4CharOfPlayer.equals("(W):")) {
				result.player = PLAYER.WHITE;
			} else {
				log.debug("could not parse: player ID: '" + last4CharOfPlayer
						+ "'");
				return null;
			}

			String moveNumberString = numberPlayerString.substring(0,
					numberPlayerString.length() - 4);
			try {
				result.moveNumber = Integer.parseInt(moveNumberString);
			} catch (Exception error) {
				log.debug("could not parse: move number: '" + moveNumberString
						+ "'");
				return null;
			}

			if (result.moveNumber < 1) {
				log.debug("not a valid move number (could be handicap move # 0)");
				return null;
			}

			if (moveXYString.equals("Pass")) {

				result.pass = true;

			} else {

				if (moveXYString.length() < 2 || moveXYString.length() > 3) {
					log.debug("could not parse: moveXYString.length expected to be 2 or 3 chars or 'Pass'");
					return null;
				}

				int x = (int) moveXYString.charAt(0) - 65;
				if (x < 0 || x > 18) {
					log.debug("could not parse: x coordinate is < 0 or > 18");
					return null;
				}

				String yCoordinate = moveXYString.substring(1,
						moveXYString.length());

				int y = -1;
				try {
					y = Integer.parseInt(yCoordinate) - 1;
				} catch (Exception error) {
					log.debug("could not parse: Y-coordinate string: '"
							+ yCoordinate + "'");
					return null;
				}
				if (y < 0 || y > 18) {
					log.debug("could not parse: y coordinate is < 0 or > 18");
					return null;
				}

				result.position = new Position(x, y);

				// Analyze any captured stones:

				if (dataArray.length > 3) {

					for (int n = 3; n < dataArray.length; n++) {
						String captureXYString = dataArray[n].trim();

						if (captureXYString.length() < 2
								|| captureXYString.length() > 3) {
							log.debug("could not parse: captureXYString.length expected to be 2 or 3");
							return null;
						}

						x = (int) captureXYString.charAt(0) - 65;
						if (x < 0 || x > 18) {
							log.debug("could not parse: x coordinate is < 0 or > 18");
							return null;
						}

						yCoordinate = captureXYString.substring(1,
								captureXYString.length());

						try {
							y = Integer.parseInt(yCoordinate) - 1;
						} catch (Exception error) {
							log.debug("could not parse: Y-coordinate string: '"
									+ yCoordinate + "'");
							return null;
						}

						if (y < 0 || y > 18) {
							log.debug("could not parse: y coordinate is < 0 or > 18");
							return null;
						}

						result.captures.add(new Position(x, y));

					}
				}

			}

			return result;

		} catch (Exception error) {
			log.error("could not parse: " + s, error);
			return null;
		}
	}

	@Override
	public String toString() {
		String result = "";
		result += "MOVE: " + this.moveNumber;
		result += "   PLAYER: " + (player == PLAYER.BLACK ? "black" : "white");
		result += "   " + (pass == true ? "pass" : position);
		if (captures != null && captures.size() > 0) {
			result += "   CAPTURES:";
			for (Position capturePosition : captures)
				result += capturePosition;
		}

		return result;
	}
}
