/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.io.IOException;
import java.net.*;
import java.util.logging.*;

/**
 *
 * @author Eivind Fugledal
 */
public class CameraSender {
    
    private DatagramSocket clientSocket;
    
    public CameraSender() {
           // nothing to do here
    }
    
    /*
    * init method
    */
    private void init(){
        try {
            clientSocket = new DatagramSocket();
        }  catch (SocketException ex) {
            Logger.getLogger(CameraSender.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /* 
    * send byte[] packet to socket 
    */
    public void send(String ipAddress, byte[] data, int port){
        this.init();
        //System.out.println(data.length);
        
         try {
            
            DatagramPacket packet = new DatagramPacket(data, 
                                        data.length, 
                                        InetAddress.getByName(ipAddress),
                                        port);
            clientSocket.send(packet);
             //System.out.println(Arrays.toString(data));
            //System.out.println("UDP send");
        } catch (IOException ex) {
            Logger.getLogger(CameraSender.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            clientSocket.close();
        }
    }
    
}
