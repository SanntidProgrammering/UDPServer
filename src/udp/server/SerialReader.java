/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;


public class SerialReader implements Runnable {
    // data from arduino

    InputStream in;
    DataHandler datahandler;

    public SerialReader(InputStream in, DataHandler datahandler) {
        this.in = in;
        this.datahandler = datahandler;
    }

    @Override
    public void run() {
        try {
            while (datahandler.shouldThreadRun()) {
                byte[] b = IOUtils.toByteArray(in);
                System.out.println(b.toString());
                datahandler.setData(b, "arduino");
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
