
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
public class FifoBroadcast {
    // private Process p;
    private ArrayList<Message> messages;
    private static ArrayList<Boolean> delivered = new ArrayList<Boolean>();
    private UniformReliabaleBroadcast urb;

    public FifoBroadcast(Process p, ArrayList<Message> messages) {
        // this.p = p;
        this.messages = messages;
        if (FifoBroadcast.delivered.size() < (messages.get(0).getId() - 1)) {
            for (int i = FifoBroadcast.delivered.size(); i < messages.get(0).getId(); i++) {
                FifoBroadcast.delivered.add(false);
            }
        }
        if (FifoBroadcast.delivered.size() == (messages.get(0).getId() - 1)) {
            FifoBroadcast.delivered.add(false);
        }
        urb = new UniformReliabaleBroadcast(p, messages);
    }

    public void sendMessage() {
        this.urb.sendMessage();
    }

    public Boolean canDeliver(Integer id) {
        if (urb.canDeliver() & messages.get(0).getId() == 1) {
            FifoBroadcast.delivered.set(0, Boolean.TRUE);
        } else {
            if (this.urb.canDeliver() & FifoBroadcast.delivered.get(messages.get(0).getId() - 2).equals(true)) {
                FifoBroadcast.delivered.set(messages.get(0).getId() - 1, true);
            }
        }
        return FifoBroadcast.delivered.get(messages.get(0).getId() - 1);
    }
}
