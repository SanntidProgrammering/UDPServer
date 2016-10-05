/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialReader implements Runnable {
    // data from arduino

    private final InputStream in;
    private final DataHandler dh;
    private final Semaphore semaphore;

    public SerialReader(InputStream in, DataHandler datahandler, Semaphore semaphore) {
        this.in = in;
        this.dh = datahandler;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            while (dh.shouldThreadRun()) {
                int i = in.available();
                byte[] readBuffer = new byte[i];
                in.read(readBuffer, 0, i);
                
                try {
                    semaphore.acquire();
                    dh.handleDataFromArduino(readBuffer);
                    System.out.println("semaphore aqured and sending data to datahandler from arduino");
                    System.out.println(Arrays.toString(readBuffer));
                } catch (InterruptedException ex) {
                    Logger.getLogger(SerialWriter.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    semaphore.release();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SerialReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
