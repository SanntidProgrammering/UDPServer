/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eivind Fugledal
 */
public class UDPServer implements Runnable {
    
    private DatagramSocket serverSocket;
    private final int serverPort = 9876;
    
    private final GUIData guiData;
    
    public UDPServer (GUIData data)
    {
        guiData = data;
    }
    
    /**
     * Receives data from client, then sends it to the datahandler
     */
    @Override
    public void run()
    {
        try {
            serverSocket = new DatagramSocket(serverPort);
            
            byte[] receiveData = new byte[6];
            
            while(true)
            {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                
                Main.ipAdress = receivePacket.getAddress().getHostAddress();
                System.out.println("IP " + Main.ipAdress);
                
                System.out.println(Arrays.toString(receiveData) + " UDP");
                guiData.receiveFromUDP(receiveData);
            }
        } catch (SocketException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
