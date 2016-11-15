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

    private final double P = 0.3;
    private final double I = 0.1;
    private final double D = 0.1;
    private final double F = 0.5;
    
    
    private final double pidOutputLimit = 100.0; // feks
    private final double speedFactor = 50.0; // % fart av maksimal hastighet
    
    

    public AutoModeScheduler(DataHandler dh, Semaphore semaphore, Logic logic) {
        this.pid = new MiniPID(P, I, D, F);
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
        acquire();
        xAngle = (double) dh.getPixyXvalue();
        
        release();
        state = this.setState(xAngle);

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

            float leftSpeed = (float) (speed * Math.abs(1 + outputPidPercent));
            float rightSpeed = (float) (speed * Math.abs(1 - outputPidPercent));
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
        double percentTurnSpeed = 0.80d;
        double speed = 255.0 * (speedFactor / 100.0);  // maks hastighet rett frem

        float leftSpeed = (float) (speed * Math.abs(1 + percentTurnSpeed));
        float rightSpeed = (float) (speed * Math.abs(1 - percentTurnSpeed));

        acquire();
        logic.runFWD(leftSpeed, rightSpeed);
        dh.incrementRequestCode();
        release();
    }

    private void searchRight() {
        double percentTurnSpeed = -0.80d;
        double speed = 255.0 * (speedFactor / 100.0);  // maks hastighet rett frem

        float leftSpeed = (float) (speed * Math.abs(1 + percentTurnSpeed));
        float rightSpeed = (float) (speed * Math.abs(1 - percentTurnSpeed));

        acquire();
        logic.runFWD(leftSpeed, rightSpeed);
        dh.incrementRequestCode();
        release();
    }

    private double limit(double a, double MIN, double MAX) {
        return (a > MAX) ? MAX : (a < MIN ? MIN : a);
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
