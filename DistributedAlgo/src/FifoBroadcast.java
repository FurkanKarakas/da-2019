
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * 
 */
public class FifoBroadcast {
    private Process p;
    private ArrayList<Message> messages;
    private static ArrayList<Boolean> delivered = new ArrayList<Boolean>();
    private UniformReliabaleBroadcast urb;

    public FifoBroadcast(Process p, ArrayList<Message> messages) {
        this.p = p;
        this.messages = messages;
        if(this.delivered.size()<(messages.get(0).getId()-1)){
            for(int i=this.delivered.size(); i<messages.get(0).getId();i++){
                this.delivered.add(false);
            }
        }
        if(this.delivered.size()==(messages.get(0).getId()-1)){
            this.delivered.add(false);
        }       
        urb = new UniformReliabaleBroadcast(p, messages);
    }
    
    public void sendMessage(){
        this.urb.sendMessage();
    }
    
    public Boolean canDeliver(Integer id){
        if(urb.canDeliver() & messages.get(0).getId()==1){
            System.out.println("Not OKKK");
            this.delivered.set(0, Boolean.TRUE);
        }
        else{
                if(this.urb.canDeliver() & this.delivered.get(messages.get(0).getId()-2).equals(true)){
                    System.out.println("Not OKKK 2");
                    this.delivered.set(messages.get(0).getId()-1,true);
                }   
        }       
        return this.delivered.get(messages.get(0).getId()-1);
    }
}
