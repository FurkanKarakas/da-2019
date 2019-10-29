import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
//import java.io.*;

public class Listener extends Thread {
	private Process process;

	public Listener(Process pi) {
		this.process = pi;
	}

	public void run() {
		DatagramSocket socket = this.process.getSocket();
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
					Message obj = (Message) ois.readObject();
					if (!obj.getM().equals("ACK")) {
						PerfectLinks ackLink = new PerfectLinks(this.process);
						Message ack = new Message("ACK", senderPort, senderIp, obj.getId());
						ackLink.sendMessage(ack);
						System.out.println("Received message: " + obj.getM());
					} else {
						this.process.removeMsg(obj);
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}