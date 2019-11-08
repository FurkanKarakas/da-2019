import java.io.IOException;
import java.util.ArrayList;

public class BestEffortBroadcast {

	private Process p;

	public BestEffortBroadcast(Process p) {
		this.p = p;
	}

	public void sendMessage(ArrayList<Message> messages) throws IOException {
		for (Message m : messages) {
			p.sendMessage(m);
		}
	}

	public boolean deliverMessage(Message msg) {
		return true;
	}
}
