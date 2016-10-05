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

public class SerialComArduino
{
    Thread reader; // reads from arduino
    Thread writer;  // writes to arduino
    DataHandler datahandler;
    boolean available;

    
    public SerialComArduino(DataHandler datahandler)
    {
        super();
        this.datahandler = datahandler;
        available=false;
    }
    
    void connect (String portName, Semaphore semaphore) throws Exception
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
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                reader = new Thread(new SerialReader(in,datahandler,this));
                writer = new Thread(new SerialWriter(out,datahandler,this, semaphore));
                
                writer.start();
                reader.start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }

    public Thread getReader() {
        return reader;
    }

    public Thread getWriter() {
        return writer;
    }
    
    

}
    





