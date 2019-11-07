
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
        ArrayList<Boolean> delivered= p.getFifoDelivred(message.getSender());
    	ArrayList<Message> messages = p.getSenderMsgs(message.getSender());
    	Integer id = message.getId();
        p.setFifoDelivred(message.getSender(), id, Boolean.FALSE);
        if (urb.canDeliver(message) & id == 1) {
            //delivered.set(0, Boolean.TRUE);
            
            p.setFifoDelivred(message.getSender(), id, Boolean.TRUE);
        } else if(id > 1){
            if (this.urb.canDeliver(message) & p.getFifoDelivred(message.getSender()).get(id - 2).equals(true)) {
                p.setFifoDelivred(message.getSender(), id, Boolean.TRUE);
            }
        }
        boolean bool= p.getFifoDelivred(message.getSender()).get(id - 1);
        System.out.println(bool);
        return bool;
    }
}
