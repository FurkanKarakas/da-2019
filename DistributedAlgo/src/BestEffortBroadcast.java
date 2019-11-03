import java.io.IOException;
import java.util.ArrayList;

public class BestEffortBroadcast {

	// private Integer id;
	private Process p;

	public BestEffortBroadcast(Process p) {
		this.p = p;
		// this.id = id;
	}

	public void sendMessage(ArrayList<Message> messages) throws IOException {

		/*
		 * for (InetSocketAddress sa : p.getProcesses()) { InetAddress addr =
		 * sa.getAddress(); Integer port = sa.getPort(); Message m = new
		 * Message(sa.toString(), port, addr, id, false); p.sendMessage(m, addr, port);
		 * }
		 */
		for (Message m : messages) {
			p.sendMessage(m, m.getDestinationInetAddr(), m.getDestinationPort());
		}

	}

	public boolean deliverMessage(Message msg) {
		return p.isDelivered(msg);
	}
}