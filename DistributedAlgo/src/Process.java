
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.concurrent.TimeUnit;

public class Process {
	private InetAddress ip;
	private Integer port;
	private Integer processId;
	private DatagramSocket socket;
	private ArrayList<InetSocketAddress> processes;
	private volatile ConcurrentHashMap<Message, Boolean> ackMsgs = new ConcurrentHashMap<Message, Boolean>();

	public Process(InetAddress ip, Integer processId, Integer port) {
		this.port = port;
		this.processId = processId;
		this.ip = ip;
		try {
			this.socket = new DatagramSocket(this.port, this.ip);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to create a socket!");
		}
		new Listener().start();

		//SigHandlerTerm sigHandlerInt = new SigHandlerTerm(this);
		//SigHandlerInt sigHandlerTerm = new SigHandlerInt(this);
		// SigHandlerUsr2 sigHandlerUsr2 = new SigHandlerUsr2(this);

		Signal signalInt = new Signal("INT");
		Signal signalTerm = new Signal("TERM");
		// Signal signalUsr2 = new Signal("USR2");

		//Signal.handle(signalInt, sigHandlerInt);
		//Signal.handle(signalTerm, sigHandlerTerm);
		// Signal.handle(signalUsr2, sigHandlerUsr2);

	}


	public ArrayList<InetSocketAddress> getProcesses() {
		return processes;
	}

	public void setProcesses(ArrayList<InetSocketAddress> processes) {
		this.processes = processes;
	}
/*
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
*/
	public void sendMessage(Message m, InetAddress destIP, int destPort) {
		new Sender(m, destIP, destPort).start();
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

	public boolean isDelivered(Message msg) {
                System.out.println(this.ackMsgs.size());
                if(this.ackMsgs.get(msg).equals(null)){
                    return false;
                }
		return this.ackMsgs.get(msg);
	}
	
	public void setMsgStatus(Message msg, boolean status) {
		this.ackMsgs.put(msg, status);
	}

	public ConcurrentHashMap<Message, Boolean> getAckMsgs() {
		return ackMsgs;
	}



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
                                                    if(!msg.isBroadcast()){
							Message ack = new Message(msg.getM(), msg.getDestinationPort(), msg.getDestinationInetAddr(), msg.getId(), true,false);
							Process.this.sendMessage(ack, senderIp, senderPort);
							System.out.println("Received message: " + msg.getM());
                                                    }else{
                                                        ArrayList<Message> messages = Process.this.createMessagesList(msg.getId(), false);
							//Process.this.sendMessage(ack, senderIp, senderPort);
							//System.out.println("Received message: " + msg.getM());
                                                        BestEffortBroadcast beb = new BestEffortBroadcast(Process.this);
                                                        beb.sendMessage(messages);
                                                        
                                                    }
						} else {
							Process.this.ackMsgs.put(msg, true);
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
	public ArrayList<Message> createMessagesList(Integer id,boolean broadcast){
                ArrayList<Message> messages = new ArrayList<Message>();
                for(InetSocketAddress sa : this.processes){
                        InetAddress addr = sa.getAddress();
			Integer port = sa.getPort();
			Message m = new Message(sa.toString(), port, addr, id, false, broadcast);
                        messages.add(m);
                }
                return messages;
        }
        
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
					System.out.println("Send acknowledgement.");
					piSocket.send(piPacket);
	
				} else {
					Process.this.ackMsgs.put(msg, false);
					while (true) {
						if (!Process.this.isDelivered(msg)) {
							System.out.println(msg.getM());
							piSocket.send(piPacket);
							try {
								TimeUnit.MILLISECONDS.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
							break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}
