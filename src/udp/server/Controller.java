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
    
    int rightSpeed = 0;
    int leftSpeed = 0;

    Thread t;

    public Controller(DataHandler dh, Semaphore semaphore) {
        this.dh = dh;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
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
        
        while (true) {
            
            try {
                semaphore.acquire();
                
            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(dh.getDataFromGuiAvailable())
            {
                if(1 != dh.getStopAUV())
                {
                    if(1 == dh.getFwd())
                       this.runFWD();
                    else if(1 == dh.getRev())
                      this.runRev();
                    else if(1 == dh.getLeft())
                      this.runLeft();
                    else if(1 == dh.getRight())
                      this.runRight();
                }
            else 
            {
                this.stop();
            }
            
                dh.setLeftMotorSpeed(leftSpeed);
                dh.setRightMotorSpeed(rightSpeed);
            
                dh.setDataFromGuiAvailable(false);
            }
            
            semaphore.release();
        }
    }
    
    /**
     * Sets motor speed to run forward
     */
    private void runFWD()
    {
        rightSpeed = 255;
        leftSpeed = 255;
        
        dh.releaseStopAUV();
    }
    
    /**
     * Sets motor speed to run reverse
     */
    private void runRev()
    {
        rightSpeed = 255;
        leftSpeed = 255;
        
        dh.releaseStopAUV();
    }
    
    /**
     * Sets motor speed to run left
     */
    private void runLeft()
    {
        rightSpeed = 255;
        leftSpeed = 0;
        
        dh.releaseStopAUV();
    }
    
    /**
     * Sets motor speed to run right
     */
    private void runRight()
    {
        rightSpeed = 0;
        leftSpeed = 255;
        
        dh.releaseStopAUV();
    }
    
    /**
     * Sets motor speed zero/stop
     */
    private void stop()
    {
        rightSpeed = 0;
        leftSpeed = 0;
        
        dh.stopAUV();
    }
}
