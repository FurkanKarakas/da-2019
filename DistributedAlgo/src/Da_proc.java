/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othman
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

//import java.util.concurrent.TimeUnit;

public class Da_proc {

    /**
     * @param args the command line arguments
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws FileNotFoundException, UnknownHostException, IOException,
            ClassNotFoundException, InterruptedException {
        if (args.length > 0) {
            int n = Integer.parseInt(args[0]);
            String fileName = args[1];

            File membership = new File(fileName);
            Scanner sc = new Scanner(membership);
            Process pi = null;
            // Parse first line from membership file that has total numbers of processes
            Integer.parseInt(sc.nextLine());

            ArrayList<InetSocketAddress> processes = new ArrayList<InetSocketAddress>();
            while (sc.hasNextLine()) {
                String[] params = sc.nextLine().trim().split(" ");

                InetAddress piAddr = InetAddress.getByName(params[1]);
                Integer port = Integer.parseInt(params[2]);
                InetSocketAddress sa = new InetSocketAddress(piAddr, port);
                processes.add(sa);
                if (Integer.parseInt(params[0]) == n) {
                	Integer broadcastCount = 0;
                	if (args[2] != null)
                		broadcastCount = Integer.parseInt(args[2]);
                    pi = new Process(piAddr, n, port, broadcastCount);
                }
            }

            pi.setProcesses(processes);
            sc.close();

        }
    }

}
