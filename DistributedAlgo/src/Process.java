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

	private FileOutputStream fos; // File output stream for da_proc_n.out
	private String logMsg = ""; // Log message that is written to the file in the end

	private ArrayList<Boolean> isAffected;
	private ArrayList<Integer> vectorClock;

	// A list of acknowledgment messages
	private volatile CopyOnWriteArrayList<Message> ackMsgs = new CopyOnWriteArrayList<Message>();

	// Hash map for each sender thread ID to see if the thread message has been
	// acknowledged by other process
	private volatile ConcurrentHashMap<Long, Boolean> threadIds = new ConcurrentHashMap<Long, Boolean>();

	private FIFOBroadcast fifoBC;
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

	public Process(final InetAddress ip, final Integer port, final Integer processId, final Integer broadcastCount,
			Integer n) {

		// Initialize the variables
		this.ip = ip;
		this.port = port;
		this.processId = processId;
		this.broadcastCount = broadcastCount;
		for (int i = 0; i < n; i++) {
			vectorClock.set(i, 0);
		}

		// FIFOBroadcast is to used broadcast and deliver messages
		this.fifoBC = new FIFOBroadcast(this);

		// Open socket and start process listener for messages
		try {
			this.socket = new DatagramSocket(this.port, this.ip);
		} catch (final SocketException e) {
			System.out.println("Failed to create a socket!");
		}
		pListener = new Listener();
		pListener.start();

		// Initialize the FileOutputStream with the given output file name
		final String fileName = "da_proc_" + this.processId.toString() + ".out";
		try {
			this.fos = new FileOutputStream(fileName);
		} catch (final FileNotFoundException e) {
			System.out.println("File not found!");
		}

		// Signal handlers for TERM, INT, USR2
		final SigHandlerTerm sigHandlerTerm = new SigHandlerTerm(this);
		final SigHandlerInt sigHandlerInt = new SigHandlerInt(this);
		final SigHandlerUsr2 sigHandlerUsr2 = new SigHandlerUsr2(this);

		final Signal signalTerm = new Signal("TERM");
		final Signal signalInt = new Signal("INT");
		final Signal signalUsr2 = new Signal("USR2");

		Signal.handle(signalInt, sigHandlerInt);
		Signal.handle(signalTerm, sigHandlerTerm);
		Signal.handle(signalUsr2, sigHandlerUsr2);

	}

	/**
	 * Signal handler for TERM. Write to output file and close process.
	 */
	public static class SigHandlerTerm implements SignalHandler {
		Process p;

		private SigHandlerTerm(final Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(final Signal signal) {
			System.out.format("Handling signal: %s\n", signal.toString());

			try {
				p.getFos().write(p.getLogMsg().getBytes());
			} catch (final IOException e1) {
				System.out.println("Failed to file to FileOutputStream.");
			}

			try {
				p.getFos().flush();
				p.getFos().close();
			} catch (final IOException e) {
				System.out.println("Failed to flush FileOutputStream.");
			}

			p.getpListener().interrupt();
			p.interrupt();
			System.exit(0);
		}
	}

	/**
	 * Signal handler for INT. Write to output file and close process.
	 */
	public static class SigHandlerInt implements SignalHandler {
		Process p;

		private SigHandlerInt(final Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(final Signal signal) {
			System.out.format("Handling signal: %s\n", signal.toString());

			try {
				p.getFos().write(p.getLogMsg().getBytes());
			} catch (final IOException e1) {
				System.out.println("Failed to file to FileOutputStream.");
			}

			try {
				p.getFos().flush();
				p.getFos().close();
			} catch (final IOException e) {
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

		private SigHandlerUsr2(final Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(final Signal signal) {

			System.out.format("Handling signal: %s\n", signal.toString());

			for (Integer i = 1; i <= this.p.getBroadcastCount(); i++) {

				// Broadcast messages 1->m
				Process.msgID += 1;
				final ArrayList<Message> msgList = this.p.createMessagesList(true, this.p.getProcessId());
				try {
					this.p.getFifoBC().sendMessage(msgList);
				} catch (final IOException e) {
					System.out.println("Failed to send messages.");
				}

				// Wait some milliseconds between broadcasts
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (final InterruptedException e) {
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
	public void sendMessage(final Message m) {
		new Sender(m).start();
	}

	/**
	 * @param m - Message that we want to see how many acknowledgments we have
	 *          received.
	 * @return Count of acknowledgments for Message m.
	 */
	public Integer msgAckCount(final Message m) {
		Integer count = 0;
		final ArrayList<Integer> senderIds = new ArrayList<Integer>();
		for (final Message mAck : ackMsgs) {
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
			final DatagramSocket socket = getSocket();
			final byte[] receive = new byte[65535];
			DatagramPacket dpReceive = null;

			// Keep listening for messages for the whole duration of the process
			while (true) {
				dpReceive = new DatagramPacket(receive, receive.length);
				try {
					// Receive a new message
					socket.receive(dpReceive);
					final InetAddress senderIp = dpReceive.getAddress();
					final Integer senderPort = dpReceive.getPort();
					final byte[] msgBytes = dpReceive.getData();

					try {
						// Get the Message object from the data
						final ByteArrayInputStream bis = new ByteArrayInputStream(msgBytes);
						final ObjectInputStream ois = new ObjectInputStream(bis);
						final Message msg = (Message) ois.readObject();

						if (!msg.isAck()) {
							// Broadcast the message if needed
							if (msg.isBroadcast() && msg.getSender() != getProcessId()) {
								final ArrayList<Message> messages = Process.this.createMessagesList(msg);
								final BestEffortBroadcast beb = new BestEffortBroadcast(Process.this);
								beb.sendMessage(messages);
							}

							// Send acknowledgment for non-acknowledgment message
							final Message ack = new Message(msg.getM(), senderPort, senderIp, msg.getDestinationPort(),
									msg.getDestinationInetAddr(), msg.getId(), true, false, msg.getSender(),
									getProcessId(), null);
							ack.setThreadId(msg.getThreadId());

							// Send acknowledgment
							Process.this.sendMessage(ack);
						} else {
							// Receive acknowledgement

							// Set threadID true so that Sender thread stops sending
							threadIds.put(msg.getThreadId(), true);

							// Add message to acknowledges and FIFO broadcast
							ackMsgs.add(msg);
							fifoBC.canDeliver(msg);
						}
					} catch (final ClassNotFoundException e) {
						System.out.println("Message class not found.");
					}
				} catch (final SocketTimeoutException e) {
					System.out.println("Socket timeout reached.");
				} catch (final IOException e) {
					System.out.println("Unable to read socket data.");
				}
			}
		}
	}

	/**
	 * Sender class sends a single message.
	 */
	public class Sender extends Thread {

		private final Message msg;

		public Sender(final Message m) {
			this.msg = m;
		}

		@Override
		public void run() {

			// Get socket, destination IP and port
			final DatagramSocket piSocket = getSocket();
			final Integer port = msg.getDestinationPort();
			final InetAddress ip = msg.getDestinationInetAddr();

			// Mark the message with thread ID, which is used to stop Thread
			final Long threadID = this.getId();
			if (!msg.isAck())
				msg.setThreadId(threadID);

			// Create Message packet that is sent
			final ByteArrayOutputStream objectOut = new ByteArrayOutputStream();
			ObjectOutputStream dataOut;
			try {
				dataOut = new ObjectOutputStream(objectOut);
				dataOut.writeObject(msg);
				dataOut.close();
			} catch (final IOException e) {
				System.out.println("Unable to create output stream.");
			}
			final byte[] data = objectOut.toByteArray();
			final DatagramPacket piPacket = new DatagramPacket(data, data.length, ip, port);

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
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
						sleepMS = Math.min(sleepMS * 2, 1000);
					}

					// Stop thread
					this.interrupt();
				}
			} catch (final IOException e) {
				System.out.println("Unable to send message.");
			}
		}
	}

	/**
	 * Create messages to be broadcast by sender.
	 *
	 * @param broadcast - Broadcast or not.
	 * @param sender    - Sender ID that broadcasts the messages.
	 * @return Initial broadcast messages.
	 */
	public ArrayList<Message> createMessagesList(final boolean broadcast, final Integer sender) {
		final ArrayList<Message> messages = new ArrayList<Message>();

		for (final InetSocketAddress sa : this.getProcesses()) {
			final InetAddress destAddr = sa.getAddress();
			final Integer destPort = sa.getPort();
			final Message m = new Message(Process.msgID.toString(), destPort, destAddr, this.getPort(), this.getIp(),
					Process.msgID, false, broadcast, sender, null, vectorClock);
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
	public ArrayList<Message> createMessagesList(final Message m) {
		final ArrayList<Message> messages = new ArrayList<Message>();

		for (final InetSocketAddress sa : getProcesses()) {
			final InetAddress addr = sa.getAddress();
			final Integer port = sa.getPort();
			final Message mRelay = new Message(m.getM(), port, addr, this.getPort(), this.getIp(), m.getId(), false,
					false, m.getSender(), null, vectorClock);
			messages.add(mRelay);
		}
		return messages;
	}

	public ArrayList<InetSocketAddress> getProcesses() {
		return processes;
	}

	public void setProcesses(final ArrayList<InetSocketAddress> processes) {
		this.processes = processes;
		this.fifoBC.setProcesses();
	}

	public void log(final String l) {
		this.logMsg = this.logMsg + l;
	}

	public String getLogMsg() {
		return logMsg;
	}

	public void setLogMsg(final String logMsg) {
		this.logMsg = logMsg;
	}

	public CopyOnWriteArrayList<Message> getAckMsgs() {
		return ackMsgs;
	}

	public void setAckMsgs(final CopyOnWriteArrayList<Message> ackMsgs) {
		this.ackMsgs = ackMsgs;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(final InetAddress ip) {
		this.ip = ip;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(final Integer port) {
		this.port = port;
	}

	public Integer getProcessId() {
		return processId;
	}

	public void setProcessId(final Integer processId) {
		this.processId = processId;
	}

	public Integer getBroadcastCount() {
		return broadcastCount;
	}

	public void setBroadcastCount(final Integer broadcastCount) {
		this.broadcastCount = broadcastCount;
	}

	public FileOutputStream getFos() {
		return fos;
	}

	public void setFos(final FileOutputStream fos) {
		this.fos = fos;
	}

	public ConcurrentHashMap<Long, Boolean> getThreadIds() {
		return threadIds;
	}

	public void setThreadIds(final ConcurrentHashMap<Long, Boolean> threadIds) {
		this.threadIds = threadIds;
	}

	public FIFOBroadcast getFifoBC() {
		return fifoBC;
	}

	public void setFifoBC(final FIFOBroadcast fifoBC) {
		this.fifoBC = fifoBC;
	}

	public Listener getpListener() {
		return pListener;
	}

	public void setpListener(final Listener pListener) {
		this.pListener = pListener;
	}

	public ArrayList<Boolean> getIsAffected() {
		return isAffected;
	}

	public void setIsAffected(int index, boolean value) {
		isAffected.set(index, value);
	}
}
