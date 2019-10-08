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

public class Da_proc {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            int n = Integer.parseInt(args[0]);
            String fileName = args[1];

            File membership = new File(fileName);
            Scanner sc;
            try {
                sc = new Scanner(membership);
                int totalN = Integer.parseInt(sc.nextLine());

                while (sc.hasNextLine()) {
                    String[] params = sc.nextLine().trim().split(" ");
                    if (Integer.parseInt(params[0]) == n) {
                    	InetAddress piAddr = InetAddress.getByName("127.0.0.8");
                    	InetAddress pjAddr = InetAddress.getByName("127.0.0.9");
                        Process pi = new Process(piAddr, n, 8001); //Integer.parseInt(params[2]));
                        Process pj = new Process(pjAddr, 24, 8080);
                        
                        PerfectLinks pl = new PerfectLinks(pi, pj);
                        pl.sendMessage("Hey pj");
                        pl.receiveMessge();
                        
                        
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }

        }
    }

}
