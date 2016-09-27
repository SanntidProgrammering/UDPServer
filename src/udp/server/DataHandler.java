/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

/**
 * Overview protocol:
 * To Arduino:
 * Byte 0: bit 0 - stopp
 *         bit 1 - fwd
 *         bit 2 - rev
 *         bit 3 - left
 *         bit 4 - right
 * Byte 1: Left motor speed
 * Byte 2: Right motor speed
 * Byte 3: bit 0 - left servo
 *         bit 1 - right servo
 *         bit 2 - auto/manual
 *         bit 3 - start
 *         bit 7 - request feedback
 * Byte 4: Sensitivity
 * Byte 5: Reserved
 * 
 * From Arduino:
 * Byte 0: Pixy x value low byte
 * Byte 1: Pixy x value high byte
 * Byte 2: Pixy y value low byte
 * Byte 3: Pixy y value high byte
 * Byte 4: Distance sensor 4-30 cm
 * Byte 5: Reserved
 * 
 * @author Eivind Fugledal
 */
public class DataHandler {
    
    private byte[] fromGUI;
    private byte[] fromArduino;
    
    /**
     * 
     */
    public DataHandler()
    {
        fromGUI = new byte[6];
        fromArduino = new byte[6];
    }
    
    /**
     * Updates byte arrays when receiving from either GUI or Arduino
     * @param data New data set from GUI/Arduino
     * @param id An identifier used to set correct byte array
     */
    public synchronized void setData(byte[] data, String id) throws IllegalArgumentException
    {
        if(id.toLowerCase().equals("gui"))
        {
            if(data.length != fromGUI.length)
                throw new IllegalArgumentException("Wrong byte array passed to fromGUI");
            else
                this.fromGUI = data;
        }
        else if(id.toLowerCase().equals("arduino"))
        {
            if(data.length != fromArduino.length)
                throw new IllegalArgumentException("Wrong byte array passed to fromArduino");
            else
                this.fromArduino = data;
        }
        
        notifyAll();
    }
    
    /**
     * Returns a byte array with requested data
     * @param id An identifier used to return correct byte array
     * @return The requested byte array
     */
    public synchronized byte[] getData(String id)
    {
        byte[] temp = new byte[6];
        
        if(id.toLowerCase().equals("gui"))
            temp = fromGUI;
        else if(id.toLowerCase().equals("arduino"))
            temp = fromArduino;
        
        notifyAll();
        
        return temp;
    }
}
