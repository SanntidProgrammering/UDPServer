/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialWriter implements Runnable {
    // stream to arduino

    OutputStream out;
    DataHandler datahandler;
    SerialComArduino serialCom;

    public SerialWriter(OutputStream out, DataHandler datahandler, SerialComArduino serialCom) {
        this.out = out;
        this.datahandler = datahandler;
        this.serialCom = serialCom;
    }

    public void run() {
        try {

            while (datahandler.shouldThreadRun()) {
                this.checkSendDataAvailable();
 
                this.out.write(datahandler.getDataFromGui());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void checkSendDataAvailable() {
        while (RunThis.enumStateEvent == SendEventState.FALSE) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(SerialReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        notifyAll();
    }
}
