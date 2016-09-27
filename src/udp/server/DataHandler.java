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
    
    private byte[] GUI;
    private byte[] Arduino;
    
    /**
     * 
     */
    public DataHandler()
    {
        GUI = new byte[6];
        Arduino = new byte[6];
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
            if(data.length != GUI.length)
                throw new IllegalArgumentException("Wrong byte array passed to fromGUI");
            else
                this.GUI = data;
        }
        else if(id.toLowerCase().equals("arduino"))
        {
            if(data.length != Arduino.length)
                throw new IllegalArgumentException("Wrong byte array passed to fromArduino");
            else
                this.Arduino = data;
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
            temp = GUI;
        else if(id.toLowerCase().equals("arduino"))
            temp = Arduino;
        
        notifyAll();
        
        return temp;
    }
    
    /**
     * Checks if program set to auto or manual mode
     * @return true if auto, false if manual
     */
    public synchronized boolean getAutoOrManual()
    {
        return this.getBitAsBool("gui", 3, 2);
    }
    
    /**
     * Returns the sensitivity value set in GUI
     * @return The sensitivity value
     */
    public synchronized int getSensitivity()
    {
        return this.getUnsignedByteValue("gui", 4);
    }
    
    /**
     * Set left motor to wanted speed
     * @param speed Wanted speed
     */
    public synchronized void setLeftMotorSpeed(int speed)
    {
        this.setByteValue("arduino", 1, (speed/100)*this.getSensitivity());
    }
    
    /**
     * Set right motor to wanted speed
     * @param speed Wanted speed
     */
    public synchronized void setRightMotorSpeed(int speed)
    {
        this.setByteValue("arduino", 2, (speed/100)*this.getSensitivity());
    }
    
    /**
     * Sets or unsets stop bit
     * @param stop true to set, false to unset
     */
    public synchronized void stop(boolean stop)
    {
        if(stop)
            this.setBit("arduino", 0, 0);
        else
            this.unSetBit("arduino", 0, 0);
    }
    
    /**
     * Sets or unsets run FWD bit
     * @param run true to set, false to unset
     */
    public synchronized void runFWD(boolean run)
    {
        if(run)
            this.setBit("arduino", 0, 1);
        else
            this.unSetBit("arduino", 0, 1);
    }
    
    /**
     * Sets or unsets run reverse bit
     * @param run true to set, false to unset
     */
    public synchronized void runRev(boolean run)
    {
        if(run)
            this.setBit("arduino", 0, 2);
        else
            this.unSetBit("arduino", 0, 2);
    }
    
    /**
     * Sets or unsets run left bit
     * @param run true to set, false to unset
     */
    public synchronized void runLeft(boolean run)
    {
        if(run)
            this.setBit("arduino", 0, 3);
        else
            this.unSetBit("arduino", 0, 3);
    }
    
    /**
     * Sets or unsets run right bit
     * @param run true to set, false to unset
     */
    public synchronized void runRight(boolean run)
    {
        if(run)
            this.setBit("arduino", 0, 4);
        else
            this.unSetBit("arduino", 0, 4);
    }
    
    /**
     * Sets the value of a specific byte
     * @param id A string id used to specify which data that contains the specific bit
     *           "gui" if data comes from GUI, "arduino" if data comes from Arduino
     * @param b The specific byte
     * @param value Value to be set to the byte
     */
    public synchronized void setByteValue(String id, int b, int value)
    {
        if(id.toLowerCase().equals("gui"))
            GUI[b] = (byte) value;
        else if(id.toLowerCase().equals("arduino"))
            Arduino[b] = (byte) value;
    }
    
    /**
     * Returns a unsigned integer with the value from a specific byte
     * @param id A string id used to specify which data that contains the specific bit
     *           "gui" if data comes from GUI, "arduino" if data comes from Arduino
     * @param b The specific byte
     * @return The unsigned integer value
     */
    public synchronized int getUnsignedByteValue(String id, int b)
    {
        int tempInt = 0;
        
        if(id.toLowerCase().equals("gui"))
            tempInt = GUI[b] & 0xFF;
        else if(id.toLowerCase().equals("arduino"))
            tempInt = Arduino[b] & 0xFF;
        
        return tempInt;
    }
    
    /**
     * Gets the value of a specific bit in a specific byte
     * @param id A string id used to specify which data that contains the specific bit
     *           "gui" if data comes from GUI, "arduino" if data comes from Arduino
     * @param b The specific byte
     * @param bit The specific bit
     * @return The requested bit
     */
    public synchronized byte getBit(String id, int b, int bit)
    {
        byte tempByte = 0;
        
        if(id.toLowerCase().equals("gui"))
            tempByte = GUI[b];
        else if(id.toLowerCase().equals("arduino"))
            tempByte = Arduino[b];
        
        return (byte) ((tempByte >> bit) & 1);
    }
    
    /**
     * Converts the value of a specific bit in a specific byte to a boolean value
     * @param id A string id used to specify which data that contains the specific bit
     *           "gui" if data comes from GUI, "arduino" if data comes from Arduino
     * @param b The specific byte
     * @param bit The specific bit
     * @return true if bit value equals 1, false if bit value equals 0
     */
    public synchronized boolean getBitAsBool(String id, int b, int bit)
    {
        boolean tempBool;
        
        tempBool = (this.getBit(id, b, bit) != 0);
        
        return tempBool;
    }
    
    /**
     * Sets the value of a specific bit in a specific byte
     * @param id A string id used to specify which data that contains the specific bit
     *           "gui" if data comes from GUI, "arduino" if data comes from Arduino
     * @param b The specific byte
     * @param bit The specific bit
     */
    public synchronized void setBit(String id, int b, int bit)
    {
        byte tempByte;
        
        if(id.toLowerCase().equals("gui"))
        {
            tempByte = GUI[b];
            tempByte = (byte) (tempByte | (1 << bit));
            GUI[b] = tempByte;
        }
        else if(id.toLowerCase().equals("arduino"))
        {
            tempByte = Arduino[b];
            tempByte = (byte) (tempByte | (1 << bit));
            Arduino[b] = tempByte;
        }
    }
    
    /**
     * Unsets the value of a specific bit in a specific byte
     * @param id A string id used to specify which data that contains the specific bit
     *           "gui" if data comes from GUI, "arduino" if data comes from Arduino
     * @param b The specific byte
     * @param bit The specific bit
     */
    public synchronized void unSetBit(String id, int b, int bit)
    {
        byte tempByte;
        
        if(id.toLowerCase().equals("gui"))
        {
            tempByte = GUI[b];
            tempByte = (byte) ((byte) tempByte & ~(1 << bit));
            GUI[b] = tempByte;
        }
        else if(id.toLowerCase().equals("arduino"))
        {
            tempByte = Arduino[b];
            tempByte = (byte) ((byte) tempByte & ~(1 << bit));
            Arduino[b] = tempByte;
        }
    }
}
