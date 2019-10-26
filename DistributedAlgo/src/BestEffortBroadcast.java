import java.io.IOException;
import java.util.ArrayList;

public class BestEffortBroadcast {

	// private Process senderProcess;
	// private ArrayList<Process> S;
	private ArrayList<PerfectLinks> Spl;

	public BestEffortBroadcast(Process senderProcess, ArrayList<Process> S) {
		// this.senderProcess = senderProcess;
		// this.S = S;
		// for (Process pi : S) {
		// this.Spl.add(new PerfectLinks(senderProcess, pi));
		// }
	}

	public void sendMessage(Integer msgId) throws IOException {
		// for (PerfectLinks pl : Spl) {
		// pl.sendMessage(msgId);
		// }
	}

	public void deliverMessage(Process pi, Integer msgId) {
		for (PerfectLinks pl : Spl) {
			// if (pl.getPj().getProcessId() == pi.getProcessId()) {
			pl.deliverMessage(msgId);
			break;
			// }
		}
	}
}