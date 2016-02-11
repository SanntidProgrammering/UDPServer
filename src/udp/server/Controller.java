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
    private boolean autoRunning;

    private final long PIDperiodeTime = 50;

    private Thread t;

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

        //this.startRequestFeedbacks();
        while (dh.shouldThreadRun()) {
            acquire();

            boolean guiCommandUpdated = dh.getDataFromGuiAvailable();
            if (guiCommandUpdated) {
                byte controlByte = dh.getFromGuiByte((byte) Protocol.COMMANDS.getValue());
                
                // auto or manual mode
                if (getBit(controlByte, Protocol.commands.AUTO_MANUAL.getValue())) {
                    dh.AUVautoMode();
                } else {
                    dh.AUVmanualMode();
                }
                
                // start or stop vehicle
                if(getBit(controlByte, Protocol.commands.START.getValue())) {
                    dh.enableAUV();
                } else {
                    dh.disableAUV();
                }
                // finish prosessing commands from gui
                dh.setDataFromGuiAvailable(false);
            }
            
            AUVstate = dh.getAUVautoMode();
            release();

            if (AUVstate == 1 && lastAUVstate == 0 && !this.autoRunning) {
                this.startPID();

            } else if (AUVstate == 0) {
                if (lastAUVstate == 1) {
                    // skifter til manuell modus, stopp timer task på pid
                    if (this.autoRunning) {
                        this.cancelPID();

                    }
                }
                if (guiCommandUpdated) {
                    this.runManual();
                }
            }

            lastAUVstate = AUVstate;
        }
        this.cancelPID();
    }

    /**
     * start scheduler PID
     */
    private void startPID() {
        try {
            timer = new Timer();
            timer.scheduleAtFixedRate(new AutoModeScheduler(dh, semaphore, logic), 0, PIDperiodeTime);
            this.autoRunning = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * stop scheduler PID
     */
    private void cancelPID() {
        try {
            timer.cancel();
            //timer.purge();
            this.autoRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logic while running in manual mode
     */
    private void runManual() {
        acquire();

        logic.prossesButtonCommandsFromGui();
        // set requestCode from GUI to arduino
        dh.setRequestCodeToArduino(dh.getRequestCodeFromGui());

        //dh.setDataFromGuiAvailable(false);
        release();
    }

    private void startRequestFeedbacks() {
        Runnable run;
        run = () -> {
            try {
                while (dh.shouldThreadRun()) {
                    acquire();
                    dh.incrementRequestCode();
                    release();
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

    /**
     * Gets a specific bit in a specific byte
     *
     * @param b The specific byte
     * @param bit The specific bit
     * @return Value of the bit
     */
    private boolean getBit(byte b, int bit) {
        return ((b >> bit) == 1);
    }
}
