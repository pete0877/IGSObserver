package com.gobaduchi.igsobserver;

import java.util.HashMap;

import com.gobaduchi.igsobserver.util.LogProxy;

public class MoveSequence {

	private static LogProxy log = new LogProxy(
			MoveSequence.class.getName());

	private HashMap<Integer, Move> sequence = new HashMap<Integer, Move>();

	// Zero means no moves recorded yet:
	private int largestMoveNumberRecorded = 0;

	// Flag to indicate if the move set is complete (all the moves are recorded
	// up to the largest move) - meaning, there are no gaps in recording.
	private boolean recordingComplete = true;

	public boolean isRecordingComplete() {
		return recordingComplete;
	}
	
	public HashMap<Integer, Move> getSequence() {
		return sequence;
	}

	public void recordMove(Move move) {
		sequence.put(move.moveNumber, move);

		if (move.moveNumber > largestMoveNumberRecorded) {
			largestMoveNumberRecorded = move.moveNumber;
		}

		evaluateCompletion();

	}

	private void evaluateCompletion() {

		if (largestMoveNumberRecorded == 0) {

			log.debug("evaluateCompletion: find no moves yet. record is complete");
			recordingComplete = true;
			return;
		}

		// Assume the record is complete and try to find holes:
		recordingComplete = true;

		// Go through all move numbers starting with 1 and ending at the largest
		// move number recorded. If a move is found to be missing, we know the
		// record is not complete:
		for (int n = 1; n <= largestMoveNumberRecorded; n++) {
			if (!sequence.containsKey(n)) {

				log.debug("evaluateCompletion: sequence of "
						+ largestMoveNumberRecorded
						+ " is not complete. Missing move # " + n);

				recordingComplete = false;
				return;
			}
		}

		log.debug("evaluateCompletion: sequence of "
				+ largestMoveNumberRecorded + " is complete");

	}
}
