/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialReader implements Runnable {
    // data from arduino

    InputStream in;
    DataHandler datahandler;
    SerialComArduino serialCom;

    public SerialReader(InputStream in, DataHandler datahandler, SerialComArduino serialCom) {
        this.in = in;
        this.datahandler = datahandler;
        this.serialCom = serialCom;
    }

    @Override
    public void run() {
        try {
            while (datahandler.shouldThreadRun()) {
               // serialCom.getMessage();

                int i = in.available();
                byte[] readBuffer = new byte[i];
                in.read(readBuffer, 0, i);
                 //System.out.println("dette er fra arduino");
                //System.out.println(Arrays.toString(readBuffer));
              
                
                
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
