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

public class Da_proc {

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
            Process pi;
            // Parse first line from membership file that has total numbers of processes
            Integer.parseInt(sc.nextLine());

            while (sc.hasNextLine()) {
                String[] params = sc.nextLine().trim().split(" ");
                if (Integer.parseInt(params[0]) == n) {
                	InetAddress piAddr = InetAddress.getByName(params[1]);
                	pi = new Process(piAddr, n, Integer.parseInt(params[2]));

                	if (n != 3) {
                		System.out.println("Sending msg!");
	                	Message msg = new Message("Hey cmd process!");
	                	PerfectLinks pipl = new PerfectLinks(pi);
	                	pipl.sendMessage(msg.getId(), 12003, InetAddress.getByName("127.0.0.1"));
                	}
                }
            }
            sc.close();
            
            
            DatagramSocket ds = new DatagramSocket(12003, InetAddress.getByName("127.0.0.1"));
            byte[] receive = new byte[65535]; 
            DatagramPacket DpReceive = null; 
            while (true) { 
      
                // Step 2 : create a DatgramPacket to receive the data. 
                DpReceive = new DatagramPacket(receive, receive.length); 
      
                // Step 3 : revieve the data in byte buffer. 
                ds.receive(DpReceive); 
      
                System.out.println("Client:-" + data(receive)); 
      
                // Exit the server if the client sends "bye" 
                if (data(receive).toString().equals("bye")) { 
                    System.out.println("Client sent bye.....EXITING"); 
                    break; 
                } 
      
                // Clear the buffer after every message. 
                receive = new byte[65535]; 
            }
        }
    }
    
    public static StringBuilder data(byte[] a) { 
        if (a == null) 
            return null; 
        StringBuilder ret = new StringBuilder(); 
        int i = 0; 
        while (a[i] != 0) 
        { 
            ret.append((char) a[i]); 
            i++; 
        }
        return ret; 
    }

}
