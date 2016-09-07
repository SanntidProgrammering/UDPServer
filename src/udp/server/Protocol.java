/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

/**
 *
 * @author mgrib
 */
public enum Protocol {
    START(0), 
    AUTO(1), 
    FWD(2), 
    LEFT(3), 
    REV(4), 
    RIGHT(5), 
    ATTACK(6), 
    SENS(7), 
    FORCE(8);
    private int value;
    
    private Protocol(int value) {
	this.value = value;
    }
    public int getValue(){
        return this.value;
    }
}
    
