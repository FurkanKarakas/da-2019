import java.util.ArrayList;

public class LocalizedCausalBroadcast {
    private Process p;
    private ArrayList<Integer> vectorClock;

    public LocalizedCausalBroadcast(Process p, ArrayList<Integer> vectorClock) {
        this.p = p;
        this.vectorClock = vectorClock;
    }
}