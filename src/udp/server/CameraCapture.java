/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Eivind Fugledal
 */
public class CameraCapture extends Thread {
    
    private VideoCapture capture;
    private Mat frame;
    private MatOfByte mob;
    private BufferedImage buff;
    private CameraSender cameraSender;
    
    public CameraCapture()
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture(-1);
        frame = new Mat();
        mob = new MatOfByte();
        cameraSender = new CameraSender();
    }
    
    @Override
    public void run()
    {
        this.capture();
    }
    
    private void capture()
    {   
        while(true)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            buff = this.getBufferedImage();
            
            if(buff != null)
            {
                try {
                    buff = this.scale(buff, buff.getType(), buff.getWidth(), buff.getHeight(), 0.25, 0.25);
            
                    ImageIO.write(buff, "bmp", baos);
                    baos.flush();
                    byte[] imageInByte = baos.toByteArray();
                    baos.close();
            
                    cameraSender.send(Main.ipAdress, imageInByte, 8765);
                
                    Thread.sleep(5);
            
                } catch (IOException ex) {
                    Logger.getLogger(CameraCapture.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CameraCapture.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private BufferedImage getBufferedImage()
    {
        BufferedImage returnImage;
        returnImage = null;
        if(capture.grab())
        {
            try {
                capture.retrieve(frame);
                this.setFrame(frame);
               	Highgui.imencode(".bmp", frame, mob);
		Image im;
                im = ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	       	BufferedImage buff = (BufferedImage) im;
                returnImage = buff;
            } 
            catch (IOException ex) {
                //Logger.getLogger(DaemonThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnImage;
    }
    
    private BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) 
    {
        BufferedImage dbi = null;
        if(sbi != null) {
            //System.out.println(dWidth*fWidth);
            dbi = new BufferedImage((int) (dWidth*fWidth), (int) (dHeight*fHeight), imageType);
            Graphics2D g = dbi.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
            g.drawRenderedImage(sbi, at);
        }
        return dbi;
    }
    
    private void setFrame(Mat frame)
    {
        this.frame = frame;
        //this.frame = frame.clone();
    }
    
    public Mat getFrame()
    {
        return this.frame;
    }

    
}
