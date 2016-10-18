/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lars-harald
 */
public class Logic {

    private final DataHandler dh;
    private int rightSpeed = 0;
    private int leftSpeed = 0;
    private int maxSpeed = 255;
    private int minSpeed = 0;
    private int buttonState;

    public Logic(DataHandler dh) {
        this.dh = dh;
    }

    protected void prossesButtonCommandsFromGui() {
        this.handleButtonStates();
        this.switchCaseButtonStates();
        dh.setLeftMotorSpeed(leftSpeed);
        dh.setRightMotorSpeed(rightSpeed);
    }

    /**
     * Sets motor speed to run forward
     */
    protected void runFWD() {
        rightSpeed = maxSpeed;
        leftSpeed = maxSpeed;

        dh.goFwd();
        dh.releaseStopAUV();
    }

    /**
     * Sets motor speed to run reverse
     */
    protected void runRev() {
        rightSpeed = maxSpeed;
        leftSpeed = maxSpeed;

        dh.goRev();
        dh.releaseStopAUV();
    }

    /**
     * Sets motor speed to run left
     *
     * If running forward or reverse and left at the same time, left motor
     * should run with half the speed compared to the right If only running
     * left, right motor run at full speed
     */
    protected void runLeft() {
        if ((1 == dh.getFwd()) || (1 == dh.getRev())) {
            rightSpeed = maxSpeed;
            leftSpeed = rightSpeed / 2;

            dh.goFwd();
        } else {
            rightSpeed = maxSpeed;
            leftSpeed = maxSpeed;

            dh.goLeft();
        }
        dh.releaseStopAUV();
    }

    /**
     * Sets motor speed to run right
     *
     * If running forward or reverse and right at the same time, left motor
     * should run with half the speed compared to the left If only running
     * right, left motor run at full speed
     */
    protected void runRight() {
        if ((1 == dh.getFwd()) || (1 == dh.getRev())) {
            leftSpeed = maxSpeed;
            rightSpeed = leftSpeed / 2;

            dh.goFwd();
        } else {
            rightSpeed = maxSpeed;
            leftSpeed = maxSpeed;

            dh.goRight();
        }
        dh.releaseStopAUV();
    }

    /**
     * Sets motor speed zero/stop
     */
    protected void stop() {
        rightSpeed = minSpeed;
        leftSpeed = minSpeed;

        dh.stopAUV();
    }

    protected void handleButtonStates() {
        if (0 != dh.getFromGuiByte((byte) 0)) {
            if (!(((1 == dh.getFwd()) && (1 == dh.getRev())) || ((1 == dh.getLeft()) && (1 == dh.getRight())))) {
                if (1 == dh.getFwd()) {
                    buttonState = 1;
                } else if (1 == dh.getRev()) {
                    buttonState = -1;
                } else {
                    buttonState = 0;
                }
                if (1 == dh.getLeft()) {
                    buttonState += 10;
                } else if (1 == dh.getRight()) {
                    buttonState += 20;
                }
            } else {
                buttonState = 0;
            }
        } else {
            buttonState = 0;
        }
    }

    protected void switchCaseButtonStates() {
        dh.resetToArduinoByte(0);
        switch (buttonState) {
            case 0:
                leftSpeed = minSpeed;
                rightSpeed = minSpeed;
                dh.stopAUV();
                break;
            case 1:
                leftSpeed = maxSpeed;
                rightSpeed = maxSpeed;
                dh.goFwd();
                break;
            case -1:
                leftSpeed = maxSpeed;
                rightSpeed = maxSpeed;
                dh.goRev();
                break;
            case 10:
                leftSpeed = maxSpeed;
                rightSpeed = maxSpeed;
                dh.goLeft();
                break;
            case 20:
                leftSpeed = maxSpeed;
                rightSpeed = maxSpeed;
                dh.goRight();
                break;
            case 21:
                leftSpeed = maxSpeed / 4;
                rightSpeed = maxSpeed;
                dh.goFwd();
                break;
            case 11:
                leftSpeed = maxSpeed;
                rightSpeed = maxSpeed / 4;
                dh.goFwd();
                break;
            case 9:
                leftSpeed = maxSpeed;
                rightSpeed = maxSpeed / 4;
                dh.goRev();
                break;
            case 19:
                leftSpeed = maxSpeed / 4;
                rightSpeed = maxSpeed;
                dh.goRev();
                break;
        }

    }

    protected void handleServoStatesFromGui() {
        if (dh.getLeftServo() == 1) {
            dh.setLeftServo();
        } else {
            dh.resetLeftServo();
        }

        if (dh.getRightServo() == 1) {
            dh.setRightServo();
        } else {
            dh.resetRightServo();
        }
    }

    protected void decideToHitBallOrNot(int distance) {
        if (distance >= dh.getDistanceSensor()) {
            dh.setLeftServo();

            // starter bakgrunnstråd og holder servo ute en stund selv om metoden returnerer
            Runnable run = () -> {
                try {
                    Thread.sleep(2000);
                    dh.resetLeftServo();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            new Thread(run).start();
        }
    }

    public int getRightSpeed() {
        return rightSpeed;
    }

    public int getLeftSpeed() {
        return leftSpeed;
    }

    public int getButtonState() {
        return buttonState;
    }

}
