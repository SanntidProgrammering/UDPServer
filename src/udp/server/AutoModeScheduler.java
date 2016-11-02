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

    private final MiniPID pid;
    private final DataHandler dh;
    private final Semaphore semaphore;
    private final Logic logic;

    private double xAngle;
    private double setpoint = 0.0;
    private double output;
    private double lastOutput;

    private double P = 0.5;
    private double I = 0.1;
    private double D = 0.0;
    
    
    private double pidOutputLimit = 10.0; // feks
    private double speedFactor = 70.0; // % fart av maksimal hastighet

    public AutoModeScheduler(DataHandler dh, Semaphore semaphore, Logic logic) {
        this.pid = new MiniPID(P, I, D);
        this.semaphore = semaphore;
        this.dh = dh;
        this.logic = logic;

        this.pid.setOutputLimits(pidOutputLimit);
        this.pid.setMaxIOutput(2);
        //miniPID.setOutputRampRate(3);
        this.pid.setOutputFilter(.3);
        //miniPID.setSetpointRange(40);
    }

    @Override
    public void run() {
        acquire();
        xAngle = dh.getPixyXvalue();
        release();
        output = limit(pid.getOutput(xAngle, setpoint),-pidOutputLimit,pidOutputLimit);
        if (output != lastOutput) {
            //                           max hastighet rett frem            %pådrag fra pid regulator
            float leftSpeed = (float) ((255.0 * (speedFactor / 100.0)) * Math.abs(1 - (output / pidOutputLimit)));
            float rightSpeed = (float) ((255.0 * (speedFactor / 100.0)) * Math.abs(1 + (output / pidOutputLimit)));
            
            acquire();
            logic.runFWD(leftSpeed, rightSpeed);
            logic.decideToHitBallOrNot(dh.getDistanceSensor());
            release();
        }
        lastOutput = output;
    }
    
    private void acquire() {
        try {
            semaphore.acquire();

        } catch (InterruptedException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setSetpoint(double setpoint) {
        this.setpoint = setpoint;
    }

    public void setP(double P) {
        this.P = P;
    }

    public void setI(double I) {
        this.I = I;
    }

    public void setD(double D) {
        this.D = D;
    }

    public void setPidOutputLimit(double pidOutputLimit) {
        this.pidOutputLimit = pidOutputLimit;
    }

    public void setSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
    }

    private void release() {
        semaphore.release();
    }

    private double limit(double a, double MIN, double MAX) {
        return (a > MAX) ? MAX : (a < MIN ? MIN : a);
    }
}
