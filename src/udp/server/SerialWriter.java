/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore;

public class SerialWriter implements Runnable {
    // stream to arduino

    private OutputStream out;
    private DataHandler datahandler;
    private Semaphore semaphore;

    public SerialWriter(OutputStream out, DataHandler datahandler, Semaphore semaphore) {
        this.out = out;
        this.datahandler = datahandler;
        this.semaphore = semaphore;
    }

    public void run() {
        try {
            while (datahandler.shouldThreadRun()) {

                if (datahandler.checkSendDataAvailable()) {
                    acquire();
                    byte[] sendByte = datahandler.getDataFromController();
                    release();
                    System.out.println(Arrays.toString(sendByte) + "SERIAL");
                    this.out.write(sendByte);
                    this.out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void acquire() {
        try {
            semaphore.acquire();

        } catch (InterruptedException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void release() {
        semaphore.release();
    }
}
