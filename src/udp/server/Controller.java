/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

/**
 *
 * @author Eivind Fugledal
 */
public class Controller implements Runnable {
    
    Thread t;
    
    public Controller()
    {
        this.start();
    }
    
    /**
     * Starts the control thread
     */
    public void start()
    {
        t = new Thread(this, "ControlThread");
        t.start();
    }

    @Override
    public void run() 
    {
        this.runManual();
    }
    
    /**
     * Logic while running in auto mode
     */
    private void runAuto()
    {
        
    }
    
    /**
     * Logic while running in manual mode
     */
    private void runManual()
    {
        Main.dh.enableAUV();
        
        while(true)
        {
            if(1 == Main.dh.getFwd()){
                System.out.println("Running fwd");
            }
            else if (0 == Main.dh.getFwd()){
                System.out.println("Not Running fwd");
            }
            /*
            if(1 == Main.dh.getFwd())
                System.out.println("Running fwd");
            else if(0== Main.dh.getFwd())
                System.out.println("Not running fwd");
            
            if(1 == Main.dh.getRev())
                System.out.println("Running rev");
            if(1 == Main.dh.getLeft())
                System.out.println("Running left");
            if(1 == Main.dh.getRight())
                System.out.println("Running right");
           */
        }
    }
}
