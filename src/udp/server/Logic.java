/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logic class
 * handles the logic that controls the vehicle and commands
 * @author lars-harald
 */
public class Logic {

    /**
     * states of movement of the vehicle algorithm: gofwd/rev + left/right if
     * fwd and left active: 1 + 10 = 11 = GOFWDANDLEFT-state
     */
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
    private final int distanceToHitBall = 22;
    
    private boolean isServoOut = false;
    
    private STATES state;


    /**
     * create new logic class
     *
     * @param dh datahandler
     */
    public Logic(DataHandler dh) {
        this.dh = dh;

    }

    /**
     * run this method when vehicle is in manual mode, controlled from gui
     */
    protected void prossesButtonCommandsFromGui() {
        // vehicle is in manual mode
        this.isServoOut = false;
        this.handleButtonStates();
        this.handleServoStatesFromGui();
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
     *
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

    /**
     * select the correct state from gui buttons pushed
     */
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

    protected void handleAutoStates(double setp, double xAngle, int dist) {
        // setter først buttonstate til null
        int buttonState = 0;
    

    }

    /**
     * selects the correct movement of the vehicle from state
     */
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

    /**
     * sets the correct motorspeeds from state (manual mode)
     */
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
                System.out.println("fwd and left");
                dh.setLeftMotorSpeed(maxSpeed);
                dh.setRightMotorSpeed(maxSpeed/4);
                break;
            case GOFWDANDRIGHT:
                dh.setLeftMotorSpeed(maxSpeed/4);
                dh.setRightMotorSpeed(maxSpeed);
                break;
            case GOREVANDRIGHT:
                dh.setLeftMotorSpeed(maxSpeed/4);
                dh.setRightMotorSpeed(maxSpeed);
                break;
            case GOREVANDLEFT:
                dh.setLeftMotorSpeed(maxSpeed);
                dh.setRightMotorSpeed(maxSpeed/4);
                break;
            // unknown command
            case DEFAULT:
                break;
            // just to be safe
            default:
                break;
        }
    }

    /**
     * sets the servo motors (manual mode)
     */
    protected void handleServoStatesFromGui() {
        if (dh.getServoFromGui() == 1) {
            dh.setServoToArduino();
        } else {
            dh.resetServoToArduino();
        }
    }

    /**
     * decide to hit the ball or not, using the left servo
     *
     * @param distance the distance that triggers the servo
     */
    protected void decideToHitBallOrNot(int distance) {
        if (distance <= this.distanceToHitBall && !this.isServoOut) {
            
            // starter bakgrunnstråd og holder servo ute en stund selv om metoden returnerer
            Runnable run = () -> {
                try {
                    // kaller uten semafore. bare et bit som skrus av eller på.
                    dh.setServoToArduino();
                    isServoOut = true;
                    Thread.sleep(1000);
                    dh.resetServoToArduino();
                    isServoOut = false;
                } catch (InterruptedException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            new Thread(run).start();
           
        }
    }

    /**
     * set right motorspeed
     *
     * @param rightSpeed
     */
    public void setRightSpeed(float rightSpeed) {
        dh.setRightMotorSpeed(rightSpeed);
    }

    /**
     * set left motorspeed
     *
     * @param leftSpeed
     */
    public void setLeftSpeed(float leftSpeed) {
        dh.setLeftMotorSpeed(leftSpeed);
    }

    /**
     * get the state the vehicle is currently in
     *
     * @return
     */
    private STATES getState() {
        return state;
    }

    /**
     * set the state of movement to the vehicle
     *
     * @param state the new state of the vehicle
     */
    private void setState(STATES state) {
        this.state = state;
    }

    /**
     * set state by value of the wanted state
     *
     * @param value integer value of the state
     */
    private void setStateByValue(int value) {
        this.state = this.findState(value);
    }

    /**
     * find a state by state value
     *
     * @param value the integer representation of the state
     * @return
     */
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
