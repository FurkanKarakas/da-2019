
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * 
 */
public class FIFOBroadcast {
    private Process p;
    private UniformReliableBroadcast urb;
    private volatile ConcurrentHashMap<Integer, CanDeliver> fifoDelivred = new ConcurrentHashMap<Integer, CanDeliver>();
    
    
    public FIFOBroadcast(Process p) {
        this.p = p;
        urb = new UniformReliableBroadcast(p);
    }
    
    public void setProcesses() {
    	Integer i = 1;
        for(InetSocketAddress process : p.getProcesses()){
            this.fifoDelivred.put(i, new CanDeliver());
            i++;
        }
    }
    
	public class CanDeliver {
		private ConcurrentHashMap<Integer, Message> receivedMesgs = new ConcurrentHashMap<Integer, Message>();
		private AtomicInteger alreadyDelivered = new AtomicInteger(1);

		public void addDeliverMsg(Message m) {
			this.receivedMesgs.put(m.getId(), m);
		}
		
		public Message getMsg(Integer id) {
			return receivedMesgs.get(id);
		}
		
		public void deliver() {
			Integer startIdx = this.alreadyDelivered.intValue();
			Message msg = receivedMesgs.get(startIdx);
			
			while (msg != null) {
				this.alreadyDelivered.getAndIncrement();
				startIdx++;
				FIFOBroadcast.this.p.log("d " + msg.getSender() + " " + msg.getM() + "\n");
				msg = receivedMesgs.get(startIdx);
			}
			
		}
		
	}

    public void sendMessage(ArrayList<Message> messages) throws IOException {
        Message m0 = messages.get(0);
        if (m0 != null)
            this.p.log("b " + m0.getId() + "\n");
        this.urb.sendMessage(messages);
    }

    public void canDeliver(Message message) {
        CanDeliver delivered= this.fifoDelivred.get(message.getSender());
    	Integer id = message.getId();
    	
    	if (this.urb.canDeliver(message) && delivered.getMsg(id) == null)
    		delivered.addDeliverMsg(message);
    	
    	delivered.deliver();

    }


	public Process getP() {
		return p;
	}

	public void setP(Process p) {
		this.p = p;
	}

	public ConcurrentHashMap<Integer, CanDeliver> getFifoDelivred() {
		return fifoDelivred;
	}

	public void setFifoDelivred(ConcurrentHashMap<Integer, CanDeliver> fifoDelivred) {
		this.fifoDelivred = fifoDelivred;
	}
    
    
}
