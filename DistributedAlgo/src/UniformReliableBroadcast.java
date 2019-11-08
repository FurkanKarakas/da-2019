import java.io.IOException;
import java.util.ArrayList;


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
            System.out.println("Unable to send messages in Uniform Reliable Broadcast.");
        }
    }

    public boolean canDeliver(Message msg){
    	// Deliver if half of the messages have been acknowledged by process p
        return p.msgAckCount(msg) > p.getProcesses().size()/2;
    }

}
