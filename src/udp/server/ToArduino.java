/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

/**
 *
 * @author Eivind Fugledal
 */
public enum ToArduino {
    
    CONTROLS(0),
    LEFT_MOTOR_SPEED(1),
    RIGHT_MOTOR_SPEED(2),
    COMMANDS(3),
    SENSITIVITY(4),
    RESERVED(5);
    
    private int value;
       
    private ToArduino(int value) {
        this.value = value;
    }
        
    public int getValue() {
        return this.value;
    }
        
    public enum controls {
        STOP(0),
        FORWARD(1),
        REVERSE(2),
        LEFT(3),
        RIGHT(4);
        
        private int value;
        
        private controls(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
    
    public enum commands {
        LEFT_SERVO(0),
        RIGHT_SERVO(1),
        AUTO_MANUAL(2),
        START(3),
        REQUEST_FEEDBACK(7);
        
        private int value;
        
        private commands(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
}
