package com.gobaduchi.igsobserver.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SocketCommunicatorImpl implements SocketCommunicator {

	private static LogProxy log = new LogProxy(
			SocketCommunicatorImpl.class.getName());

	private String host;
	private int port;

	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	@Override
	public void setServer(String host, int port) {

		this.host = host;
		this.port = port;
	}

	@Override
	public void openCommunication() throws Exception {

		if (host == null || host.length() == 0 || port <= 0)
			throw new IllegalArgumentException(
					"host or port not set properly through setServer()");

		if (socket != null)
			closeCommunication();

		log.debug("going to try to establish connection with " + host + ":"
				+ port);

		try {
			InetAddress serverAddr = InetAddress.getByName(host);
			socket = new Socket(serverAddr, port);

			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);

			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			log.debug("connection established successfully");

		} catch (Exception error) {

			throw new Exception("while establishing communication with " + host
					+ ":" + port, error);
		}

	}

	@Override
	public String receiveLine() throws Exception {

		String line;
		try {

			if (reader == null)
				throw new Exception("socket input stream null");

			line = reader.readLine();

			if (line == null)
				throw new Exception("socket input stream ended obruptly");

		} catch (Exception error) {

			throw new Exception("while sending line", error);
		}

		return line;
	}

	@Override
	public void sendLine(String line) throws Exception {

		try {

			if (writer == null)
				throw new Exception("socket output stream null");

			writer.println(line);

		} catch (Exception error) {

			throw new Exception("while sending line", error);
		}
	}

	@Override
	public void closeCommunication() {

		log.debug("closeCommunication called");

		try {
			if (writer != null) {
				
				log.debug("closing the writer");
				
				// For some strange reason this becomes a blocking call:
				// writer.close();
			}
		} catch (Exception error) {
		}

		try {
			if (reader != null) {

				log.debug("closing the reader");

				// For some strange reason this becomes a blocking call:
				// reader.close();
			}
		} catch (Exception error) {
		}

		try {
			if (socket != null) {

				log.debug("closing the socket");
				
				socket.close();
			}
		} catch (Exception error) {
		}

		socket = null;
		writer = null;
		reader = null;

		log.debug("closeCommunication completed");

	}

}
