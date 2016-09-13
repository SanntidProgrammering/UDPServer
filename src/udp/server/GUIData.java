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
    
    private final DataHandler dataHandler;
    
    private ByteArrayInputStream baos;
    private ObjectInputStream ois;
    
    public GUIData(DataHandler dh)
    {
        this.dataHandler = dh;
    }
    
    /**
     * @param data
     */
    public void receiveFromUDP(byte[] data)
    {
        if(!Arrays.equals(data, dataHandler.getData("gui")))
            this.setValuesToDataHandler(data);
    }
    
    public void setValuesToDataHandler(byte[] data)
    {
        dataHandler.setData(data, "gui");
    }
    
    public void checkValuesFromDataHandler()
    {
        
    }
    
    public void sendDataToUDP()
    {
        
    }
}
