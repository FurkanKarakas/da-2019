/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* PERFECT LINKS:
1) (Validity) If pi and pj are correct, then every message sent by pi to pj is eventually delivered by pj.
2) (No duplication) No message is delivered (to a process) more than once.
3) (No creation) No message is delivered unless it was sent.
*/
import java.net.Socket;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerfectLinks {
    private Process pi;
    private Process pj;

    public Process getPi() {
        return pi;
    }

    public Process getPj() {
        return pj;
    }

    public void setPi(Process pi) {
        this.pi = pi;
    }

    public void setPj(Process pj) {
        this.pj = pj;
    }

    public void sendMessage(String m, Integer n) {
        DataOutputStream ostream = null;
        try {
            pi.getSocket().connect(pj.getSocket().getLocalSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ostream = new DataOutputStream(pi.getSocket().getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Integer i = 0; i < n; i++) {
            if (pi.isAlive() && pj.isAlive()) {
                try {
                    ostream.writeByte(Process.messageID++);
                    ostream.writeUTF(m);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void receiveMessge(){
        DataInputStream istream=null;
        try {
            pj.getSocket().connect(pi.getSocket().getLocalSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            istream = new DataInputStream(pj.getSocket().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String m = null;
        try {
            m=istream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(m);
    }
}
