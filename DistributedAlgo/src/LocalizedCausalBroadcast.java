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

	public void sendMessage(Integer msgID) throws IOException {
		this.p.loglock.lock();
		this.p.VClock.lock();
		try {
			this.p.log("b " + msgID + "\n");
			CopyOnWriteArrayList<Integer> vectorClockCurrent = new CopyOnWriteArrayList<Integer>(this.p.getVectorClock());
        
			vectorClockCurrent = this.p.mask(vectorClockCurrent);
			ArrayList<Message> messages = this.p.createMessagesList(true, this.p.getProcessId(), vectorClockCurrent);
			this.fifoBC.sendMessage(messages);
		} finally {
			this.p.loglock.unlock();
			this.p.VClock.unlock();
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
		this.p.VClock2.lock();
		boolean canLCBdeliver = true;
		try {
			CopyOnWriteArrayList<Integer> messageVC = message.getVectorClock();
			CopyOnWriteArrayList<Integer> processVC = p.getVectorClock();
					
			
					if(processVC.get(message.getSender()-1)!=message.getId()-1){
						canLCBdeliver = false;
					}
			for (Integer i = 0; i < messageVC.size(); i++) {
				if (messageVC.get(i) > processVC.get(i)) {
					canLCBdeliver = false;
					break;
				}
			}
		} finally {
			this.p.VClock2.unlock();
		}
		return canLCBdeliver;
	}

	public class DeliverThread extends Thread {

		@Override
		public void run() {
			System.out.println("Starting Localised Causal Broadcast thread.");
			
			while (true) {
				p.Pendinglock2.lock();
				try {
					for (Message message : pending) {
						if (canLCBdeliver(message)) {
							fifoBC.canDeliver(message);
													
							pending.remove(message);
													
						}
					}
				} finally {
					p.Pendinglock2.unlock();
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