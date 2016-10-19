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

    private final DataHandler dh;
    private final Semaphore semaphore;
    private final Logic logic;

    Thread t;

    public Controller(DataHandler dh, Semaphore semaphore) {
        this.dh = dh;
        this.semaphore = semaphore;
        this.logic = new Logic(this.dh);
    }
    
    public void start(){
        t = new Thread(this,"controller thread");
        t.start();
    }

    @Override
    public void run() {

        this.startRequestFeedbacks();

        while (dh.shouldThreadRun()) {
            
            try {
                semaphore.acquire();

            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (dh.getAUVautoMode() == 1) {

                this.runAuto();

            } else if (dh.getAUVautoMode() == 0) {

                this.runManual();

            }
            semaphore.release();
        }
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

        if (dh.getDataFromGuiAvailable()) {
            if (dh.isDataFromArduinoAvailable()) {
                System.out.println("Pixy x value: " + dh.getPixyXvalue());
                System.out.println("Pixy y value: " + dh.getPixyYvalue());
                System.out.println("Distance: " + dh.getDistanceSensor());
            }
            logic.prossesButtonCommandsFromGui();

            dh.setDataFromGuiAvailable(false);
        }

    }

    private void startRequestFeedbacks() {
        Runnable run = () -> {
            try {
                while (dh.shouldThreadRun()) {
                    dh.incrementRequestCode();
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        new Thread(run).start();
    }
}
