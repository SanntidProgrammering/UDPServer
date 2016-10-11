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
    int maxSpeed = 255;
    int minSpeed = 0;

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
                if(dh.isDataFromArduinoAvailable())
                {
                    System.out.println("Pixy x value: " + dh.getPixyXvalue());
                    System.out.println("Pixy y value: " + dh.getPixyYvalue());
                    System.out.println("Distance: " + dh.getDistanceSensor());
                }
                
                if(0 != dh.getByte((byte) 0))
                {
                    if(!(((1 == dh.getFwd()) && (1 == dh.getRev())) || ((1 == dh.getLeft()) && (1 == dh.getRight()))))
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
        rightSpeed = maxSpeed;
        leftSpeed = maxSpeed;
        
        dh.releaseStopAUV();
    }
    
    /**
     * Sets motor speed to run reverse
     */
    private void runRev()
    {
        rightSpeed = maxSpeed;
        leftSpeed = maxSpeed;
        
        dh.releaseStopAUV();
    }
    
    /**
     * Sets motor speed to run left
     * 
     * If running forward or reverse and left at the same time, left motor should run
     * with half the speed compared to the right 
     * If only running left, right motor run at full speed
     */
    private void runLeft()
    {
        if((1 == dh.getFwd()) || (1 == dh.getRev()))
        {
            rightSpeed = maxSpeed;
            leftSpeed = rightSpeed/2;
        }
        else
        {
            rightSpeed = maxSpeed;
            leftSpeed = minSpeed;
        }
        
        dh.releaseStopAUV();
    }
    
    /**
     * Sets motor speed to run right
     * 
     * If running forward or reverse and right at the same time, left motor should run
     * with half the speed compared to the left 
     * If only running right, left motor run at full speed
     */
    private void runRight()
    {
        if((1 == dh.getFwd()) || (1 == dh.getRev()))
        {
            leftSpeed = maxSpeed;
            rightSpeed = leftSpeed/2;
        }
        else
        {
            rightSpeed = minSpeed;
            leftSpeed = maxSpeed;
        }
        
        dh.releaseStopAUV();
    }
    
    /**
     * Sets motor speed zero/stop
     */
    private void stop()
    {
        rightSpeed = minSpeed;
        leftSpeed = minSpeed;
        
        dh.stopAUV();
    }
}
