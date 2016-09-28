/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
import java.io.OutputStream;

public class SerialWriter implements Runnable {
    // stream to arduino

    OutputStream out;
    DataHandler datahandler;
    SerialComArduino serialCom;

    public SerialWriter(OutputStream out, DataHandler datahandler,SerialComArduino serialCom) {
        this.out = out;
        this.datahandler = datahandler;
        this.serialCom = serialCom;
    }

    public void run() {
        try {
             
            while (datahandler.shouldThreadRun()) {
                serialCom.setMessage();
                
                if (datahandler.isNewDataAvailable()) {
                     System.out.println("dette er til arduino");
                    this.out.write(datahandler.getData("gui"));
                     System.out.println(datahandler.getData("gui"));
                     
               }
           
            
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
