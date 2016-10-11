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
import org.apache.commons.io.IOUtils;

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
                try {
                    byte[] data = IOUtils.toByteArray(in);
                    
                    semaphore.acquire();
                    dh.handleDataFromArduino(data);
                    System.out.println("semaphore aqured and sending data to datahandler from arduino");
                    System.out.println(Arrays.toString(data));
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
