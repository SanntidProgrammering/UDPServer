/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eivind Fugledal
 */
public class GUIData extends Thread {
    private DataHandler dh;
    private Semaphore semaphore;
    
    
    public GUIData(DataHandler dh, Semaphore semaphore)
    {
        this.dh = dh;
        this.semaphore = semaphore;
    }
    
    
    @Override
    public void run()
    {
        
    }
    
    /**
     * Checks if data received from UDP have changed compared to the one stored 
     * in DataHandler.
     * @param data Data received from UDP
     */
    public void receiveFromUDP(byte[] data)
    {
        try {
            semaphore.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(GUIData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //if(!Arrays.equals(data, dh.getDataFromController())){
            System.out.println("Data mottatt til GUIData");
            dh.setDataFromGUI(data);
        //}
        
        semaphore.release();
    }
}
