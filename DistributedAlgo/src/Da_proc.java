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
                    String[] p = sc.nextLine().trim().split(" ");
                    if (Integer.parseInt(p[0]) == n) {
                        System.out.println("Start process: " + n);
                    }
                }
            } catch(Exception e) {
                System.out.println(e);
            }


        }
    }
    
}
