/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
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
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void udpSend(byte[] data) {
        if (hasReceivedSomething) {
            DatagramPacket sendpacket
                    = new DatagramPacket(data, data.length, guiIp, guiPort);
            try {
                serverSocket.send(sendpacket);
                System.out.println(Arrays.toString(data) + " TO GUI, with ip: " + guiIp.toString() + " on port: " + guiPort);
            } catch (IOException ex) {
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("NEI FAEN NO HAR DU KØDDA DET TIL");
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

            this.acquire();
            byte[] sendData = dh.getDataFromArduino();
            this.release();

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
