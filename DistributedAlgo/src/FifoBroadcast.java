
import java.io.IOException;
import java.util.ArrayList;

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
    private ArrayList<Message> messages;
    private static ArrayList<Boolean> delivered = new ArrayList<Boolean>();
    private UniformReliableBroadcast urb;

    public FIFOBroadcast(Process p, ArrayList<Message> messages) {
        this.p = p;
        this.messages = messages;
        if (FIFOBroadcast.delivered.size() < (messages.get(0).getId() - 1)) {
            for (int i = FIFOBroadcast.delivered.size(); i < messages.get(0).getId(); i++) {
                FIFOBroadcast.delivered.add(false);
            }
        }
        if (FIFOBroadcast.delivered.size() == (messages.get(0).getId() - 1)) {
            FIFOBroadcast.delivered.add(false);
        }
        urb = new UniformReliableBroadcast(p, messages);
    }

    public void sendMessage() throws IOException {
    	String logBroadcast = "b " +  messages.get(0).getM() + "\n";
    	this.p.getFos().write(logBroadcast.getBytes());
        this.urb.sendMessage();
    }

    public Boolean canDeliver(Integer id) {
        if (urb.canDeliver() & messages.get(0).getId() == 1) {
			String logMsg = "d " + messages.get(0).getSender() + " " + messages.get(0).getM() + "\n";
			try {
				p.getFos().write(logMsg.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            FIFOBroadcast.delivered.set(0, Boolean.TRUE);
        } else {
            if (this.urb.canDeliver() & FIFOBroadcast.delivered.get(messages.get(0).getId() - 2).equals(true)) {
                FIFOBroadcast.delivered.set(messages.get(0).getId() - 1, true);
            }
        }
        return FIFOBroadcast.delivered.get(messages.get(0).getId() - 1);
    }
}
