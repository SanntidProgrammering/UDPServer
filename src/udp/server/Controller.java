/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eivind Fugledal
 */
public class Controller implements Runnable {
    
    private DataHandler dh;
    private Semaphore semaphore;

    Thread t;

    public Controller(DataHandler dh, Semaphore semaphore) {
        this.dh = dh;
        this.semaphore = semaphore;
    }

    /**
     * Starts the control thread
     */
    /*public void start() {
        t = new Thread(this, "ControlThread");
        t.start();
    }*/

    @Override
    public void run() {
        System.out.println("Controller is running");
        this.runManual();
    }

    /**
     * Logic while running in auto mode
     */
    private void runAuto() {

    }

    /**
     * Logic while running in manual mode
     */
    private void runManual() {

        byte rightSpeed = 0;
        byte leftSpeed = 0;
        
        boolean speedChanged = false;
        
        while (true) {
            
            try {
                semaphore.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (1 == dh.getFwd()) {
                if((leftSpeed != 100) || (rightSpeed != 100))
                {
                    rightSpeed = (byte) 100;
                    leftSpeed = (byte) 100;
                    
                    dh.releaseStopAUV();
                    
                    speedChanged = true;
                }
            } 
            else if (0 == dh.getFwd()) {
                if((leftSpeed != 0) || (rightSpeed != 0))
                {
                    rightSpeed = (byte) 0;
                    leftSpeed = (byte) 0;
                    
                    dh.stopAUV();
                    
                    speedChanged = true;
                }  
            }
            
            if(speedChanged)
            {
                dh.setLeftMotorSpeed(leftSpeed);
                dh.setRightMotorSpeed(rightSpeed);
                
                speedChanged = false;
            }
            
            semaphore.release();
        }
    }
}
