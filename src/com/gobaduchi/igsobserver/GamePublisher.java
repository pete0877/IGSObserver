package com.gobaduchi.igsobserver;

public interface GamePublisher {
	
	void startNewGame(Game game) throws Exception;
	void processMoves(MoveSequence moves) throws Exception;
	void processResult(GameOutcome outcome) throws Exception;

	void addGameSubscriber(GameSubscriber subscriber);
	void removeAllSubscribers();
	
}
