/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AutoModeScheduler class, extends TimerTask
 * starts a new PID regulator that controls the movement of the vehicle from a given setpoint and feedback value
 * @author lars-harald
 */
public class AutoModeScheduler extends TimerTask {

    /**
     * the states of autonomus mode
     */
    private static enum AUTOMODES {
        FWD(1),
        SEARCH_LEFT(-255),
        SEARCH_RIGHT(255);

        private final int value;

        private AUTOMODES(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    private AUTOMODES state;
    private final MiniPID pid;
    private final DataHandler dh;
    private final Semaphore semaphore;
    private final Logic logic;

    private double xAngle;
    private final double setpoint = 0.0;
    private double output;
    private double lastOutput;

    private double P;
    private double I;
    private double D;
    private double F;
    private double RR;

    private final double pidOutputLimit = 255f / 2f; // feks
    private final double speedFactor = 60.0; // % fart av maksimal hastighet

    /**
     * create a new AutoModeScheduler
     * @param dh the shared resource
     * @param semaphore semaphore object
     * @param logic logic class 
     */
    public AutoModeScheduler(DataHandler dh, Semaphore semaphore, Logic logic) {
        this.semaphore = semaphore;
        acquire();
        P = dh.getP();
        I = dh.getI();
        D = dh.getD();
        F = dh.getF();
        RR = dh.getRR();
        release();

        this.pid = new MiniPID(P, I, D, F);
        this.pid.setOutputRampRate(RR);

        this.dh = dh;
        this.logic = logic;

        this.pid.setOutputLimits(pidOutputLimit);
        //this.pid.setMaxIOutput(2);
        pid.setOutputRampRate(5.0);
        this.pid.setOutputFilter(.01);
        //miniPID.setSetpointRange(40);

    }

    /**
     * run the AutoModeScheduler. starts the PID regulator 
     */
    @Override
    public void run() {

        // flag to check if pid parameters has changed
        boolean pidChanged = false;

        acquire();
        xAngle = (double) dh.getPixyXvalue();

        // get pid paramters if new values are available
        if (dh.getPidParamChanged()) {
            P = dh.getP();
            I = dh.getI();
            D = dh.getD();
            F = dh.getF();
            RR = dh.getRR();
            pidChanged = true;
            // new values has been stored
            dh.setPidParamChanged(false);
        }
        release();
        state = this.setState(xAngle);

        if (pidChanged) {
            pid.setPID(P, I, D, F);
            pid.setOutputRampRate(RR);
        }

        switch (state) {
            case FWD:
                pid.reset();
                this.advance();
                break;
            case SEARCH_LEFT:
                this.searchLeft();
                break;
            case SEARCH_RIGHT:
                this.searchRight();
                break;
        }

    }

    /**
     * acquire the semaphore
     */
    private void acquire() {
        try {
            semaphore.acquire();

        } catch (InterruptedException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * release the semaphore
     */
    private void release() {
        semaphore.release();
    }

    /**
     * PID tracking object, object in sight of  the camera
     */
    private void advance() {

        output = pid.getOutput(xAngle, setpoint); // pid regulator
        output = limit(output, -pidOutputLimit, pidOutputLimit); // begrens output

        //if (output != lastOutput) {
        //double speed = 255.0 * (speedFactor / 100.0);  // maks hastighet rett frem
        //double outputPidPercent = output / pidOutputLimit; // utgang fra pid i prosent av maks pådrag 
        float leftSpeed = (255f / 2f) + (float) output;
        float rightSpeed = (255f / 2f) - (float) output;

        //float leftSpeed  = (float) min((speed * Math.abs(1 + outputPidPercent)), 255f);
        //float rightSpeed = (float) min((speed * Math.abs(1 - outputPidPercent)), 255f);
        System.out.println("*************************************" + "PID OUTPUT: " + output + " SPEEDS, LEFT: " + leftSpeed + " RIGHT: " + rightSpeed);

        acquire();
        logic.runFWD(leftSpeed, rightSpeed);
        logic.decideToHitBallOrNot(dh.getDistanceSensor());
        dh.incrementRequestCode();
        release();
        // }
        lastOutput = output;
    }

    /**
     * searching for the object, object in left hand of the camera
     */
    private void searchLeft() {
        double percentTurnSpeed = 0.80d;
        double speed = 255.0 * (speedFactor / 100.0);  // maks hastighet rett frem

        float leftSpeed = (float) min((speed * Math.abs(1 + percentTurnSpeed)), 255f);
        float rightSpeed = (float) min((speed * Math.abs(1 - percentTurnSpeed)), 255f);

        acquire();
        logic.runFWD(leftSpeed, rightSpeed);
        dh.incrementRequestCode();
        release();
    }

    /**
     * searching for the object, object in right hand of the camera
     */
    private void searchRight() {
        double percentTurnSpeed = -0.80d;
        double speed = 255.0 * (speedFactor / 100.0);  // maks hastighet rett frem

        float leftSpeed = (float) min((speed * Math.abs(1 + percentTurnSpeed)), 255f);
        float rightSpeed = (float) min((speed * Math.abs(1 - percentTurnSpeed)), 255f);

        acquire();
        logic.runFWD(leftSpeed, rightSpeed);
        dh.incrementRequestCode();
        release();
    }

    /**
     * limit a double value
     * @param a
     * @param MIN
     * @param MAX
     * @return 
     */
    private double limit(double a, double MIN, double MAX) {
        return (a > MAX) ? MAX : (a < MIN ? MIN : a);
    }

    /**
     * get the minimum value of two double's
     * @param a
     * @param MIN
     * @return 
     */
    private double min(double a, double MIN) {
        return MIN < a ? MIN : a;
    }

    /**
     * set the state of autonomus mode
     * @param value
     * @return 
     */
    private AUTOMODES setState(double value) {
        AUTOMODES result = AUTOMODES.FWD;
        long intValue = Math.round(value);

        if (intValue == AUTOMODES.SEARCH_LEFT.value) {
            result = AUTOMODES.SEARCH_LEFT;
        } else if (intValue == AUTOMODES.SEARCH_RIGHT.value) {
            result = AUTOMODES.SEARCH_RIGHT;
        }
        System.out.println("STATE CHANGED " + result);
        return result;
    }
}
