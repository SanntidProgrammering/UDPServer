/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.util.Timer;
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
    private Timer timer;
    private byte AUVstate;
    private byte lastAUVstate;
    private long PIDperiodeTime = 100;

    Thread t;

    public Controller(DataHandler dh, Semaphore semaphore) {
        this.dh = dh;
        this.semaphore = semaphore;
        this.logic = new Logic(this.dh);
        this.timer = new Timer();
    }

    public void start() {
        t = new Thread(this, "controller thread");
        t.start();
    }

    @Override
    public void run() {

        this.startRequestFeedbacks();
        acquire();
        boolean run = dh.shouldThreadRun();
        release();

        while (run) {
            acquire();
            run = dh.shouldThreadRun();
            AUVstate = dh.getAUVautoMode();
            release();
            if (AUVstate == 1 && lastAUVstate == 0) {
                // start ny pid regulering
                timer.scheduleAtFixedRate(new PidScheduler(dh, semaphore), 0, PIDperiodeTime);

            } else if (AUVstate == 0) {
                if (lastAUVstate == 1) {
                    // skifter til manuell modus, stopp timer task på pid
                    this.canselPID();
                }
                this.runManual();
            }
            lastAUVstate = AUVstate;
        }
        this.canselPID();
    }

    /**
     * stop scheduler PID
     */
    private void canselPID() {
        timer.cancel();
        timer.purge();
    }

    /**
     * Logic while running in manual mode
     */
    private void runManual() {
        acquire();
        boolean dataFromGui = dh.getDataFromGuiAvailable();
        release();
        if (dataFromGui) {
            acquire();
            
            if (dh.isDataFromArduinoAvailable()) {
                System.out.println("Camera x value: " + dh.getPixyXvalue());
                System.out.println("Camera y value: " + dh.getPixyYvalue());
                System.out.println("Distance: " + dh.getDistanceSensor());
            }
            logic.prossesButtonCommandsFromGui();

            dh.setDataFromGuiAvailable(false);
            release();
        }
    }

    private void startRequestFeedbacks() {
        Runnable run;
        run = () -> {
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

    private void acquire() {
        try {
            semaphore.acquire();

        } catch (InterruptedException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void release() {
        semaphore.release();
    }
}
