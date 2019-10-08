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
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerfectLinks {
    private Process pi;
    private Process pj;
    
    public PerfectLinks(Process p1, Process p2) {
    	this.pi = p1;
    	this.pj = p2;
    }

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

    public void sendMessage(String m) {
    	byte[] buf = m.getBytes();
    	DatagramSocket pjSocket = pj.createSocket();
    	DatagramSocket piSocket = pj.createSocket();
    	InetAddress pjAddress = pjSocket.getInetAddress();
    	Integer pjPort = pjSocket.getPort();
    	DatagramPacket mPacket = new DatagramPacket(buf, buf.length, pjAddress, pjPort);
    	try {
			piSocket.send(mPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	pj.closeSocket();
    	pi.closeSocket();
    	/*
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
        */
    }
    public void receiveMessge(){
    	byte[] buf = new byte[256];
    	DatagramPacket packet = new DatagramPacket(buf, buf.length);
    	DatagramSocket pjSocket = pj.getSocket();
    	try {
			pjSocket.receive(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.print(packet.getData());
    	pjSocket.close();
    	/*
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
        */
    }
}
