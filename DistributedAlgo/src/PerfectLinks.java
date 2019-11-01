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
//import java.util.concurrent.TimeoutException;
import java.io.*;
//import java.util.ArrayList;
//import java.util.Timer;
//import java.util.TimerTask;

public class PerfectLinks extends Thread {
	private Process pi;
	private Message msg;
	private InetAddress destIP;
	private Integer destPort;
	private Integer numberattempts;

	// private ArrayList<Integer> delivered;

	public PerfectLinks(Process pi, Message m, InetAddress destIP, int destPort, int numberattempts) {
		this.pi = pi;
		this.msg = m;
		this.destIP = destIP;
		this.destPort = destPort;
		this.numberattempts = numberattempts;
		
	}
	
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
		

		DatagramSocket piSocket = pi.getSocket();
		final byte[] data = objectOut.toByteArray();

		DatagramPacket piPacket = new DatagramPacket(data, data.length, ip, port);
		
		
		
		try {
			if (msg.getM().equals("ACK")) {
				System.out.println("Send msg: " + msg.getM());
				piSocket.send(piPacket);
	
			} else {
				this.pi.addMsg(msg);
				for (int i = 0; i < numberattempts; i++) {
					if (!this.pi.isDelivered(msg))
						piSocket.send(piPacket);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}
