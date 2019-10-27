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
import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PerfectLinks {
	private Process pi;

	private ArrayList<Integer> delivered;
	

	public PerfectLinks(Process pi) {
		this.pi = pi;
	}

	public void sendMessage(Message msg) throws IOException {
		// Handle sending by pi
		
		Integer port = msg.getPort();
		InetAddress ip = msg.getInetAddr();

		final ByteArrayOutputStream objectOut = new ByteArrayOutputStream();
		final ObjectOutputStream dataOut = new ObjectOutputStream(objectOut);
		dataOut.writeObject(msg);
		dataOut.close();

		DatagramSocket piSocket = pi.getSocket();
		final byte[] data = objectOut.toByteArray();

		DatagramPacket piPacket = new DatagramPacket(data, data.length, ip, port);
		piSocket.connect(ip, port);

		piSocket.send(piPacket);
		if (!msg.getM().equals("ACK"))
			this.pi.addMsg(msg);

		System.out.println("Send msg: " + msg.getM());

	}

	public Process getPi() {
		return pi;
	}

	public void setPi(Process pi) {
		this.pi = pi;
	}

}
