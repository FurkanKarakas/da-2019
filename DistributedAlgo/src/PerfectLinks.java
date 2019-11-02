/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* PERFECT LINKS:
1) (Validity) If pi and pj are correct, then every message sent by pi to pj is eventually delivered by pj.
2) (No duplication) No message is delivered (to a process) more than once.
3) (No creation) No message is delivered unless it was sent.
*/
import java.net.*;
import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
import java.io.*;
//import java.util.ArrayList;
//import java.util.Timer;
//import java.util.TimerTask;
/*
public class PerfectLinks {
	
	private Process pi;
	private Message msg;
	private InetAddress destIP;
	private Integer destPort;
	// private ArrayList<Integer> delivered;

	public PerfectLinks(Process pi, Message m, InetAddress destIP, int destPort) {
		this.pi = pi;
		this.msg = m;
		this.destIP = destIP;
		this.destPort = destPort;
	}
	
	public void sendMessage() {
		new Sender(msg, destIP, destPort).start();
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
			
	
			DatagramSocket piSocket = PerfectLinks.this.pi.getSocket();
			final byte[] data = objectOut.toByteArray();
	
			DatagramPacket piPacket = new DatagramPacket(data, data.length, ip, port);
			
			try {
				if (msg.isAck()) {
					System.out.println("Send acknowledgement.");
					piSocket.send(piPacket);
	
				} else {
					Process.setMsgStatus(msg, false);
					while (true) {
						if (!Process.isDelivered(msg)) {
							piSocket.send(piPacket);
							try {
								TimeUnit.MILLISECONDS.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							System.out.println("Breaking loop");
							break;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}
*/