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

            while (sc.hasNextLine()) {
                String[] params = sc.nextLine().trim().split(" ");
                if (Integer.parseInt(params[0]) == n) {
                    InetAddress piAddr = InetAddress.getByName(params[1]);
                    pi = new Process(piAddr, n, Integer.parseInt(params[2]));

                    if (n != 3) {
                        Message msg = new Message("Hey cmd process!", 12003, InetAddress.getByName("127.0.0.1"));
                        PerfectLinks pipl = new PerfectLinks(pi);
                        pipl.sendMessage(msg, InetAddress.getByName("127.0.0.1"), 12003, 1);
                    }
                }
            }
            sc.close();

            pi.listen();
        }
    }

}
