
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * 
 */
public class UniformReliableBroadcast {
    private Process p;

    public UniformReliableBroadcast(Process p) {
        this.p = p;
    }
    
    public void sendMessage(ArrayList<Message> messages){
        BestEffortBroadcast beb = new BestEffortBroadcast(p);
        try {
            beb.sendMessage(messages);
        } catch (IOException ex) {
            Logger.getLogger(UniformReliableBroadcast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean canDeliver(Message msg){
        return p.msgCount(msg) > p.getProcesses().size()/2;
    }

}
