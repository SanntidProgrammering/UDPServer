/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Eivind Fugledal
 */
public class Main {
    
    protected static DataHandler dh;
    private static GUIData gd;
    private static Thread controller;
    private static Thread server;
    private static Semaphore semaphore;
    static SendEventState enumStateEvent;
    protected static String ipAdress;
    private static CameraCapture camera;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        semaphore = new Semaphore(1, true);
        
        dh = new DataHandler();
        dh.setThreadStatus(true);
        
        gd = new GUIData(dh, semaphore);
        controller = new Thread(new Controller(dh, semaphore));
        server = new Thread(new UDPServer(gd));
        
        gd.start();
        controller.start();
        server.start();
        
        camera = new CameraCapture();
        
        SerialComArduino sca = new SerialComArduino(dh);
        try {
            sca.connect("COM4", semaphore);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
