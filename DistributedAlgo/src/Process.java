import sun.misc.Signal;
import sun.misc.SignalHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class Process extends Thread {
	private DatagramSocket socket; // Socket used to send and receive messages
	private InetAddress ip; // Socket IP address of the process
	private Integer port; // Socket port number of the process

	private Integer processId; // Process ID that is given in membership file
	private ArrayList<InetSocketAddress> processes; // List of all process IP/port numbers from the membership file
	private Integer broadcastCount; // The broadcast count m given in the membership file
	private Integer processCount;

	private FileOutputStream fos; // File output stream for da_proc_n.out
	private String logMsg = ""; // Log message that is written to the file in the end

	private ArrayList<Boolean> isAffected;
	private ArrayList<Integer> vectorClock;

	// A list of acknowledgment messages
	private volatile CopyOnWriteArrayList<Message> ackMsgs = new CopyOnWriteArrayList<Message>();

	// Hash map for each sender thread ID to see if the thread message has been
	// acknowledged by other process
	private volatile ConcurrentHashMap<Long, Boolean> threadIds = new ConcurrentHashMap<Long, Boolean>();

	private LocalizedCausalBroadcast LCB;
	// private FIFOBroadcast fifoBC;
	private Listener pListener;
	static Integer msgID = 0;

	/**
	 * Process constructor
	 *
	 * @param ip             - Socket IP address of the process
	 * @param port           - Socket port number of the process
	 * @param processId      - Process ID that is given in membership file
	 * @param broadcastCount - The broadcast count m given in the membership file
	 */

	public Process(InetAddress ip, Integer port, Integer processId, Integer broadcastCount, Integer n) {

		// Initialize the variables
		this.ip = ip;
		this.port = port;
		this.processId = processId;
		this.broadcastCount = broadcastCount;
		this.vectorClock = new ArrayList<Integer>();
		this.isAffected = new ArrayList<Boolean>();
		this.processCount = n;

		for (int i = 0; i < n; i++) {
			vectorClock.add(0);
		}
		// LocalizedCausalBroadcast is to used broadcast and deliver messages
		this.LCB = new LocalizedCausalBroadcast(this);

		// FIFOBroadcast is to used broadcast and deliver messages
		// this.fifoBC = new FIFOBroadcast(this);

		// Open socket and start process listener for messages
		try {
			this.socket = new DatagramSocket(this.port, this.ip);
		} catch (SocketException e) {
			System.out.println("Failed to create a socket!");
		}
		pListener = new Listener();
		pListener.start();

		// Initialize the FileOutputStream with the given output file name
		String fileName = "da_proc_" + this.processId.toString() + ".out";
		try {
			this.fos = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
		}

		// Signal handlers for TERM, INT, USR2
		SigHandlerTerm sigHandlerTerm = new SigHandlerTerm(this);
		SigHandlerInt sigHandlerInt = new SigHandlerInt(this);
		SigHandlerUsr2 sigHandlerUsr2 = new SigHandlerUsr2(this);

		Signal signalTerm = new Signal("TERM");
		Signal signalInt = new Signal("INT");
		Signal signalUsr2 = new Signal("USR2");

		Signal.handle(signalInt, sigHandlerInt);
		Signal.handle(signalTerm, sigHandlerTerm);
		Signal.handle(signalUsr2, sigHandlerUsr2);

	}

	/**
	 * Signal handler for TERM. Write to output file and close process.
	 */
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
				System.out.println("Failed to file to FileOutputStream.");
			}

			try {
				p.getFos().flush();
				p.getFos().close();
			} catch (IOException e) {
				System.out.println("Failed to flush FileOutputStream.");
			}

			p.getpListener().interrupt();
			p.interrupt();
			// p.getLCB().
			System.exit(0);
		}
	}

	/**
	 * Signal handler for INT. Write to output file and close process.
	 */
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
				System.out.println("Failed to file to FileOutputStream.");
			}

			try {
				p.getFos().flush();
				p.getFos().close();
			} catch (IOException e) {
				System.out.println("Failed to flush FileOutputStream.");
			}

			p.getpListener().interrupt();
			p.interrupt();
			System.exit(0);
		}
	}

	/**
	 * Signal handler for USR2. After receiving signal, start broadcasting.
	 */
	public static class SigHandlerUsr2 implements SignalHandler {
		Process p;

		private SigHandlerUsr2(Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {

			System.out.format("Handling signal: %s\n", signal.toString());

			for (Integer i = 1; i <= this.p.getBroadcastCount(); i++) {

				// Broadcast messages 1->m
				Process.msgID += 1;
				ArrayList<Message> msgList = this.p.createMessagesList(true, this.p.getProcessId());
				try {
					this.p.getLCB().sendMessage(msgList);
				} catch (IOException e) {
					System.out.println("Failed to send messages.");
				}

				// Wait some milliseconds between broadcasts
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (InterruptedException e) {
					System.out.println("Timeout interrupted.");
				}
			}

			return;

		}
	}

	/**
	 * This method creates a new instance of the Sender class, and it makes this
	 * thread start sending message m.
	 *
	 * @param m - Message to be sent
	 */
	public void sendMessage(Message m) {
		new Sender(m).start();
	}

	/**
	 * @param m - Message that we want to see how many acknowledgments we have
	 *          received.
	 * @return Count of acknowledgments for Message m.
	 */
	public Integer msgAckCount(Message m) {
		Integer count = 0;
		ArrayList<Integer> senderIds = new ArrayList<Integer>();
		for (Message mAck : ackMsgs) {
			if (mAck.getId().equals(m.getId()) && mAck.getSender().equals(m.getSender())
					&& !senderIds.contains(mAck.getAckSender())) {
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
			DatagramSocket socket = getSocket();
			byte[] receive = new byte[65535];
			DatagramPacket dpReceive = null;

			// Keep listening for messages for the whole duration of the process
			while (true) {
				dpReceive = new DatagramPacket(receive, receive.length);
				try {
					// Receive a new message
					socket.receive(dpReceive);
					InetAddress senderIp = dpReceive.getAddress();
					Integer senderPort = dpReceive.getPort();
					byte[] msgBytes = dpReceive.getData();

					try {
						// Get the Message object from the data
						ByteArrayInputStream bis = new ByteArrayInputStream(msgBytes);
						ObjectInputStream ois = new ObjectInputStream(bis);
						Message msg = (Message) ois.readObject();

						if (!msg.isAck()) {
							// Broadcast the message if needed
							if (msg.isBroadcast() && msg.getSender() != getProcessId()) {
								ArrayList<Message> messages = Process.this.createMessagesList(msg);
								BestEffortBroadcast beb = new BestEffortBroadcast(Process.this);
								beb.sendMessage(messages);
							}

							// Send acknowledgment for non-acknowledgment message
							Message ack = new Message(msg.getM(), senderPort, senderIp, msg.getDestinationPort(),
									msg.getDestinationInetAddr(), msg.getId(), true, false, msg.getSender(),
									getProcessId(), msg.getVectorClock());
							ack.setThreadId(msg.getThreadId());

							// Send acknowledgment
							Process.this.sendMessage(ack);
						} else {
							// Receive acknowledgement

							// Set threadID true so that Sender thread stops sending
							threadIds.put(msg.getThreadId(), true);

							// Add message to acknowledges and broadcast
							ackMsgs.add(msg);
							LCB.deliver(msg);
							// fifoBC.canDeliver(msg);
						}
					} catch (ClassNotFoundException e) {
						System.out.println("Message class not found.");
					}
				} catch (SocketTimeoutException e) {
					System.out.println("Socket timeout reached.");
				} catch (IOException e) {
					System.out.println("Unable to read socket data.");
				}
			}
		}
	}

	/**
	 * Sender class sends a single message.
	 */
	public class Sender extends Thread {

		private Message msg;

		public Sender(Message m) {
			this.msg = m;
		}

		@Override
		public void run() {

			// Get socket, destination IP and port
			DatagramSocket piSocket = getSocket();
			Integer port = msg.getDestinationPort();
			InetAddress ip = msg.getDestinationInetAddr();

			// Mark the message with thread ID, which is used to stop Thread
			Long threadID = this.getId();
			if (!msg.isAck())
				msg.setThreadId(threadID);

			// Create Message packet that is sent
			ByteArrayOutputStream objectOut = new ByteArrayOutputStream();
			ObjectOutputStream dataOut;
			try {
				dataOut = new ObjectOutputStream(objectOut);
				dataOut.writeObject(msg);
				dataOut.close();
			} catch (IOException e) {
				System.out.println("Unable to create output stream.");
			}
			byte[] data = objectOut.toByteArray();
			DatagramPacket piPacket = new DatagramPacket(data, data.length, ip, port);

			// Send packet
			try {
				if (msg.isAck()) {
					// Send acknowledgement
					piSocket.send(piPacket);
				} else {
					// Sleep first 50ms and increase until 500
					Integer sleepMS = 150;

					// Keep sending until we receive acknowledgment
					threadIds.put(threadID, false);

					while (!threadIds.get(threadID)) {
						piSocket.send(piPacket);

						// Sleep after sending and increase sleep time
						try {
							TimeUnit.MILLISECONDS.sleep(sleepMS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						sleepMS = Math.min(sleepMS * 2, 1000);
					}

					// Stop thread
					this.interrupt();
				}
			} catch (IOException e) {
				System.out.println("Unable to send message.");
			}
		}
	}

	public ArrayList<Integer> mask() {
		ArrayList<Integer> maskedVC = new ArrayList<Integer>();

		for (Integer i = 0; i < this.vectorClock.size(); i++) {
			if (isAffected.get(i))
				maskedVC.add(this.vectorClock.get(i));
			else
				maskedVC.add(0);
		}
		return maskedVC;
	}

	/**
	 * Create messages to be broadcast by sender.
	 *
	 * @param broadcast - Broadcast or not.
	 * @param sender    - Sender ID that broadcasts the messages.
	 * @return Initial broadcast messages.
	 */
	public ArrayList<Message> createMessagesList(boolean broadcast, Integer sender) {
		ArrayList<Message> messages = new ArrayList<Message>();

		for (InetSocketAddress sa : this.getProcesses()) {
			InetAddress destAddr = sa.getAddress();
			Integer destPort = sa.getPort();
			ArrayList<Integer> maskedVectorClock = mask();
			Message m = new Message(Process.msgID.toString(), destPort, destAddr, this.getPort(), this.getIp(),
					Process.msgID, false, broadcast, sender, null, maskedVectorClock);
			messages.add(m);
		}
		return messages;
	}

	/**
	 * Relay messages to be sent
	 *
	 * @param m - Message that should be relayed.
	 * @return Relay messages.
	 */
	public ArrayList<Message> createMessagesList(Message m) {
		ArrayList<Message> messages = new ArrayList<Message>();

		for (InetSocketAddress sa : getProcesses()) {
			InetAddress addr = sa.getAddress();
			Integer port = sa.getPort();
			Message mRelay = new Message(m.getM(), port, addr, this.getPort(), this.getIp(), m.getId(), false, false,
					m.getSender(), null, m.getVectorClock());
			messages.add(mRelay);
		}
		return messages;
	}

	/**
	 * Increase the vector clock of index by one. The function is used in FIFO
	 * broadcast.
	 * 
	 * @param index - The index of vector clock that we want to increase.
	 */
	public void increaseVectorClock(Integer index) {
		Integer increasedValue = this.vectorClock.get(index) + 1;
		this.vectorClock.set(index, increasedValue);
	}

	public ArrayList<InetSocketAddress> getProcesses() {
		return processes;
	}

	public void setProcesses(ArrayList<InetSocketAddress> processes) {
		this.processes = processes;
		// this.fifoBC.setProcesses();
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

	public void setAckMsgs(CopyOnWriteArrayList<Message> ackMsgs) {
		this.ackMsgs = ackMsgs;
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

	public Integer getBroadcastCount() {
		return broadcastCount;
	}

	public void setBroadcastCount(Integer broadcastCount) {
		this.broadcastCount = broadcastCount;
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
	/*
	 * public FIFOBroadcast getFifoBC() { return fifoBC; }
	 * 
	 * public void setFifoBC(FIFOBroadcast fifoBC) { this.fifoBC = fifoBC; }
	 */

	public LocalizedCausalBroadcast getLCB() {
		return LCB;
	}

	public void setLCB(LocalizedCausalBroadcast lCB) {
		LCB = lCB;
	}

	public Listener getpListener() {
		return pListener;
	}

	public void setpListener(Listener pListener) {
		this.pListener = pListener;
	}

	public ArrayList<Boolean> getIsAffected() {
		return isAffected;
	}

	public void setIsAffected(int index, boolean value) {
		isAffected.add(value);
	}

	public ArrayList<Integer> getVectorClock() {
		return vectorClock;
	}

	public Integer getProcessCount() {
		return processCount;
	}

	public void setProcessCount(Integer processCount) {
		this.processCount = processCount;
	}

}
