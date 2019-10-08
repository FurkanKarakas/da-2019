
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class Process extends Thread {
	private InetAddress ip;
	private Integer port;
	private Integer processId;
	private DatagramSocket socket;
	static int messageID = 0;

	public InetAddress getIp() {
		return ip;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public Integer getPort() {
		return port;
	}

	public Integer getProcessId() {
		return processId;
	}

	public void setProcessId(Integer processId) {
		this.processId = processId;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
	
	public DatagramSocket createSocket() {
		System.out.println(this.ip);
		System.out.println(this.port);
		try {
			if (this.socket == null) {
				this.socket = new DatagramSocket(this.port, this.ip);
			}
			else if (this.socket.isBound() == false)
				this.socket = new DatagramSocket(this.port, this.ip);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.socket;
	}
	
	public void closeSocket() {
		this.socket.close();
	}

	public Process(InetAddress ip, Integer processId, Integer port) {
		this.port = port;
		this.processId = processId;
		this.ip = ip;

		SigHandlerUsr2 sigHandlerUsr2 = new SigHandlerUsr2(this);
		SigHandlerInt sigHandlerTerm = new SigHandlerInt(this);
		SigHandlerTerm sigHandlerInt = new SigHandlerTerm(this);

		Signal signalTerm = new Signal("TERM");
		Signal signalInt = new Signal("INT");
		//Signal signalUsr2 = new Signal("USR2");

		//Signal.handle(signalInt, sigHandlerInt);
		//Signal.handle(signalTerm, sigHandlerTerm);
		//Signal.handle(signalUsr2, sigHandlerUsr2);

		this.start();
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				// exception
			}
		}
	}

	public void sendMessage(String m, Process pj) {

	}

	@SuppressWarnings("deprecation")
	public static class SigHandlerUsr2 implements SignalHandler {
		Process p;

		private SigHandlerUsr2(Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {

			System.out.format("Handling signal: %s\n", signal.toString());
		}
	}

	@SuppressWarnings("deprecation")
	public static class SigHandlerTerm implements SignalHandler {
		Process p;

		private SigHandlerTerm(Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			System.out.format("Handling signal: %s\n", signal.toString());
			p.interrupt();
		}
	}

	@SuppressWarnings("deprecation")
	public static class SigHandlerInt implements SignalHandler {
		Process p;

		private SigHandlerInt(Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			System.out.format("Handling signal: %s\n", signal.toString());
			p.interrupt();
		}
	}
}
