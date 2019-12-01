import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class LocalizedCausalBroadcast {
	private Process p;
	private FIFOBroadcast fifoBC;
	private CopyOnWriteArrayList<Message> pending;
	private DeliverThread lcbDeliver;

	public LocalizedCausalBroadcast(Process p) {
		this.p = p;
		this.pending = new CopyOnWriteArrayList<Message>();
		fifoBC = new FIFOBroadcast(p);
		fifoBC.setProcesses();
		lcbDeliver = new DeliverThread();
		lcbDeliver.start();
	}

	public void sendMessage(ArrayList<Message> messages) throws IOException {
		this.fifoBC.sendMessage(messages);
	}

	public void deliver(Message message) {
		this.pending.add(message);
	}

	public boolean canLCBdeliver(Message message) {
		ArrayList<Integer> messageVC = message.getVectorClock();
		ArrayList<Integer> processVC = p.getVectorClock();
		boolean canLCBdeliver = true;

		for (Integer i = 0; i < messageVC.size(); i++) {
			if (messageVC.get(i) > processVC.get(i)) {
				canLCBdeliver = false;
				break;
			}
		}
		return canLCBdeliver;
	}

	public class DeliverThread extends Thread {

		@Override
		public void run() {
			System.out.println("Starting Localised Causal Broadcast thread.");

			while (true) {
				for (Message message : pending) {
					if (canLCBdeliver(message)) {
						fifoBC.canDeliver(message);
						pending.remove(message);
					}
				}
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("Failed to sleep in deliver thread.");
				}
			}
		}
	}

}