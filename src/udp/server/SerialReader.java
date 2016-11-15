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
        while (dh.shouldThreadRun()) {
            try {
                byte[] data = new byte[6];
                in.read(data,0,data.length);
                System.out.println(Arrays.toString(data)+ "FROM SERIAL");
                
                semaphore.acquire();   
                dh.handleDataFromArduino(data);
                
            } catch (InterruptedException ex) {
                Logger.getLogger(SerialWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SerialReader.class.getName()).log(Level.SEVERE, null, ex);
            
            } finally {
                semaphore.release();
            }
        }
    }
}
