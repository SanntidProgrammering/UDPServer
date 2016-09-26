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

    public SerialWriter(OutputStream out, DataHandler datahandler) {
        this.out = out;
        this.datahandler = datahandler;
    }

    public void run() {
        try {
            int c = 0;
            while (datahandler.shouldThreadRun()) {
                if (datahandler.isNewDataAvailable()) {
                    this.out.write(datahandler.getData("gui"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
