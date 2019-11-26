import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Da_proc {

    public static void main(String[] args) throws FileNotFoundException, UnknownHostException, IOException,
            ClassNotFoundException, InterruptedException {
        if (args.length > 0) {
            // Read processID, filename (membership) and broadcastCount m from CMD
            int processID = Integer.parseInt(args[0]);
            String fileName = args[1];
            Integer broadcastCount = 0;
            if (args[2] != null)
                broadcastCount = Integer.parseInt(args[2]);

            // Initialize File, Scanner and Process
            File membership = new File(fileName);
            Scanner sc = new Scanner(membership);
            Process pi = null;

            // Parse first line from membership file that has total numbers of processes
            Integer n = Integer.parseInt(sc.nextLine());

            // processes read all process IP addresses and port numbers from membership file
            ArrayList<InetSocketAddress> processes = new ArrayList<InetSocketAddress>();

            // Read the membership file
            for (Integer i = 0; i < n; i++) {
                // Read one line
                String[] params = sc.nextLine().trim().split(" ");
                Integer currentID = Integer.parseInt(params[0]);
                InetAddress piAddr = InetAddress.getByName(params[1]);
                Integer port = Integer.parseInt(params[2]);

                // Add to processes the current addr/port
                processes.add(new InetSocketAddress(piAddr, port));

                // Check if the ID's match
                if (currentID == processID) {
                    pi = new Process(piAddr, port, processID, broadcastCount, n);
                }
                // Set all of the affected processes to false
                pi.setIsAffected(i, false);
            }

            // Read the dependencies between processes
            for (Integer i = 0; i < n; i++) {
                String[] params = sc.nextLine().trim().split(" ");
                if (i + 1 == processID) {
                    for (int j = 0; j < params.length; j++) {
                        pi.setIsAffected(Integer.parseInt(params[j]) - 1, true);
                    }
                    break;
                }
            }

            // Finally set list of all processes to the current process
            pi.setProcesses(processes);
            sc.close();
        }
    }
}
