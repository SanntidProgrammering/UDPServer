/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

/**
 * Overview protocols: To Arduino: Byte 0: bit 0 - stopp bit 1 - fwd bit 2 - rev
 * bit 3 - left bit 4 - right Byte 1: Left motor speed Byte 2: Right motor speed
 * Byte 3: bit 0 - left servo bit 1 - right servo bit 2 - auto/manual bit 3 -
 * start bit 7 - request feedback Byte 4: Sensitivity Byte 5: Reserved
 *
 * From Arduino: Byte 0: Pixy x value low byte Byte 1: Pixy x value high byte
 * Byte 2: Pixy y value low byte Byte 3: Pixy y value high byte Byte 4: Distance
 * sensor 4-30 cm Byte 5: Reserved
 *
 * @author Eivind Fugledal
 */
public class DataHandler {

    private final GUIData gui;
    private final ArduinoData arduino;

    private byte[] toArduino;
    private byte[] fromArduino;

    private boolean dataflag;
    private boolean keepAliveThreads = true;

    /**
     *
     */
    public DataHandler() {
        gui = new GUIData(this);
        arduino = new ArduinoData();

        toArduino = new byte[6];
        fromArduino = new byte[6];

        // Must be the last call in the constructor
        this.startThreads();
    }

    /**
     * Updates byte arrays when receiving from either GUI or Arduino
     *
     * @param data New data set from GUI/Arduino
     * @param id An identifier used to set correct byte array
     */
    public synchronized void setData(byte[] data, String id) {
        if (id.toLowerCase().equals("gui")) {
            this.toArduino = data;
            this.setDataAvailable(true);
        }
        if (id.toLowerCase().equals("arduino")) {
            this.fromArduino = data;
        }

        notifyAll();
    }

    /**
     * Returns a byte array with requested data
     *
     * @param id An identifier used to return correct byte array
     * @return The requested byte array
     */
    public synchronized byte[] getData(String id) {
        byte[] temp = new byte[6];

        if (id.toLowerCase().equals("gui")) {
            temp = toArduino;
            this.set
        }
        if (id.toLowerCase().equals("arduino")) {
            temp = fromArduino;
            this.dataflag = false;
        }

        notifyAll();

        return temp;
    }

    /**
     * Starts thred for GUI receiving/sending and Arduino receiving/sending
     */
    private void startThreads() {
        gui.start();
        arduino.start();
    }

    public boolean isNewDataAvailable() {
        return dataflag;
    }

    public synchronized void setDataAvailable(boolean bool) {
        this.dataflag = bool;
        notifyAll();
    }

    public void killAllThreads() {
        this.keepAliveThreads = false;
    }

    public boolean shouldThreadRun() {
        return this.keepAliveThreads;
    }

}
