/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 *
 * @author Eivind
 */
public class GUIData {
    
    private ByteArrayInputStream baos;
    private ObjectInputStream ois;
    
    private ArrayList<Boolean> list;
    
    public GUIData()
    {
        
    }
    
    /**
     * Checks for changes in controls
     * @param data
     * @throws java.lang.Exception
     */
    public void processData(byte[] data) throws Exception
    {
        baos = new ByteArrayInputStream(data);
        ois = new ObjectInputStream(baos);
            
        list = (ArrayList<Boolean>) ois.readObject(); 
    }
}
