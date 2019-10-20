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
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class da_proc {

    /**
     * @param args the command line arguments
     * @throws ClassNotFoundException 
     * @throws InterruptedException 
     */
    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws FileNotFoundException, UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        if (args.length > 0) {
            int n = Integer.parseInt(args[0]);
            String fileName = args[1];

            File membership = new File(fileName);
            Scanner sc = new Scanner(membership);
 
            // Parse first line from membership file that has total numbers of processes
            Integer.parseInt(sc.nextLine());

            while (sc.hasNextLine()) {
                String[] params = sc.nextLine().trim().split(" ");
                if (Integer.parseInt(params[0]) == n) {
                	InetAddress piAddr = InetAddress.getByName("127.0.0.1");
                	InetAddress pjAddr = InetAddress.getByName("127.0.0.1");

                	Process pi = new Process(piAddr, Integer.parseInt(params[2]), 12001);
                    Process pj = new Process(pjAddr, 2, 12002);

                    PerfectLinks pl = new PerfectLinks(pi, pj);
                    Message msg = new Message("Hey pj");
                    pl.sendMessage(msg.getId());
                    pl.deliverMessage(msg.getId());
                    
                }
            }
            sc.close();
        }
    }

}
