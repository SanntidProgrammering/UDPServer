/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 *
 * @author Eivind Fugledal
 */
public class GUIData extends Thread {
    
    private ByteArrayInputStream baos;
    private ObjectInputStream ois;
    
    public GUIData()
    {
    }
    
    /**
     * Checks if data received from UDP have changed compared to the one stored 
     * in DataHandler.
     * @param data Data received from UDP
     */
    public void receiveFromUDP(byte[] data)
    {
        if(!Arrays.equals(data, Main.dh.getData("gui")))
            this.setValuesToDataHandler(data);
    }
    
    /**
     * Updates data stored in DataHandler
     * @param data Updated data
     */
    public void setValuesToDataHandler(byte[] data)
    {
        Main.dh.setData(data, "gui");
    }
    
    /**
     * 
     */
    public void checkValuesFromDataHandler()
    {
        
    }
    
    /**
     * 
     */
    public void sendDataToUDP()
    {
        
    }
}
