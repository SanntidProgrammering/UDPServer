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
 *
 * @author lars-harald
 */
public class AutoModeScheduler extends TimerTask {

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

    private final double pidOutputLimit = 100.0; // feks
    private final double speedFactor = 60.0; // % fart av maksimal hastighet

    public AutoModeScheduler(DataHandler dh, Semaphore semaphore, Logic logic) {
        this.pid = new MiniPID();
        this.semaphore = semaphore;
        this.dh = dh;
        this.logic = logic;

        this.pid.setOutputLimits(pidOutputLimit);
        //this.pid.setMaxIOutput(2);
        pid.setOutputRampRate(5.0);
        this.pid.setOutputFilter(.01);
        //miniPID.setSetpointRange(40);
    }

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

    private void advance() {

        output = pid.getOutput(xAngle, setpoint); // pid regulator
        output = limit(output, -pidOutputLimit, pidOutputLimit); // begrens output

        if (output != lastOutput) {
            double speed = 255.0 * (speedFactor / 100.0);  // maks hastighet rett frem
            double outputPidPercent = output / pidOutputLimit; // utgang fra pid i prosent av maks pådrag 

            float leftSpeed  = (float) min((speed * Math.abs(1 + outputPidPercent)), 255f);
            float rightSpeed = (float) min((speed * Math.abs(1 - outputPidPercent)), 255f);
            System.out.println("*************************************" + "PID OUTPUT: " + outputPidPercent + " SPEEDS, LEFT: " + leftSpeed + " RIGHT: " + rightSpeed);

            acquire();
            logic.runFWD(leftSpeed, rightSpeed);
            logic.decideToHitBallOrNot(dh.getDistanceSensor());
            dh.incrementRequestCode();
            release();
        }
        lastOutput = output;
    }

    private void searchLeft() {
        double percentTurnSpeed = 0.90d;
        double speed = 255.0 * (speedFactor / 100.0);  // maks hastighet rett frem

        float leftSpeed = (float) min((speed * Math.abs(1 + percentTurnSpeed)), 255f);
        float rightSpeed = (float) min((speed * Math.abs(1 - percentTurnSpeed)), 255f);

        acquire();
        logic.runFWD(leftSpeed, rightSpeed);
        dh.incrementRequestCode();
        release();
    }

    private void searchRight() {
        double percentTurnSpeed = -0.90d;
        double speed = 255.0 * (speedFactor / 100.0);  // maks hastighet rett frem

        float leftSpeed = (float) min((speed * Math.abs(1 + percentTurnSpeed)),255f);
        float rightSpeed = (float) min((speed * Math.abs(1 - percentTurnSpeed)),255f);

        acquire();
        logic.runFWD(leftSpeed, rightSpeed);
        dh.incrementRequestCode();
        release();
    }

    private double limit(double a, double MIN, double MAX) {
        return (a > MAX) ? MAX : (a < MIN ? MIN : a);
    }

    private double min(double a, double MIN) {
        return MIN < a ? MIN : a;
    }

    private AUTOMODES setState(double value) {
        AUTOMODES result = AUTOMODES.FWD;
        long intValue = Math.round(value);

        if (intValue == AUTOMODES.SEARCH_LEFT.value) {
            result = AUTOMODES.SEARCH_LEFT;
        } else if (intValue == AUTOMODES.SEARCH_RIGHT.value) {
            result = AUTOMODES.SEARCH_RIGHT;
        }

        return result;
    }
}
