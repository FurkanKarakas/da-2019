
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
public class UniformReliabaleBroadcast {
    private Process p;
    private ArrayList<Message> messages;

    public UniformReliabaleBroadcast(Process p, ArrayList<Message> messages) {
        this.messages = messages;
        this.p = p;
    }
    
    public void sendMessage(){
        BestEffortBroadcast beb = new BestEffortBroadcast(p);
        try {
            beb.sendMessage(this.messages);
        } catch (IOException ex) {
            Logger.getLogger(UniformReliabaleBroadcast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean canDeliver(){
        Integer counter= 0;
        for(Message m: this.messages){
            
            if(p.isDelivered(m)){
                counter ++;
            }
        }
        if(counter > this.messages.size()/2){
            return true;
        }
        return false;
    }
    
}
