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
	private ArrayList<Integer> delivered;

	public PerfectLinks(Process pi, Process pj) {
		this.pi = pi;
		this.pj = pj;
		this.delivered = new ArrayList<Integer>();
	}

	public void sendMessage(Integer msgId) throws IOException {
		// Handle sending by pi
		

        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeInt(msgId);
        dataOut.close();

		DatagramSocket piSocket = pi.create();
		InetAddress ip = pj.getIp();
		Integer port = pj.getPort();
		final byte[] bytes = byteOut.toByteArray();

		DatagramPacket piPacket = new DatagramPacket(bytes, bytes.length, ip, port);
		piSocket.connect(ip, port);

		//System.out.println("IsBound : " + piSocket.isBound());
		//System.out.println("isConnected : " + piSocket.isConnected());
		//System.out.println("InetAddress : " + piSocket.getInetAddress());
		//System.out.println("Port : " + piSocket.getPort());
		//System.out.println("Remote socket address : " + piSocket.getRemoteSocketAddress());
		//System.out.println("Local socket address : " + piSocket.getLocalSocketAddress());

		// Handle receiving by pj
		DatagramSocket pjSocket = pj.create();
		DatagramPacket pjPacket = new DatagramPacket(bytes, bytes.length);

		piSocket.send(piPacket);
		pjSocket.receive(pjPacket);

        final ByteArrayInputStream byteIn = new ByteArrayInputStream(pjPacket.getData());
        final DataInputStream dataIn = new DataInputStream(byteIn);
        final int received = dataIn.readInt();
        
		pi.close();
		pj.close();
		System.out.println("Received msg with ID: " + received);
		
	}
	
	public void deliverMessage(Integer msgId) {
		if (this.isDelivered(msgId))
			System.out.println("Delivered msg with ID: " + msgId);
		else 
			this.delivered.add(msgId);
	}

	public boolean isDelivered(Integer mId) {
		return this.delivered.contains(mId);
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
