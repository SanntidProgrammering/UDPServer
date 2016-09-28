/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Overview protocol:
 * To Arduino:
 * Byte 0: bit 0 - stopp
 *         bit 1 - fwd
 *         bit 2 - rev
 *         bit 3 - left
 *         bit 4 - right
 * Byte 1: Left motor speed
 * Byte 2: Right motor speed
 * Byte 3: bit 0 - left servo
 *         bit 1 - right servo
 *         bit 2 - auto/manual
 *         bit 3 - start
 *         bit 7 - request feedback
 * Byte 4: Sensitivity
 * Byte 5: Reserved
 * 
 * From Arduino:
 * Byte 0: Pixy x value low byte
 * Byte 1: Pixy x value high byte
 * Byte 2: Pixy y value low byte
 * Byte 3: Pixy y value high byte
 * Byte 4: Distance sensor 4-30 cm
 * Byte 5: Reserved
 * 
 * @author Eivind Fugledal
 */
public class DataHandler {
    
    private byte[] dataFromArduino;
    private byte[] dataFromGui;
    private boolean dataFromArduinoAvaliable = false;
    private boolean dataFromGuiAvailable = false;
    private boolean threadStatus;
    private int pixyXvalue;
    private int pixyYvalue;
    private int distanceSensor;
    private byte requestCodeFromArduino;
    private boolean enableAUV;

    public DataHandler() {
        this.dataFromArduino = new byte[6];
        this.dataFromGui = new byte[6];
    }

    //*****************************************************************
    //********** PRIVATE METHODS AREA**********************************
    private byte setBit(byte b, int bit) {
        return b |= 1 << bit;
    }

    private byte releaseBit(byte b, int bit) {
        return b &= ~(1 << bit);
    }

    //*****************************************************************
    //********************** THREAD STATUS METHODS*********************
    public boolean shouldThreadRun() {
        return threadStatus;
    }

    public void setThreadStatus(boolean threadStatus) {
        this.threadStatus = threadStatus;
    }

    //*****************************************************************
    //*************** FROM ARDUINO METHODS*****************************
    public void handleDataFromArduino(byte[] data) {
        // check if the array is of the same length and the requestcode has changed
        if (data.length == this.dataFromArduino.length && data[5] != this.getRequestCodeFromArduino()) {
            this.dataFromArduino = data;
            this.setDistanceSensor(data[4]);
            this.setRequestCodeFromArduino(data[5]);
            this.setPixyXvalue(new BigInteger(Arrays.copyOfRange(data, 0, 2)).intValue());
            this.setPixyYvalue(new BigInteger(Arrays.copyOfRange(data, 2, 4)).intValue());
            this.dataFromArduinoAvaliable = true;
        }
    }

    public boolean isDataFromArduinoAvailable() {
        return this.dataFromArduinoAvaliable;
    }

    public int getPixyXvalue() {
        return pixyXvalue;
    }

    public void setPixyXvalue(int pixyXvalue) {
        this.pixyXvalue = pixyXvalue;
    }

    public int getPixyYvalue() {
        return pixyYvalue;
    }

    public void setPixyYvalue(int pixyYvalue) {
        this.pixyYvalue = pixyYvalue;
    }

    public int getDistanceSensor() {
        return distanceSensor;
    }

    public void setDistanceSensor(int distanceSensor) {
        this.distanceSensor = distanceSensor;
    }

    public byte getRequestCodeFromArduino() {
        return requestCodeFromArduino;
    }

    public void setRequestCodeFromArduino(byte requestCodeFromArduino) {
        this.requestCodeFromArduino = requestCodeFromArduino;
    }

    //****************************************************************
    //************** FROM GUI METHODS*********************************
    public byte[] getDataFromGui() {
        Main.enumStateEvent = SendEventState.FALSE;
        return this.dataFromGui;
    }
    
    public void setDataFromGUI(byte[] data) {
        this.dataFromGui = data;
        this.fireStateChanged();
    }

    public void stopAUV() {
        dataFromGui[0] = this.setBit(dataFromGui[0], 0);
        this.fireStateChanged();
    }

    public void releaseStopAUV() {
        dataFromGui[0] = this.releaseBit(dataFromGui[0], 0);
        this.fireStateChanged();
    }

    public void goFwd() {
        dataFromGui[0] = this.setBit(dataFromGui[0], 1);
        this.fireStateChanged();
    }

    public void releaseGoFwd() {
        dataFromGui[0] = this.releaseBit(dataFromGui[0], 1);
        this.fireStateChanged();
    }

    public void goRew() {
        dataFromGui[0] = this.setBit(dataFromGui[0], 2);
        this.fireStateChanged();
    }

    public void releaseGoRew() {
        dataFromGui[0] = this.releaseBit(dataFromGui[0], 2);
        this.fireStateChanged();
    }

    public void goLeft() {
        dataFromGui[0] = this.setBit(dataFromGui[0], 3);
        this.fireStateChanged();
    }

    public void releaseGoLeft() {
        dataFromGui[0] = this.releaseBit(dataFromGui[0], 3);
        this.fireStateChanged();
    }

    public void goRight() {
        dataFromGui[0] = this.setBit(dataFromGui[0], 4);
        this.fireStateChanged();
    }

    public void releaseGoRight() {
        dataFromGui[0] = this.releaseBit(dataFromGui[0], 4);
        this.fireStateChanged();
    }

    public void setLeftMotorSpeed(byte speed) {
        dataFromGui[1] = speed;
        this.fireStateChanged();
    }

    public void setRightMotorSpeed(byte speed) {
        dataFromGui[2] = speed;
        this.fireStateChanged();
    }

    public void setLeftServo() {
        dataFromGui[3] = this.setBit(dataFromGui[0], 0);
        this.fireStateChanged();
    }

    public void resetLeftServo() {
        dataFromGui[3] = this.releaseBit(dataFromGui[0], 0);
        this.fireStateChanged();
    }

    public void setRightServo() {
        dataFromGui[3] = this.setBit(dataFromGui[0], 1);
        this.fireStateChanged();
    }

    public void resetRightServo() {
        dataFromGui[3] = this.releaseBit(dataFromGui[0], 1);
        this.fireStateChanged();
    }

    public void AUVmanualMode() {
        dataFromGui[3] = this.releaseBit(dataFromGui[0], 2);
        this.fireStateChanged();
    }

    public void AUVautoMode() {
        dataFromGui[3] = this.setBit(dataFromGui[0], 2);
        this.fireStateChanged();
    }
    
    public byte getAUVautoMode() {
        return dataFromGui[3];
    }

    public void enableAUV() {
        dataFromGui[3] = this.setBit(dataFromGui[0], 3);
        this.fireStateChanged();
    }

    public void disableAUV() {
        dataFromGui[3] = this.releaseBit(dataFromGui[0], 3);
        this.fireStateChanged();
    }

    public void setSensitivity(byte sensetivity) {
        dataFromGui[4] = sensetivity;
        this.fireStateChanged();
    }

    public byte getSensitivity() {
        return dataFromGui[4];
    }

    public byte getRequestCode() {
        return dataFromGui[5];
    }

    public void incrementRequestCode() {
        dataFromGui[5]++;
        this.fireStateChanged();
    }

    public synchronized void fireStateChanged() {
        Main.enumStateEvent = SendEventState.TRUE;
        notifyAll();
    }

}
