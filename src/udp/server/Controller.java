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
public class Controller extends Thread {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        DataHandler dh = new DataHandler();
        
        while(true)
        {
            //dh.checkGUIData();
        }
    }
    
    @Override
    public void run()
    {
        
    }
}
