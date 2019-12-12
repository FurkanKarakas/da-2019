import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

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

	public void sendMessage(Integer msgID) throws IOException {

		try {
			this.p.VClock.lock();
			ArrayList<Integer> vectorClockCurrent = new ArrayList<Integer>(this.p.getVectorClock());
			this.p.log("b " + msgID + "\n");
			this.p.VClock.unlock();
			vectorClockCurrent = this.p.mask(vectorClockCurrent);
			ArrayList<Message> messages = this.p.createMessagesList(true, this.p.getProcessId(), vectorClockCurrent);
			this.fifoBC.sendMessage(messages);
		} finally {

		}
	}

	public void deliver(Message message) {
		this.p.Pendinglock.lock();
		try {
			this.pending.add(message);
		} finally {
			this.p.Pendinglock.unlock();
		}
	}

	public boolean canLCBdeliver(Message message) {
		// this.p.VClock.lock();
		boolean canLCBdeliver = true;
		try {
			ArrayList<Integer> messageVC = message.getVectorClock();
			ArrayList<Integer> processVC = p.getVectorClock();

			if (processVC.get(message.getSender() - 1) != message.getId() - 1) {
				canLCBdeliver = false;
			}
			for (Integer i = 0; i < messageVC.size(); i++) {
				if (messageVC.get(i) > processVC.get(i)) {
					canLCBdeliver = false;
					break;
				}
			}
		} finally {
			// this.p.VClock.unlock();
		}
		return canLCBdeliver;
	}

	public class DeliverThread extends Thread {

		@Override
		public void run() {

			while (true) {

				try {
					for (Message message : pending) {
						if (canLCBdeliver(message)) {
							fifoBC.canDeliver(message);
							p.Pendinglock.lock();
							pending.remove(message);
							p.Pendinglock.unlock();
						}
					}
				} finally {

				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("Failed to sleep in deliver thread.");
				}
			}
		}
	}

}