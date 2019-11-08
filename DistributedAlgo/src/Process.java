import sun.misc.Signal;
import sun.misc.SignalHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Process extends Thread {
	private InetAddress ip;
	private Integer port;
	private Integer processId;
	private DatagramSocket socket;
	private ArrayList<InetSocketAddress> processes;
	private FileOutputStream fos;
	private Integer bcCount;
	
	private String logMsg = "";
	private volatile CopyOnWriteArrayList<Message> ackMsgs = new CopyOnWriteArrayList<Message>();
	private volatile ConcurrentHashMap<Long, Boolean> threadIds = new ConcurrentHashMap<Long, Boolean>();
	private FIFOBroadcast fifoBC;
	private Listener pListener;
	static Integer msgID = 0;
	
	/**
	 * This is a thread-safe hashmap. In this data structure, we map a given Message
	 * to a boolean value in order to store if the process has received the
	 * acknowledgement yet.
	 */

	/**
	 * In the constructor of the process, we start listening to the incoming
	 * messages right away.
	 * 
	 * @param ip
	 * @param processId
	 * @param port
	 * @throws IOException 
	 */
	


	public Process(InetAddress ip, Integer processId, Integer port, Integer bcCount) throws IOException {
		this.port = port;
		this.processId = processId;
		this.ip = ip;
		this.fifoBC = new FIFOBroadcast(this);
		this.bcCount = bcCount;
		try {
			this.socket = new DatagramSocket(this.port, this.ip);
		} catch (SocketException e) {
			System.out.println("Failed to create a socket!");
		}
		pListener = new Listener();
		pListener.start();
		
		String fileName = "da_proc_" + this.processId.toString() + ".out";
		this.fos = new FileOutputStream(fileName);

		SigHandlerTerm sigHandlerInt = new SigHandlerTerm(this);
		SigHandlerInt sigHandlerTerm = new SigHandlerInt(this);
		SigHandlerUsr2 sigHandlerUsr2 = new SigHandlerUsr2(this);

		Signal signalInt = new Signal("INT");
		Signal signalTerm = new Signal("TERM");
		Signal signalUsr2 = new Signal("USR2");

		Signal.handle(signalInt, sigHandlerInt);
		Signal.handle(signalTerm, sigHandlerTerm);
		Signal.handle(signalUsr2, sigHandlerUsr2);

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
			
			for (Integer i = 1; i <= this.p.getBcCount(); i++) {
				Process.msgID += 1;
				ArrayList<Message> msgList = this.p.createMessagesList(true, this.p.getProcessId());
				try {
					this.p.fifoBC.sendMessage(msgList);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Failed to send messages.");
				}
			}
			
            return;
			
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

			try {
				p.getFos().write(p.getLogMsg().getBytes());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				p.getFos().flush();
				p.getFos().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			p.getpListener().interrupt();
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
			
			try {
				p.getFos().write(p.getLogMsg().getBytes());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				p.getFos().flush();
				p.getFos().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			p.getpListener().interrupt();
			p.interrupt();
			System.exit(0);
		}
	}

	/**
	 * This method creates a new instance of the sendMessage class, and it makes
	 * this thread start executing.
	 * 
	 * @param m
	 * @param destIP
	 * @param destPort
	 */
	public void sendMessage(Message m) {
		new Sender(m, this).start();
	}
	/*
	public boolean isDelivered(Message msg) {
		if (this.ackMsgs.get(msg).equals(null)) {
			return false;
		}
		return this.ackMsgs.get(msg);
	}
	*/
	
	public Integer msgCount(Message m) {
		Integer count = 0;
		ArrayList<Integer> senderIds = new ArrayList<Integer>();
		for (Message mAck : ackMsgs) {
			if (mAck.getId().equals(m.getId()) && mAck.getSender().equals(m.getSender()) && !senderIds.contains(mAck.getAckSender())) {
				count++;
				senderIds.add(mAck.getAckSender());
			}
		}
		
		return count;
	}

	/**
	 * This is a subclass of the class Process, and it extends Thread. It is
	 * responsible for listening to the incoming messages.
	 */
	public class Listener extends Thread {

		@Override
		public void run() {
			System.out.println("Start listener.");
			DatagramSocket socket = Process.this.getSocket();
			byte[] receive = new byte[65535];
			DatagramPacket dpReceive = null;
			while (true) {
				dpReceive = new DatagramPacket(receive, receive.length);
				try {
					socket.receive(dpReceive);
					byte[] msgBytes = dpReceive.getData();
					Integer senderPort = dpReceive.getPort();
					InetAddress senderIp = dpReceive.getAddress();

					ByteArrayInputStream bis = new ByteArrayInputStream(msgBytes);
					ObjectInputStream ois = new ObjectInputStream(bis);
					try {
						Message msg = (Message) ois.readObject();
						if (!msg.isAck()) {
							if (msg.isBroadcast() && Process.this.msgCount(msg) == 0) {
								ArrayList<Message> messages = Process.this.createMessagesList(msg);
								BestEffortBroadcast beb = new BestEffortBroadcast(Process.this);
								beb.sendMessage(messages);
							}
							Message ack = new Message(msg.getM(), senderPort,
									senderIp, msg.getDestinationPort(), msg.getDestinationInetAddr(),
									msg.getId(), true, false, msg.getSender(), Process.this.getProcessId());
							ack.setThreadId(msg.getThreadId());
							Process.this.sendMessage(ack);
						} else {
							threadIds.put(msg.getThreadId(), true);
							ackMsgs.add(msg);
							fifoBC.canDeliver(msg);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				} catch (SocketTimeoutException e) {
					// System.out.println("Timeout reached.");
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}


	/**
	 * This class is a subclass of the class Process, and it is responsible for
	 * sending messages concurrently.
	 */
	public class Sender extends Thread {

		private Message msg;
		private Process p;

		public Sender(Message m, Process p) {
			this.msg = m;
			this.p = p;
		}

		@Override
		public void run() {
			Integer port = msg.getDestinationPort();
			InetAddress ip = msg.getDestinationInetAddr();
			
			if (!msg.isAck())
				msg.setThreadId(this.getId());

			final ByteArrayOutputStream objectOut = new ByteArrayOutputStream();
			ObjectOutputStream dataOut;
			try {
				dataOut = new ObjectOutputStream(objectOut);
				dataOut.writeObject(msg);
				dataOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			DatagramSocket piSocket = Process.this.getSocket();
			final byte[] data = objectOut.toByteArray();

			DatagramPacket piPacket = new DatagramPacket(data, data.length, ip, port);
			try {
				if (msg.isAck()) {
					piSocket.send(piPacket);
				} else {
					
					threadIds.put(this.getId(), false);
					Integer sleepMS = 50;
					while (!threadIds.get(this.getId())) {
						//System.out.println("Send msgs N times: " + count);
						piSocket.send(piPacket);
						try {
							TimeUnit.MILLISECONDS.sleep(sleepMS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						sleepMS = Math.min(sleepMS*3, 500);
					}
					this.interrupt();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<Message> createMessagesList(boolean broadcast, Integer sender) {
		ArrayList<Message> messages = new ArrayList<Message>();
		
		for (InetSocketAddress sa : this.processes) {
			InetAddress addr = sa.getAddress();
			Integer port = sa.getPort();
			Message m = new Message(Process.msgID.toString(), port, addr, this.getPort(), this.getIp(), Process.msgID, false, broadcast, sender, null);
			messages.add(m);
		}
		
		return messages;
	}
	
	public ArrayList<Message> createMessagesList(Message m) {
		ArrayList<Message> messages = new ArrayList<Message>();

		for (InetSocketAddress sa : this.processes) {
			InetAddress addr = sa.getAddress();
			Integer port = sa.getPort();
			Message mRelay = new Message(m.getM(), port, addr, this.getPort(), this.getIp(), m.getId(), false, false, m.getSender(), null);
			messages.add(mRelay);
		}
		
		return messages;
	}
	
	public ArrayList<InetSocketAddress> getProcesses() {
		return processes;
	}

	public void setProcesses(ArrayList<InetSocketAddress> processes) {
		this.processes = processes;
		this.fifoBC.setProcesses();
	}

	public void log(String l) {
		this.logMsg = this.logMsg + l;
	}

	public String getLogMsg() {
		return logMsg;
	}

	public void setLogMsg(String logMsg) {
		this.logMsg = logMsg;
	}

	public CopyOnWriteArrayList<Message> getAckMsgs() {
		return ackMsgs;
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

	public Integer getBcCount() {
		return bcCount;
	}

	public void setBcCount(Integer bcCount) {
		this.bcCount = bcCount;
	}

	public FileOutputStream getFos() {
		return fos;
	}

	public void setFos(FileOutputStream fos) {
		this.fos = fos;
	}



	public ConcurrentHashMap<Long, Boolean> getThreadIds() {
		return threadIds;
	}

	public void setThreadIds(ConcurrentHashMap<Long, Boolean> threadIds) {
		this.threadIds = threadIds;
	}

	public FIFOBroadcast getFifoBC() {
		return fifoBC;
	}
	public void setFifoBC(FIFOBroadcast fifoBC) {
		this.fifoBC = fifoBC;
	}
	public Listener getpListener() {
		return pListener;
	}
	public void setpListener(Listener pListener) {
		this.pListener = pListener;
	}
	public void setAckMsgs(CopyOnWriteArrayList<Message> ackMsgs) {
		this.ackMsgs = ackMsgs;
	}
	
	
	
}
