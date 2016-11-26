/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

/**
 * serialComArduino class
 * enables two-way communication between the application and a serialport
 * @author lars-harald
 */
public class SerialComArduino
{
    private Thread reader; // reads from arduino
    private Thread writer;  // writes to arduino
    private final DataHandler datahandler;
    
    /**
     * create a new SerialComArduino object
     * @param datahandler the shared resource
     */
    public SerialComArduino(DataHandler datahandler)
    {
        this.datahandler = datahandler;
    }

    /**
     * connect streams to a serialport and start the reader and writer
     * @param portName the name of the usb port
     * @param semaphore semaphore object 
     * @throws Exception 
     */
    public void connect (String portName, Semaphore semaphore) throws Exception
    {
        System.out.println("Connect");
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(19200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                serialPort.disableReceiveTimeout();
                serialPort.enableReceiveThreshold(6);
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                reader = new Thread(new SerialReader(in,datahandler, semaphore));
                writer = new Thread(new SerialWriter(out,datahandler, semaphore));
                
                writer.start();
                reader.start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }   
}
    





