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
public class Controller implements Runnable {
    
    Thread t;
    
    private byte[] guiData;
    private byte[] arduinoData;
    
    public Controller()
    {
        guiData = new byte[6];
        arduinoData = new byte[6];
        
        this.start();
        
    }
    
    /**
     * Starts the control thread
     */
    public void start()
    {
        t = new Thread(this, "ControlThread");
        t.start();
    }

    @Override
    public void run() 
    {
        this.getData();
        
        while(!this.checkIfAuto())
        {
            
        }
    }
    
    /**
     * Collects data from datahandler
     */
    private void getData()
    {
        guiData = Main.dh.getData("gui");
        arduinoData = Main.dh.getData("arduino");
    }
    
    /**
     * Checks if GUI is set in auto or manual mode
     * @return true if auto, false if manual
     */
    private boolean checkIfAuto()
    {
        return this.getBitAsBool("gui", 3, 2);
    }
    
    /**
     * Gets the value of a specific bit in a specific byte
     * @param id A string id used to specify which data that contains the specific bit
     *           "gui" if data comes from GUI, "arduino" if data comes from Arduino
     * @param b The specific byte
     * @param bit The specific bit
     * @return The requested bit
     */
    private byte getBit(String id, int b, int bit)
    {
        byte tempByte = 0;
        
        if(id.toLowerCase().equals("gui"))
            tempByte = guiData[b];
        else if(id.toLowerCase().equals("arduino"))
            tempByte = arduinoData[b];
        
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
    private boolean getBitAsBool(String id, int b, int bit)
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
    private void setBit(String id, int b, int bit)
    {
        byte tempByte;
        
        if(id.toLowerCase().equals("gui"))
        {
            tempByte = guiData[b];
            tempByte = (byte) (tempByte | (1 << bit));
            guiData[b] = tempByte;
        }
        else if(id.toLowerCase().equals("arduino"))
        {
            tempByte = arduinoData[b];
            tempByte = (byte) (tempByte | (1 << bit));
            arduinoData[b] = tempByte;
        }
    }
    
    /**
     * 
     */
    private void stop()
    {
        this.setBit("arduino", 0, 0);
    }
    
    /**
     * 
     */
    private void runFWD()
    {
        this.setBit("arduino", 0, 1);
    }
    
    /**
     * 
     */
    private void runRev()
    {
        this.setBit("arduino", 0, 2);
    }
    
    /**
     * 
     */
    private void runLeft()
    {
        this.setBit("arduino", 0, 3);
    }
    
    /**
     * 
     */
    private void runRight()
    {
        this.setBit("arduino", 0, 4);
    }
}
