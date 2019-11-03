import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class BestEffortBroadcast {

	private Integer id;
	private Process p;
	public BestEffortBroadcast(Process p, Integer id) {
		this.p = p;
		this.id = id;
	}

	public void sendMessage() throws IOException {
		
		for (InetSocketAddress sa : p.getProcesses()) {
			InetAddress addr = sa.getAddress();
			Integer port = sa.getPort();
			Message m = new Message(sa.toString(), port, addr, id, false);
			p.sendMessage(m, addr, port);
		}
			
	}

	public boolean deliverMessage(Message m) {
		return true;
		//return this.senderProcess.isDelivered(m);
	}
}