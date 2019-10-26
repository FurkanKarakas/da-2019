import java.net.*;
//import java.io.*;

public class Listener extends Thread {
    private Process process;

    public Listener(Process pi) {
        this.process = pi;
    }

    public void run() {
        DatagramSocket socket = this.process.getSocket();
        byte[] receive = new byte[65535];
        DatagramPacket DpReceive = null;
        while (true) {
            DpReceive = new DatagramPacket(receive, receive.length);
            try {
                socket.receive(DpReceive);
                System.out.println(DpReceive.getAddress().toString() + DpReceive.getData().toString());
            } catch (Exception e) {
                //e.printStackTrace();
            }

        }
    }

}