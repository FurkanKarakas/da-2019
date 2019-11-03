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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
                	pi = new Process(piAddr, n, port);
                }
            }

            pi.setProcesses(processes);
            sc.close();
            
            if (1 == n) {
                ArrayList<Message> messages = pi.createMessagesList(2,true);
                FifoBroadcast fifoBroadcast= new FifoBroadcast(pi, messages);
                fifoBroadcast.sendMessage();
            	//UniformReliabaleBroadcast urb = new UniformReliabaleBroadcast(pi, messages);
                //BestEffortBroadcast beb = new BestEffortBroadcast(pi);
                //urb.sendMessage();
                //beb.sendMessage(messages);
                //Message message = new Message("Hello", 12002, InetAddress.getByName("127.0.0.1"), 1, false, false);
                //pi.sendMessage(message, InetAddress.getByName("127.0.0.1"), 12002);
                TimeUnit.MILLISECONDS.sleep(1000);
                System.out.println(fifoBroadcast.canDeliver(2));
            }
            
        }
    }

}
