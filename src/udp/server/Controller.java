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
    
    byte rightSpeed = 0;
    byte leftSpeed = 0;
    boolean speedChanged = false;

    Thread t;

    public Controller(DataHandler dh, Semaphore semaphore) {
        this.dh = dh;
        this.semaphore = semaphore;
    }

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
        
        while (true) {
            
            try {
                semaphore.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(0 != dh.getByte((byte) 0))
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
            
            if((leftSpeed != dh.getLeftMotorSpeed()) && (rightSpeed != dh.getRightMotorSpeed()))
                speedChanged = true;
            else
                speedChanged = false;
            
            if(speedChanged)
            {
                dh.setLeftMotorSpeed(leftSpeed);
                dh.setRightMotorSpeed(rightSpeed);
            }
            
            semaphore.release();
        }
    }
    
    private void runFWD()
    {
        rightSpeed = (byte) 255;
        leftSpeed = (byte) 255;
        
        speedChanged = true;
    }
    
    private void runRev()
    {
        rightSpeed = (byte) 255;
        leftSpeed = (byte) 255;
        
        speedChanged = true;
    }
    
    private void runLeft()
    {
        rightSpeed = (byte) 255;
        leftSpeed = (byte) 0;
        
        speedChanged = true;
    }
    
    private void runRight()
    {
        rightSpeed = (byte) 0;
        leftSpeed = (byte) 255;
        
        speedChanged = true;
    }
    
    private void stop()
    {
        rightSpeed = (byte) 0;
        leftSpeed = (byte) 0;
        
        speedChanged = true;
    }
}
