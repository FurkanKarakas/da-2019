
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;



public class StubbornLinks {
    private Process pi;
    private Process pj;
    private PerfectLinks perfectLinks;
    private boolean delivred;
    
    
    public StubbornLinks(Process pi, Process pj) {
        this.pi = pi;
        this.pj = pj;
        this.perfectLinks = new PerfectLinks(pi,pj);
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
    public void sendMessage(String m){
        this.delivred=false;
        while(true){
            try {
                perfectLinks.sendMessage(m);
                delivred=perfectLinks.isDelivred();
            } catch (IOException ex) {
                Logger.getLogger(StubbornLinks.class.getName()).log(Level.SEVERE, null, ex);
            }
            
           if(delivred){
             break;  
           } 
        }
        while(true){
            System.out.println("Delivred");
        }
    }
    public void receiveMessage(){
        
    }
}