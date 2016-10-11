/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SerialReader implements Runnable {
    // data from arduino

    private final InputStream in;
    private final ByteArrayOutputStream bos;
    private final DataHandler dh;
    private final Semaphore semaphore;

    public SerialReader(InputStream in, DataHandler datahandler, Semaphore semaphore) {
        this.in = in;
        this.dh = datahandler;
        this.semaphore = semaphore;
        this.bos = new ByteArrayOutputStream(6);
    }

    @Override
    public void run() {
        int nRead;
        byte[] data = new byte[6];
        try {
            while (dh.shouldThreadRun()) {
                while((nRead = in.read(data, 0 , data.length)) != -1){
                    bos.write(data, 0, nRead);
                }
                bos.flush();
                data = bos.toByteArray();
                try {
                    
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
