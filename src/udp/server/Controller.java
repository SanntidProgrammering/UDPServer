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
        
        byte rightSpeed = 0;
        byte leftSpeed = 0;
        
        while(true)
        {
            if(1 == Main.dh.getFwd())
            {
                rightSpeed = (byte) 255;
                leftSpeed = (byte) 255;
            }
            else if(0 == Main.dh.getFwd())
            {
                rightSpeed = (byte) 0;
                leftSpeed = (byte) 0;
            }
            
            Main.dh.setLeftMotorSpeed(leftSpeed);
            Main.dh.setRightMotorSpeed(rightSpeed);
        }
    }
}
