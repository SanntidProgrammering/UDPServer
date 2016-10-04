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
            
    }
}
