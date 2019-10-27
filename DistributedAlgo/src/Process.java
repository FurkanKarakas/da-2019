
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Process extends Thread {
	private InetAddress ip;
	private Integer port;
	private Integer processId;
	private DatagramSocket socket;

	private ArrayList<Message> sndMsgs = null;
	private Listener pListener = null;
	private Timer resendTimer = null;
	private Integer timeOut = 1000;

	class ResendMsgs extends TimerTask {
		private Process proc;
		
		public ResendMsgs(Process proc) {
			this.proc = proc;
		}
		public void run() {
			for (Message m : this.proc.getSndMsgs()) {
				try {
					PerfectLinks pl = new PerfectLinks(this.proc);
					pl.sendMessage(m);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Process(InetAddress ip, Integer processId, Integer port) throws SocketException {
		this.port = port;
		this.processId = processId;
		this.ip = ip;
		this.socket = new DatagramSocket(this.port, this.ip);
		
		this.sndMsgs = new ArrayList<Message>();
		this.resendTimer = new Timer();
		this.resendTimer.scheduleAtFixedRate(new ResendMsgs(this), timeOut, timeOut);

		SigHandlerTerm sigHandlerInt = new SigHandlerTerm(this);
		SigHandlerInt sigHandlerTerm = new SigHandlerInt(this);
		// SigHandlerUsr2 sigHandlerUsr2 = new SigHandlerUsr2(this);

		Signal signalInt = new Signal("INT");
		Signal signalTerm = new Signal("TERM");
		// Signal signalUsr2 = new Signal("USR2");

		Signal.handle(signalInt, sigHandlerInt);
		Signal.handle(signalTerm, sigHandlerTerm);
		// Signal.handle(signalUsr2, sigHandlerUsr2);

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

	public DatagramSocket create() throws SocketException {
		this.socket = new DatagramSocket(this.port, this.ip);
		return this.socket;
	}

	public void close() {
		this.socket.close();
	}

	public void listen() {
		this.pListener = new Listener(this);
		this.pListener.start();
	}

	public void addMsg(Message m) {
		if (!sndMsgs.contains(m))
			sndMsgs.add(m);
	}
	
	public void removeMsg(Message m) {
		sndMsgs.remove(m);
	}

	public static class SigHandlerUsr2 implements SignalHandler {
		Process p;

		private SigHandlerUsr2(Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {

			System.out.format("Handling signal: %s\n", signal.toString());
			p.interrupt();
			System.exit(0);
		}
	}

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
			System.exit(0);
		}
	}

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
			System.exit(0);
		}
	}

	public InetAddress getIp() {
		return ip;
	}
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getProcessId() {
		return processId;
	}
	public void setProcessId(Integer processId) {
		this.processId = processId;
	}
	
	public ArrayList<Message> getSndMsgs() {
		return sndMsgs;
	}
	public void setSndMsgs(ArrayList<Message> sndMsgs) {
		this.sndMsgs = sndMsgs;
	}
}
