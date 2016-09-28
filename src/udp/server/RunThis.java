/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author odroid
 */
public class RunThis {

    public static void main(String[] args) {
        try {
            DataHandler dh = new DataHandler();
            byte[] b = {22,23,24,25,26,27};
            dh.setData(b, "gui"); 
            SerialComArduino sca = new SerialComArduino(dh);
            sca.connect("/dev/ttyACM0");
            //sca.connect("/dev/ttyUSB0");
            //System.out.println(b);
            
           
            
        } catch (Exception ex) {
            Logger.getLogger(RunThis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
