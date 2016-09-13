/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.net.*;

/**
 *
 * @author Eivind Fugledal
 */
public class UDPServer {
    
    private DatagramSocket serverSocket;
    private final int serverPort = 9876;
    
    private final GUIData guiData;
    
    public UDPServer (GUIData data) throws Exception
    {
        guiData = data;
        this.run();
    }
    
    /**
     * Receives data from client, then sends it to the data handler
     * @throws Exception 
     */
    private void run() throws Exception
    {
        serverSocket = new DatagramSocket(serverPort);
        
        byte[] receiveData = new byte[6];
        byte[] sendData = new byte[6];
        
        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            
            guiData.receiveFromUDP(receiveData);
        }
    }
}
