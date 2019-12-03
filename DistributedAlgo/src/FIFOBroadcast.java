import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FIFOBroadcast {
	private Process p;
	private UniformReliableBroadcast urb;
	private volatile ConcurrentHashMap<Integer, CanDeliver> fifoDelivered = new ConcurrentHashMap<Integer, CanDeliver>();

	public FIFOBroadcast(Process p) {
		this.p = p;
		urb = new UniformReliableBroadcast(p);
	}

	public void setProcesses() {
		for (Integer i = 1; i <= p.getProcessCount(); i++) {
			this.fifoDelivered.put(i, new CanDeliver());
		}
	}

	public void sendMessage(ArrayList<Message> messages) throws IOException {
		// Log the broadcast based on first message
		// Message m0 = messages.get(0);
		// if (m0 != null)
			// this.p.log("b " + m0.getId() + "\n");

		// URB broadcast all messages
		this.urb.sendMessage(messages);
	}

	public void canDeliver(Message message) {
		CanDeliver delivered = this.fifoDelivered.get(message.getSender());
		Integer id = message.getId();

		// If we can URB deliver, add it to possible deliverable messages
		if (this.urb.canDeliver(message) && delivered.getMsg(id) == null)
			delivered.addDeliverMsg(message);

		// Deliver all available packets
		delivered.deliver();
	}

	/**
	 * Store the URB delivered messages and check if we can FIFO deliver.
	 */
	public class CanDeliver {
		// Received messages so far
		private ConcurrentHashMap<Integer, Message> receivedMesgs = new ConcurrentHashMap<Integer, Message>();

		// How many messages have been delivered in order
		private AtomicInteger alreadyDelivered = new AtomicInteger(1);

		public void addDeliverMsg(Message m) {
			// Add new URB delivered message
			this.receivedMesgs.put(m.getId(), m);
		}

		public Message getMsg(Integer id) {
			// Get URB delivered message based on ID
			return receivedMesgs.get(id);
		}

		public void deliver() {
			// FIFO deliver

			// Start with the current highest delivered message
			Integer startIdx = this.alreadyDelivered.intValue();
			Message msg = receivedMesgs.get(startIdx);

			// Loop until all currently deliverable messages are logged and delivered
			while (msg != null) {
				this.alreadyDelivered.getAndIncrement();
				startIdx++;
				FIFOBroadcast.this.p.log("d " + msg.getSender() + " " + msg.getM() + "\n");

				// Increase vector clock
				Integer senderIndex = msg.getSender() - 1;
				p.increaseVectorClock(senderIndex);

				msg = receivedMesgs.get(startIdx);

			}
		}
	}
}
