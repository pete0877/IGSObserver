package com.gobaduchi.igsobserver.util;

public interface SocketCommunicator {

	void setServer(String host, int port);
	void openCommunication() throws Exception;
	void closeCommunication();
	String receiveLine() throws Exception;
	void sendLine(String line) throws Exception;
		
}
