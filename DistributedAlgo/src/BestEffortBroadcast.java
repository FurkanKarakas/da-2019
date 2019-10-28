import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class BestEffortBroadcast {

	private Process senderProcess;
        private ArrayList<Message> messages;
        private ArrayList<InetAddress> inetAdresses;
//	private ArrayList<Process> S;
	private PerfectLinks perfectLinks;

	public BestEffortBroadcast(Process senderProcess) {
		 this.senderProcess = senderProcess;
                 this.perfectLinks=new PerfectLinks(senderProcess);
	}

	public void sendMessage() throws IOException {
                    for(Message message : messages){
                        perfectLinks.sendMessage(message);
                    }
	}

	public void deliverMessage(Process pi, Integer msgId) {
		
	}
}