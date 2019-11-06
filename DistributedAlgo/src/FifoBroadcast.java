
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

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
        this.urb.sendMessage(messages);
    }

    public Boolean canDeliver(Message message) {
    	ArrayList<Boolean> delivered = new ArrayList<Boolean>();
    	ArrayList<Message> messages = p.getSenderMsgs(message.getSender());
    	Integer id = message.getId();
    	for (int i = 0; i < id; i++) {
            delivered.add(false);
        }
    	
        if (urb.canDeliver(message) & id == 1) {
            delivered.set(0, Boolean.TRUE);
        } else {
            if (this.urb.canDeliver(message) & delivered.get(id - 2).equals(true)) {
                delivered.set(messages.get(0).getId() - 1, true);
            }
        }
        return delivered.get(id - 1);
    }
}
