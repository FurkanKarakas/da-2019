
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

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

    public FIFOBroadcast(Process p) {
        this.p = p;
        urb = new UniformReliableBroadcast(p);
    }

    public void sendMessage(ArrayList<Message> messages) throws IOException {
        Message m0 = messages.get(0);
        if (m0 != null)
            this.p.log("b " + m0.getId() + "\n");
        this.urb.sendMessage(messages);
    }

    public Boolean canDeliver(Message message) {
        Process.CanDeliver delivered= p.getFifoDelivred(message.getSender());
    	Integer id = message.getId();
  
        if (urb.canDeliver(message) & id == 1) {
            //delivered.set(0, Boolean.TRUE);
            delivered.addDeliver(id, true);
        } else if(id > 1){

            if (this.urb.canDeliver(message) & delivered.canDeliver(id-1)) {
            	//System.out.println("Can deliver: " + id);
            	delivered.addDeliver(id, true);
            }
        }

        return delivered.canDeliver(id);
    }
}
