/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eivind Fugledal
 */
public class UDPServer implements Runnable {

    private DatagramSocket serverSocket;
    private InetAddress guiIp;
    private int guiPort;
    private final int serverPort = 9876;

    private boolean hasReceivedSomething = false;
    private byte lastRequestCodeFromGui;

    private final Semaphore semaphore;
    private final DataHandler dh;

    public UDPServer(Semaphore semaphore, DataHandler dh) {
        this.semaphore = semaphore;
        this.dh = dh;
    }

    /**
     * Receives data from client, then sends it to the data handler
     *
     */
    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(serverPort);

            byte[] receiveData = new byte[6];

            while (dh.shouldThreadRun()) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                guiIp = receivePacket.getAddress();
                guiPort = receivePacket.getPort();

                System.out.println(Arrays.toString(receiveData) + " FROM GUI, with ip: " + guiIp.toString() + " on port: " + guiPort);

                this.setDataToDatahandler(receiveData);
                hasReceivedSomething = true;

                this.checkForSendingToGUI();
            }
        } catch (SocketException ex) {
            System.out.println("Exception-.-.-.-.-.-.-.-.-.-.-.-");
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);

        } catch (IOException ex) {
            System.out.println("Exception-.-.-.-.-.-.-.-.-.-.-.-");
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void udpSend(byte[] data) {
        if (hasReceivedSomething) {
            DatagramPacket sendpacket
                    = new DatagramPacket(data, data.length, guiIp, 9877);
            try {
                serverSocket.send(sendpacket);
                System.out.println(Arrays.toString(data) + " TO GUI, with ip: " + guiIp.toString() + " on port: " + guiPort);
            } catch (IOException ex) {
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("UDP is not ready. missing IP address for GUI");
        }
    }

    /**
     * Checks if data received from UDP have changed compared to the one stored
     * in DataHandler.
     *
     * @param data Data received from UDP
     */
    public void setDataToDatahandler(byte[] data) {
        this.acquire();

        dh.setDataFromGUI(data);

        this.release();
    }

    private void checkForSendingToGUI() {
        byte requestCode;

        this.acquire();
        requestCode = dh.getRequestCode();
        this.release();

        if (requestCode != this.lastRequestCodeFromGui) {

            byte[] sendData = new byte[6];

            this.acquire();

            int xAngle = (int) (100.0f * dh.getPixyXvalue());
            int yAngle = (int) (100.0f * dh.getPixyYvalue());
            byte distanceSensor = (byte) dh.getDistanceSensor();

            this.release();

            byte[] x = new byte[2];
             x = BigInteger.valueOf(xAngle).toByteArray();
            byte[] y = new byte[2];
            y = BigInteger.valueOf(yAngle).toByteArray();
            System.out.println("x: " + Arrays.toString(x));
            sendData[0] = x[0];
            sendData[1] = x[1];
            sendData[2] = y[0];
            sendData[3] = y[1];
            sendData[4] = distanceSensor;
            sendData[5] = 0;

            this.udpSend(sendData);

            this.lastRequestCodeFromGui = requestCode;
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
