/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

/**
 *
 * @author Eivind Fugledal
 */
public class Main {
    
    protected static DataHandler dh;
    private static GUIData gd;
    private static ArduinoData ad;
    private static Controller controller;
    static SendEventState enumStateEvent;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        dh = new DataHandler();
        gd = new GUIData();
        ad = new ArduinoData();
        controller = new Controller();
        
        gd.start();
        ad.start();
    }
    
}
