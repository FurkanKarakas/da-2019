
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;



public class StubbornLinks {
    private Process pi;
    private Process pj;
    private FairLossLinks fairLossLinks;
    private boolean delivred;
    
    
    public StubbornLinks(Process pi, Process pj) {
        this.pi = pi;
        this.pj = pj;
        this.fairLossLinks = new FairLossLinks(pi,pj);
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
                this.fairLossLinks.sendMessage(m);
                delivred=this.fairLossLinks.isDelivred();
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
        try {
            this.fairLossLinks.receiveMessage();
        } catch (IOException ex) {
            Logger.getLogger(StubbornLinks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}