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

public class FairLossLinks {
    private Process pi;
    private Process pj;
    private boolean delivred;
    
    public FairLossLinks(Process pi, Process pj) {
    	this.pi = pi;
    	this.pj = pj;
    }

    public void sendMessage(String m) throws IOException {
        this.delivred=false;
    	// Handle sending by pi
    	byte[] bufi = m.getBytes();

    	DatagramSocket piSocket = pi.create();
    	InetAddress ip = pj.getIp();
    	Integer port = pj.getPort();
    	
    	DatagramPacket piPacket = new DatagramPacket(bufi, bufi.length, ip, port);
    	piSocket.connect(ip, port);
    	
		System.out.println("IsBound : " + piSocket.isBound()); 
		System.out.println("isConnected : " + piSocket.isConnected()); 
		System.out.println("InetAddress : " + piSocket.getInetAddress()); 
		System.out.println("Port : " + piSocket.getPort()); 
		System.out.println("Remote socket address : " + piSocket.getRemoteSocketAddress()); 
		System.out.println("Local socket address : " + piSocket.getLocalSocketAddress()); 


    	// Handle receiving by pj
    	byte[] bufj = m.getBytes();

    	DatagramSocket pjSocket = pj.create();
    	DatagramPacket pjPacket = new DatagramPacket(bufj, bufj.length);


    	piSocket.send(piPacket);
    	pjSocket.receive(pjPacket);

    	String received = new String(pjPacket.getData(), 0, pjPacket.getLength());
        if(received==m){
            this.delivred=true;
        }
    	System.out.println(received);
    	pi.close();
    	pj.close();
    }
    
    public void receiveMessage() throws IOException {
    	// NOT USED CURRENTLY

    	byte[] buf = new byte[256];
    	DatagramPacket packet = new DatagramPacket(buf, buf.length);
    	DatagramSocket pjSocket = pj.getSocket();
		
    	pjSocket.receive(packet);
    	
    	System.out.print(packet.getData());
    	pjSocket.close();
    }

    public Process getPi() {
        return pi;
    }

    public void setPi(Process pi) {
        this.pi = pi;
    }

    public Process getPj() {
        return pj;
    }

    public void setPj(Process pj) {
        this.pj = pj;
    }

    public boolean isDelivred() {
        return delivred;
    }

    public void setDelivred(boolean delivred) {
        this.delivred = delivred;
    }
    

}
