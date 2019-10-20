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

public class PerfectLinks {
	private Process pi;
	private Process pj;
	private ArrayList<String> delivered;

	public PerfectLinks(Process pi, Process pj) {
		this.pi = pi;
		this.pj = pj;
		this.delivered = new ArrayList<String>();
	}

	public void sendMessage(String m) throws IOException {
		// Handle sending by pi
		byte[] bufi = m.getBytes();

		DatagramSocket piSocket = pi.create();
		InetAddress ip = pj.getIp();
		Integer port = pj.getPort();

		DatagramPacket piPacket = new DatagramPacket(bufi, bufi.length, ip, port);
		piSocket.connect(ip, port);

		System.out.println("IsBound : " + piSocket.isBound());
		System.out.println("isConnected : " + piSocket.isConnected());
		System.out.println("InetAddress : " + piSocket.getInetAddress());
		System.out.println("Port : " + piSocket.getPort());
		System.out.println("Remote socket address : " + piSocket.getRemoteSocketAddress());
		System.out.println("Local socket address : " + piSocket.getLocalSocketAddress());

		// Handle receiving by pj
		byte[] bufj = m.getBytes();

		DatagramSocket pjSocket = pj.create();
		DatagramPacket pjPacket = new DatagramPacket(bufj, bufj.length);

		piSocket.send(piPacket);
		pjSocket.receive(pjPacket);

		String received = new String(pjPacket.getData(), 0, pjPacket.getLength());
		this.delivered.add(received);

		System.out.println(received);
		pi.close();
		pj.close();
	}

	public boolean isDelivered(String m) {
		// TODO
		return true;
	}

	public Process getPi() {
		return pi;
	}

	public void setPi(Process pi) {
		this.pi = pi;
	}

	public Process getPj() {
		return pj;
	}

	public void setPj(Process pj) {
		this.pj = pj;
	}

}
