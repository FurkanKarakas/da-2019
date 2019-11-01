import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
//import java.net.InetAddress;
import java.util.ArrayList;

public class BestEffortBroadcast {

	private Process senderProcess;
	private Message message;

	public BestEffortBroadcast(Process senderProcess, Message message) {
		this.senderProcess = senderProcess;
		this.message = message;
	}

	public void sendMessage() throws IOException {
		for (InetSocketAddress sa : senderProcess.getProcesses()) {
			InetAddress addr = sa.getAddress();
			Integer port = sa.getPort();
			PerfectLinks pl = new PerfectLinks(senderProcess, message, addr, port, 1);
			pl.start();
		}
			
	}

	public boolean deliverMessage(Message m) {
		return this.senderProcess.isDelivered(m);
	}
}