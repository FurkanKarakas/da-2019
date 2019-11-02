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
		System.out.println("Start listener.");
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
						Message ack = new Message("ACK", senderPort, senderIp, obj.getId());
						PerfectLinks ackLink = new PerfectLinks(this.process, ack, dpReceive.getAddress(), dpReceive.getPort(), 1);
						ackLink.start();
						System.out.println("Received message: " + obj.getM());
					} else {
                                                obj.setDelivered(true);
						this.process.removeMsg(obj);
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