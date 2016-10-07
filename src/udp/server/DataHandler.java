/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Overview protocol: To Arduino: Byte 0: bit 0 - stopp bit 1 - fwd bit 2 - rev
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

    private byte[] dataFromArduino;
    private byte[] dataToArduino;
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
        this.dataToArduino = new byte[6];
        this.dataFromGui = new byte[6];
    }

    //*****************************************************************
    //********** PRIVATE METHODS AREA**********************************
    /**
     * Sets a specific bit in a specific byte to 1
     *
     * @param b The specific byte
     * @param bit The specific bit
     * @return Value of the bit
     */
    private byte setBit(byte b, int bit) {
        return b |= 1 << bit;
    }

    /**
     * Sets a specific bit in a specific byte to 1
     *
     * @param b The specific byte
     * @param bit The specific bit
     * @return Value of the bit
     */
    private byte releaseBit(byte b, int bit) {
        return b &= ~(1 << bit);
    }

    /**
     * Gets a specific bit in a specific byte
     *
     * @param b The specific byte
     * @param bit The specific bit
     * @return Value of the bit
     */
    private byte getBit(byte b, int bit) {
        return (byte) ((b >> bit) & 1);
    }

    //*****************************************************************
    //********************** THREAD STATUS METHODS*********************
    /**
     * Returns the threads status
     *
     * @return The threads status
     */
    public boolean shouldThreadRun() {
        return threadStatus;
    }

    /**
     * Sets the threads status
     *
     * @param threadStatus Thread status
     */
    public void setThreadStatus(boolean threadStatus) {
        this.threadStatus = threadStatus;
    }

    //*****************************************************************
    //*************** FROM ARDUINO METHODS*****************************
    public void handleDataFromArduino(byte[] data) {
        // check if the array is of the same length and the requestcode has changed
        if (data.length == this.dataFromArduino.length && data[ToArduino.commands.REQUEST_FEEDBACK.getValue()] != this.getRequestCodeFromArduino()) {
            this.dataFromArduino = data;
            this.setDistanceSensor(data[4]);
            this.setRequestCodeFromArduino(data[5]);
            this.setPixyXvalue(new BigInteger(Arrays.copyOfRange(data, 0, 2)).intValue());
            this.setPixyYvalue(new BigInteger(Arrays.copyOfRange(data, 2, 4)).intValue());
            this.dataFromArduinoAvaliable = true;
        }
    }

    /**
     *
     * @return true if new data available, false if not
     */
    public boolean isDataFromArduinoAvailable() {
        return this.dataFromArduinoAvaliable;
    }

    /**
     * Gets x-value from Pixy camera
     *
     * @return x-value
     */
    public int getPixyXvalue() {
        return pixyXvalue;
    }

    /**
     * Sets x-value from Pixy camera
     *
     * @param pixyXvalue x-value
     */
    public void setPixyXvalue(int pixyXvalue) {
        this.pixyXvalue = pixyXvalue;
    }

    /**
     * Gets y-value from Pixy camera
     *
     * @return y-value
     */
    public int getPixyYvalue() {
        return pixyYvalue;
    }

    /**
     * Sets y-value from Pixy camera
     *
     * @param pixyYvalue y-value
     */
    public void setPixyYvalue(int pixyYvalue) {
        this.pixyYvalue = pixyYvalue;
    }

    /**
     * Gets value from distance sensor
     *
     * @return Distance
     */
    public int getDistanceSensor() {
        return distanceSensor;
    }

    /**
     * Sets value from distance sensor
     *
     * @param distanceSensor Distance
     */
    public void setDistanceSensor(int distanceSensor) {
        this.distanceSensor = distanceSensor;
    }

    /**
     * Gets request code from Arduino
     *
     * @return Request code
     */
    public byte getRequestCodeFromArduino() {
        return requestCodeFromArduino;
    }

    /**
     * Sets request code from Arduino
     *
     * @param requestCodeFromArduino Request code
     */
    public void setRequestCodeFromArduino(byte requestCodeFromArduino) {
        this.requestCodeFromArduino = requestCodeFromArduino;
    }

    //****************************************************************
    //************** FROM GUI METHODS*********************************
    /**
     * Gets the byte array containing data from GUI
     *
     * @return The byte array
     */
    public byte[] getDataFromGui() {
        Main.enumStateEvent = SendEventState.FALSE;
        return this.dataFromGui;
    }

    /**
     * Sets the byte array containing data from GUI
     *
     * @param data New byte array
     */
    public void setDataFromGUI(byte[] data) {
        this.dataFromGui = data;
        this.fireStateChanged();
    }
    
    public byte getByte(byte b)
    {
        return dataFromGui[b];
    }

    /**
     * Sets stop bit to high
     */
    public void stopAUV() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.setBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.STOP.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets stop bit to low
     */
    public void releaseStopAUV() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.releaseBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.STOP.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets forward bit to high
     */
    public void goFwd() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.setBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.FORWARD.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets forward bit to low
     */
    public void releaseGoFwd() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.releaseBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.FORWARD.getValue());
        this.fireStateChanged();
    }

    /**
     * Gets value of forward bit
     *
     * @return Forward bit
     */
    public byte getFwd() {
        return this.getBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.FORWARD.getValue());
    }

    /**
     * Sets reverse bit to high
     */
    public void goRev() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.setBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.REVERSE.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets reverse bit to low
     */
    public void releaseGoRev() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.releaseBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.REVERSE.getValue());
        this.fireStateChanged();
    }

    /**
     * Gets value of reverse bit
     *
     * @return Reverse bit
     */
    public byte getRev() {
        return this.getBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.REVERSE.getValue());
    }

    /**
     * Sets left bit to high
     */
    public void goLeft() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.setBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.LEFT.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets left bit to low
     */
    public void releaseGoLeft() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.releaseBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.LEFT.getValue());
        this.fireStateChanged();
    }

    /**
     * Gets value of left bit
     *
     * @return Left bit
     */
    public byte getLeft() {
        return this.getBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.LEFT.getValue());
    }

    /**
     * Sets right bit to high
     */
    public void goRight() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.setBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.RIGHT.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets right bit to low
     */
    public void releaseGoRight() {
        dataToArduino[ToArduino.CONTROLS.getValue()] = this.releaseBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.RIGHT.getValue());
        this.fireStateChanged();
    }

    /**
     * Gets value of right bit
     *
     * @return Right bit
     */
    public byte getRight() {
        return this.getBit(dataFromGui[ToArduino.CONTROLS.getValue()], ToArduino.controls.RIGHT.getValue());
    }

    /**
     * Sets left motor speed
     *
     * @param speed Speed value between 0-255
     */
    public void setLeftMotorSpeed(byte speed) {
        dataToArduino[ToArduino.LEFT_MOTOR_SPEED.getValue()] = (byte) ((speed / 100) * this.getSensitivity());
        this.fireStateChanged();
    }
    
    public byte getLeftMotorSpeed()
    {
        return dataToArduino[ToArduino.LEFT_MOTOR_SPEED.getValue()];
    }

    /**
     * Sets right motor speed
     *
     * @param speed Speed value between 0-255
     */
    public void setRightMotorSpeed(byte speed) {
        dataToArduino[ToArduino.RIGHT_MOTOR_SPEED.getValue()] = (byte) ((speed / 100) * this.getSensitivity());
        this.fireStateChanged();
    }
    
    public byte getRightMotorSpeed()
    {
        return dataToArduino[ToArduino.RIGHT_MOTOR_SPEED.getValue()];
    }

    /**
     * Sets left servo bit to high
     */
    public void setLeftServo() {
        dataToArduino[ToArduino.COMMANDS.getValue()] = this.setBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.LEFT_SERVO.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets left servo bit to low
     */
    public void resetLeftServo() {
        dataToArduino[ToArduino.COMMANDS.getValue()] = this.releaseBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.LEFT_SERVO.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets right servo bit to high
     */
    public void setRightServo() {
        dataToArduino[ToArduino.COMMANDS.getValue()] = this.setBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.RIGHT_SERVO.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets right servo bit to low
     */
    public void resetRightServo() {
        dataToArduino[ToArduino.COMMANDS.getValue()] = this.releaseBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.RIGHT_SERVO.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets the auto/manual mode bit to low, which means that the vehicle is now
     * in manual mode
     */
    public void AUVmanualMode() {
        dataToArduino[ToArduino.COMMANDS.getValue()] = this.releaseBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.AUTO_MANUAL.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets the auto/manual mode bit to high, which means that the vehicle is
     * now in auto mode
     */
    public void AUVautoMode() {
        dataToArduino[ToArduino.COMMANDS.getValue()] = this.setBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.AUTO_MANUAL.getValue());
        this.fireStateChanged();
    }

    /**
     * Gets the auto/manual mode bit
     *
     * @return The auto/manual mode bit
     */
    public byte getAUVautoMode() {
        return this.getBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.AUTO_MANUAL.getValue());
    }

    /**
     * Sets the start bit to high
     */
    public void enableAUV() {
        dataToArduino[ToArduino.COMMANDS.getValue()] = this.setBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.START.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets the start bit to low
     */
    public void disableAUV() {
        dataToArduino[ToArduino.COMMANDS.getValue()] = this.releaseBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.START.getValue());
        this.fireStateChanged();
    }

    /**
     * Sets the value of sensitivity given from GUI (in percent)
     *
     * @param sensitivity Value between 0-100 percent
     */
    public void setSensitivity(byte sensitivity) {
        dataToArduino[ToArduino.SENSITIVITY.getValue()] = sensitivity;
        this.fireStateChanged();
    }

    /**
     * Gets the sensitivity value
     *
     * @return Sensitivity value, between 0-100
     */
    public int getSensitivity() {
        return dataFromGui[ToArduino.SENSITIVITY.getValue()] & 0xFF;
    }

    /**
     * Gets the request code
     *
     * @return The request code
     */
    public byte getRequestCode() {
        return this.getBit(dataFromGui[ToArduino.COMMANDS.getValue()], ToArduino.commands.REQUEST_FEEDBACK.getValue());
    }

    public void incrementRequestCode() {
        dataFromGui[5]++;
        this.fireStateChanged();
    }

    public void fireStateChanged() {
        Main.enumStateEvent = SendEventState.TRUE;
        //notifyAll();
    }

    public boolean checkSendDataAvailable() {
        return Main.enumStateEvent == SendEventState.TRUE;
    }
}
