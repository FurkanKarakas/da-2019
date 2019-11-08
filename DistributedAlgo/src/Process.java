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

public class Process extends Thread {
	private InetAddress ip;
	private Integer port;
	private Integer processId;
	private DatagramSocket socket;
	private ArrayList<InetSocketAddress> processes;
	private FileOutputStream fos;
	private Integer bcCount;
	
	private String logMsg = "";
	private volatile ConcurrentHashMap<Integer, CanDeliver> fifoDelivred = new ConcurrentHashMap<Integer, CanDeliver>();
	private volatile CopyOnWriteArrayList<Message> ackMsgs = new CopyOnWriteArrayList<Message>();
	private volatile CopyOnWriteArrayList<Message> received = new CopyOnWriteArrayList<Message>();
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
	
	public class CanDeliver {
		private ConcurrentHashMap<Integer, Boolean> canDeliverId = new ConcurrentHashMap<Integer, Boolean>();
		private ConcurrentHashMap<Integer, Boolean> isLogged = new ConcurrentHashMap<Integer, Boolean>();

		public ConcurrentHashMap<Integer, Boolean> getCanDeliverId() {
			return canDeliverId;
		}

		public void setCanDeliverId(ConcurrentHashMap<Integer, Boolean> canDeliverId) {
			this.canDeliverId = canDeliverId;
		}
		
		public void log(Integer isLog) {
			isLogged.put(isLog, true);
		}
		
		public boolean isLogged(Integer isLog) {
			if (isLogged.get(isLog) == null)
				return false;
			return true;
		}
		
		public void addDeliver(Integer id, Boolean isDeliverable) {
			canDeliverId.put(id, isDeliverable);
		}
		
		public boolean canDeliver(Integer id) {
			if (canDeliverId.get(id) == null)
				return false;
			return canDeliverId.get(id);
		}
	}

	public Process(InetAddress ip, Integer processId, Integer port, Integer bcCount) throws IOException {
		this.port = port;
		this.processId = processId;
		this.ip = ip;
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
		this.received = new CopyOnWriteArrayList<Message>();

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

	public ArrayList<InetSocketAddress> getProcesses() {
		return processes;
	}

	public void setProcesses(ArrayList<InetSocketAddress> processes) {
		this.processes = processes;
                int i=1;
                for(InetSocketAddress process : processes){
                    this.fifoDelivred.put(i, new CanDeliver());
                    i++;
                }
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
				FIFOBroadcast fifoBroadcast = new FIFOBroadcast(p);
				try {
					fifoBroadcast.sendMessage(msgList);
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
	public void sendMessage(Message m, InetAddress destIP, int destPort) {
		new Sender(m, destIP, destPort).start();
	}
	/*
	public boolean isDelivered(Message msg) {
		if (this.ackMsgs.get(msg).equals(null)) {
			return false;
		}
		return this.ackMsgs.get(msg);
	}
	*/
	public boolean isDeliveredBeb(Message msg) {
		return this.received.contains(msg);
	}
	
	public Integer msgCount(Message m) {
		Integer count = 0;
		ArrayList<Integer> senderIds = new ArrayList<Integer>();
		for (Message mAck : ackMsgs) {
			if (mAck.equals(m) && !senderIds.contains(mAck.getAckSender())) {
				count++;
				senderIds.add(mAck.getAckSender());
				//System.out.println("mAck is: " + senderIds);
			}
		}
		
		return count;
	}

	public CopyOnWriteArrayList<Message> getAckMsgs() {
		return ackMsgs;
	}

	public boolean isAck(Message msg) {

		return this.ackMsgs.contains(msg);
	}

    public void setFifoDelivred(Integer sender, Integer id, Boolean value){
    	CanDeliver delivered = this.fifoDelivred.get(sender);
        delivered.addDeliver(id, value);
        this.fifoDelivred = new ConcurrentHashMap<Integer, CanDeliver>(this.fifoDelivred);
    }

    public CanDeliver getFifoDelivred(Integer sender){
        return this.fifoDelivred.get(sender);
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
			FIFOBroadcast fifoBC = new FIFOBroadcast(Process.this);
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
								// Process.this.sendMessage(ack, senderIp, senderPort);
								// System.out.println("Received message: " + msg.getM());
								BestEffortBroadcast beb = new BestEffortBroadcast(Process.this);
								beb.sendMessage(messages);
								//System.out.println("Received message: " + msg.getM());
							}
							Message ack = new Message(msg.getM(), senderPort,
									senderIp, msg.getId(), true, false, msg.getSender(), Process.this.getProcessId());
							Process.this.sendMessage(ack, senderIp, senderPort);
							Process.this.received.add(msg);
						} else {
							Process.this.ackMsgs.add(msg);
							CanDeliver canDel = Process.this.fifoDelivred.get(msg.getSender());
							if (canDel != null) {
								if (!canDel.isLogged(msg.getId()) && fifoBC.canDeliver(msg)) {
									Process.this.log("d " + msg.getSender() + " " + msg.getM() + "\n");
									//System.out.println("Logging");
									canDel.log(msg.getId());
								}
							}         
							
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

	public ArrayList<Message> createMessagesList(boolean broadcast, Integer sender) {
		ArrayList<Message> messages = new ArrayList<Message>();
		
		for (InetSocketAddress sa : this.processes) {
			InetAddress addr = sa.getAddress();
			Integer port = sa.getPort();
			Message m = new Message(Process.msgID.toString(), port, addr, Process.msgID, false, broadcast, sender, null);
			messages.add(m);
		}
		
		return messages;
	}
	
	public ArrayList<Message> createMessagesList(Message m) {
		ArrayList<Message> messages = new ArrayList<Message>();

		for (InetSocketAddress sa : this.processes) {
			InetAddress addr = sa.getAddress();
			Integer port = sa.getPort();
			Message mRelay = new Message(m.getM(), port, addr, m.getId(), false, false, m.getSender(), null);
			messages.add(mRelay);
		}
		
		return messages;
	}

	/**
	 * This class is a subclass of the class Process, and it is responsible for
	 * sending messages concurrently.
	 */
	public class Sender extends Thread {

		private Message msg;
		private InetAddress destIP;
		private Integer destPort;

		public Sender(Message m, InetAddress destIP, int destPort) {
			this.msg = m;
			this.destIP = destIP;
			this.destPort = destPort;
		}

		@Override
		public void run() {
			Integer port = destPort;
			InetAddress ip = destIP;

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
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} else {
					while (true) {
						//if (!Process.this.isAck(msg)) {
							piSocket.send(piPacket);
							try {
								TimeUnit.MILLISECONDS.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						//} else
							//break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
	
	public CopyOnWriteArrayList<Message> getReceived() {
		return received;
	}

	public void setReceived(CopyOnWriteArrayList<Message> received) {
		this.received = received;
	}

	
}
