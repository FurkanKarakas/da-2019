/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


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
    public void sendFinite(String m, Integer n){
        for(Integer i=0;i<n;i++){
            if(pi.isAlive() && pj.isAlive()){
                
        }
        }     
    }
    public void SendInfinite(String m){
        while(true){
            if(pi.isAlive() && pj.isAlive()){
                
        }
        }
    }
}
