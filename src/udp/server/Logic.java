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

    protected enum STATES {
        STOP(0),
        GOFWD(1),
        GOREV(-1),
        GOLEFT(10),
        GORIGHT(20),
        GOFWDANDLEFT(11),
        GOFWDANDRIGHT(21),
        GOREVANDLEFT(9),
        GOREVANDRIGHT(19),
        DEFAULT(-99);

        private int value;

        private STATES(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    private final DataHandler dh;
    private final int maxSpeed = 255;
    private final int minSpeed = 0;
    private STATES state;

    public Logic(DataHandler dh) {
        this.dh = dh;
    }

    protected void prossesButtonCommandsFromGui() {
        this.handleButtonStates();
        this.switchCaseButtonStates();
        this.switchCaseMotorSpeeds();

    }

    /**
     * Sets motor speed to run forward
     *
     * @param leftSpeed
     * @param rightSpeed
     */
    protected void runFWD(float leftSpeed, float rightSpeed) {
        this.setState(STATES.GOFWD);
        this.switchCaseButtonStates();
        this.setLeftSpeed(leftSpeed);
        this.setRightSpeed(rightSpeed);
    }

    /**
     * Sets motor speed to run reverse
     * @param leftSpeed
     * @param rightSpeed
     */
    protected void runRev(float leftSpeed, float rightSpeed) {
        this.setState(STATES.GOREV);
        this.switchCaseButtonStates();
        this.setLeftSpeed(leftSpeed);
        this.setRightSpeed(rightSpeed);
    }

    /**
     * Sets motor speed to run left
     */
    protected void runLeft() {
        this.setState(STATES.GOLEFT);
        this.switchCaseButtonStates();
        this.switchCaseMotorSpeeds();
    }

    /**
     * Sets motor speed to run right
     */
    protected void runRight() {
        this.setState(STATES.GORIGHT);
        this.switchCaseButtonStates();
        this.switchCaseMotorSpeeds();
    }

    /**
     * Sets motor speed zero/stop
     */
    protected void stop() {
        this.setState(STATES.STOP);
        this.switchCaseButtonStates();
        this.switchCaseMotorSpeeds();

    }

    protected void handleButtonStates() {
        // setter først buttonstate til null
        int buttonState = 0;
        // sjekker at controlbyte er ulik null
        if (0 != dh.getFromGuiByte((byte) 0)) {
            // sjekker at ingen kommandoer er ulovlige (frem/bak samtidig)
            if (!(((1 == dh.getFwd()) && (1 == dh.getRev())) || ((1 == dh.getLeft()) && (1 == dh.getRight())))) {
                // gå frem
                if (1 == dh.getFwd()) {

                    buttonState = STATES.GOFWD.getValue();
                    // gå bakover    
                } else if (1 == dh.getRev()) {
                    buttonState = STATES.GOREV.getValue();
                }
                if (1 == dh.getLeft()) {
                    buttonState += STATES.GOLEFT.getValue();
                } else if (1 == dh.getRight()) {
                    buttonState += STATES.GORIGHT.getValue();
                }
            }
        }
        this.setStateByValue(buttonState);
    }

    protected void switchCaseButtonStates() {
        dh.resetToArduinoByte(0);
        switch (this.getState()) {
            case STOP:
                dh.stopAUV();
                break;
            case GOFWD:
                dh.goFwd();
                break;
            case GOREV:
                dh.goRev();
                break;
            case GOLEFT:
                dh.goLeft();
                break;
            case GORIGHT:
                dh.goRight();
                break;
            case GOFWDANDLEFT:
                dh.goFwd();
                break;
            case GOFWDANDRIGHT:
                dh.goFwd();
                break;
            case GOREVANDRIGHT:
                dh.goRev();
                break;
            case GOREVANDLEFT:
                dh.goRev();
                break;
            // unknown command
            case DEFAULT:
                break;
            // just to be safe
            default:
                break;
        }

    }

    protected void switchCaseMotorSpeeds() {
        switch (this.getState()) {
            case STOP:
                dh.setLeftMotorSpeed(minSpeed);
                dh.setRightMotorSpeed(minSpeed);
                break;
            case GOFWD:
                dh.setLeftMotorSpeed(maxSpeed);
                dh.setRightMotorSpeed(maxSpeed);
                break;
            case GOREV:
                dh.setLeftMotorSpeed(maxSpeed);
                dh.setRightMotorSpeed(maxSpeed);
                break;
            case GOLEFT:
                dh.setLeftMotorSpeed(maxSpeed);
                dh.setRightMotorSpeed(maxSpeed);
                break;
            case GORIGHT:
                dh.setLeftMotorSpeed(maxSpeed);
                dh.setRightMotorSpeed(maxSpeed);
                break;
            case GOFWDANDLEFT:
                dh.setLeftMotorSpeed(maxSpeed / 4);
                dh.setRightMotorSpeed(maxSpeed);
                break;
            case GOFWDANDRIGHT:
                dh.setLeftMotorSpeed(maxSpeed);
                dh.setRightMotorSpeed(maxSpeed / 4);
                break;
            case GOREVANDRIGHT:
                dh.setLeftMotorSpeed(maxSpeed);
                dh.setRightMotorSpeed(maxSpeed / 4);
                break;
            case GOREVANDLEFT:
                dh.setLeftMotorSpeed(maxSpeed / 4);
                dh.setRightMotorSpeed(maxSpeed);
                break;
            // unknown command
            case DEFAULT:
                break;
            // just to be safe
            default:
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

            // starter bakgrunnstråd og holder servo ute en stund selv om metoden returnerer
            Runnable run = () -> {
                try {
                    dh.setLeftServo();
                    Thread.sleep(2000);
                    dh.resetLeftServo();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            new Thread(run).start();
        }
    }

    public void setRightSpeed(float rightSpeed) {
        dh.setRightMotorSpeed(rightSpeed);
    }

    public void setLeftSpeed(float leftSpeed) {
        dh.setLeftMotorSpeed(leftSpeed);
    }

   private STATES getState() {
        return state;
    }

    private void setState(STATES state) {
        this.state = state;
    }

    private void setStateByValue(int value) {
        this.state = this.findState(value);
    }

    private STATES findState(int value) {
        STATES[] stateArray = STATES.values();
        for (STATES s : stateArray) {
            if (s.getValue() == value) {
                return s;
            }
        }
        return STATES.DEFAULT;
    }

}
