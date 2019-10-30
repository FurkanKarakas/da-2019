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

	// private ArrayList<Integer> delivered;

	public PerfectLinks(Process pi) {
		this.pi = pi;
	}

	public boolean sendMessage(Message msg, InetAddress destIP, int destPort, int numberattempts) throws IOException {
		// Handle sending by pi
		Integer port = destPort;
		InetAddress ip = destIP;

		final ByteArrayOutputStream objectOut = new ByteArrayOutputStream();
		final ObjectOutputStream dataOut = new ObjectOutputStream(objectOut);
		dataOut.writeObject(msg);
		dataOut.close();

		DatagramSocket piSocket = pi.getSocket();
		piSocket.setSoTimeout(10);
		final byte[] data = objectOut.toByteArray();

		DatagramPacket piPacket = new DatagramPacket(data, data.length, ip, port);
		piSocket.connect(ip, port);

		System.out.println("Send msg: " + msg.getM());

		byte[] receive = new byte[65535];
		DatagramPacket dpReceive = null;
		if (msg.getM().equals("ACK")) {
			piSocket.send(piPacket);
		} else {
			this.pi.addMsg(msg);
			for (int i = 0; i < numberattempts; i++) {
				piSocket.send(piPacket);

				dpReceive = new DatagramPacket(receive, receive.length);
				try {
					piSocket.receive(dpReceive);
					byte[] msgBytes = dpReceive.getData();

					ByteArrayInputStream bis = new ByteArrayInputStream(msgBytes);
					ObjectInputStream ois = new ObjectInputStream(bis);
					try {
						Message obj = (Message) ois.readObject();
						if (obj.getM().equals("ACK")) {
							if (obj.getId().equals(msg.getId())) {
								this.pi.removeMsg(msg);
								return true;
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

		return false;

	}

	// Implement the thread execution
	public void run(Message msg, InetAddress destIP, int destPort, int numberattempts) {
		// TODO: Implement sendMessage in a parallel execution in a thread. Run()
		// function is void.
		// this.sendMessage(msg, destIP, destPort, numberattempts);
	}

	public Process getPi() {
		return pi;
	}

	public void setPi(Process pi) {
		this.pi = pi;
	}

}
