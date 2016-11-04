/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp.server;

import java.awt.AWTException;
import java.awt.Graphics;
//import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;


/**
 *
 * @author mgrib
 */
public class ObjectTracker implements Runnable {
    
    private DataHandler dh;
    private Semaphore semaphore;
    private CameraCapture camCap;
    
    private VideoCapture capture;

    private Mat webcam_image;
    private Mat hsv_image; 
    private Mat thresholded;
    private Mat thresholded2;
    private double[] data;
    private Mat circles;
    private double[] hsv_values;
    
    private Scalar hsv_min;
    private Scalar hsv_max;
    
    private List<Mat> lhsv;
    private Mat array255;
    private Mat distance;
    
    private int hueMin = 23;//35;
    private int hueMax = 35;//74;
    private int satMin = 70;//93;
    private int satMax = 170;//223;
    private int valMin = 78;//74;
    private int valMax = 200;//14335;
    
    private double brightness;
    private double contrast;
    
    List<MatOfPoint> contours;
    
    public ObjectTracker(DataHandler dh, Semaphore semaphore, CameraCapture camCap){
        this.dh = dh;
        this.semaphore = semaphore;
        this.camCap = camCap;
        
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
        try{ 
            createMat();
            createFrames();
            setColorTrackingValues();
            //trackColors();
            
        }
        catch(AWTException e){
            e.printStackTrace();
        }
    }
    
    
    @Override
    public void run() {
        this.trackColors();
    }
    
    
    
    
    private void createFrames() throws AWTException {
                
                //capture = new VideoCapture(1); 

                //capture.set(3, 1280); //capture.set(3, 1366); // 500
		//capture.set(4, 720); //capture.set(4, 768); // 400
		//capture.set(15, -3);
		
                //capture.read(webcam_image);  
                webcam_image = this.camCap.getFrame();

		array255 = new Mat(webcam_image.height(),webcam_image.width(),CvType.CV_8UC1);  
		array255.setTo(new Scalar(255));  

                distance=new Mat(webcam_image.height(),webcam_image.width(),CvType.CV_8UC1);  
                lhsv = new ArrayList<>(3);      
		circles = new Mat();
    }

    private void trackColors() {
               
               while(true)  {
		    capture.read(webcam_image);  
	            if( !webcam_image.empty() ) { 
                                    
                                    
                        //Adjusting brightness and contrast
                        webcam_image.convertTo(webcam_image,-1, brightness, contrast);
                                     
                        //Adding blur to remove noise
                        //Imgproc.blur(webcam_image, webcam_image, new Size(7, 7));
                                    
                        // converting to HSV image
                        Imgproc.cvtColor(webcam_image, hsv_image, Imgproc.COLOR_BGR2HSV);  
                                    
                        //Checking if the hsv image is in range.
                        Core.inRange(hsv_image, hsv_min, hsv_max, thresholded);         
                                    
                        Imgproc.erode(thresholded, thresholded, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8,8)));
			Imgproc.dilate(thresholded, thresholded, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8)));
                        Core.split(hsv_image, lhsv); // We get 3 2D one channel Mats  
			Mat S = lhsv.get(1);  
			Mat V = lhsv.get(2);  
			Core.subtract(array255, S, S);  
			Core.subtract(array255, V, V);  
			S.convertTo(S, CvType.CV_32F);  
			V.convertTo(V, CvType.CV_32F);  
			Core.magnitude(S, V, distance);  
			Core.inRange(distance,new Scalar(0.0), new Scalar(200.0), thresholded2);  
                                        
                        Imgproc.GaussianBlur(thresholded, thresholded, new Size(9,9),0,0);  
			Imgproc.HoughCircles(thresholded, circles, Imgproc.CV_HOUGH_GRADIENT, 2, thresholded.height()/8, 200, 100, 0, 0);   
			Imgproc.findContours(thresholded, contours, thresholded2, Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
			Imgproc.drawContours(webcam_image, contours, -1, new Scalar(255, 0, 0), 2);   

                        Core.circle(webcam_image, new Point(210,210), 10, new Scalar(100,10,10),3);  
			data=webcam_image.get(210, 210);  
			Core.putText(webcam_image,String.format("("+String.valueOf(data[0])+","+String.valueOf(data[1])+","+String.valueOf(data[2])+")"),new Point(30, 30) , 3 //FONT_HERSHEY_SCRIPT_SIMPLEX  
				,1.0,new Scalar(100,10,10,255),3); 
                        //ArrayList<Float> errorAngles = new ArrayList<>();
                        ArrayList<Float> errorAngles = getTargetError();
                        
                        if(errorAngles != null){
                        
                          try {
                              semaphore.acquire();
                          } catch (InterruptedException ex) {
                              Logger.getLogger(ObjectTracker.class.getName()).log(Level.SEVERE, null, ex);
                          }
                          float eXa = (errorAngles.get(0));
                          float eYa = (errorAngles.get(1));
                          this.dh.setPixyXvalue( (int) eXa);
                          this.dh.setPixyYvalue( (int) eYa);
                          
                          System.out.print("AngleErrorX: "+errorAngles.get(0));
                          System.out.println("       AngleErrorY: "+ errorAngles.get(1));
                          
                          semaphore.release();
                        
                        }         
                        }
                                                
                    else{
				  
			System.out.println(" --(!) No captured frame -- Break!");  
			break;  
			}                                         
                                        
               
                                    
               }
    }


    private void createMat() {
        webcam_image = new Mat();
        hsv_image = new Mat();  
        thresholded =new Mat();  
        thresholded2 =new Mat(); 
        data = new double[3]; 
        circles = new Mat();
        
        lhsv = new ArrayList<>(3);  // new ArrayList<Mat>(3);  
        
        array255 = new Mat(webcam_image.height(),webcam_image.width(),CvType.CV_8UC1);  
	array255.setTo(new Scalar(255));  
        
        distance = new Mat(webcam_image.height(),webcam_image.width(),CvType.CV_8UC1);
        
       hsv_min = new Scalar(1,1,1);
       hsv_max = new Scalar(1,1,1);
        
       contours = new ArrayList<>(); // new ArrayList<MatOfPoint>();
    }



  

    private ArrayList<Float> getTargetError() {
        ArrayList<Float> angles = null;
        
        int x = getX(contours);
	int y = getY(contours);
        contours.clear();
                                                
        if(x>0){              
        angles = new ArrayList<>();                                        
        Core.circle(webcam_image, new Point(x, y), 4, new Scalar(50,49,0,255), 4);

        float centerX = webcam_image.width() / 2;
        float centerY = webcam_image.height() / 2;
                                                  
                                                  
        // CenterCirle
        Core.circle(webcam_image, new Point(centerX, centerY), 4, new Scalar(50,49,0,255), 4);
                                                  
        // Setup camera angles (from producer)
        float cameraAngleX = 60.0f; //70.42f;
        float cameraAngleY = 43.30f;
                                                  
        // Calculate difference from x and y to center
        float pixErrorX = x - centerX;
        float pixErrorY = -y + centerY;
                                                  
        // Calculate angle error in x and y direction
        float angleErrorX = (pixErrorX/centerX)*cameraAngleX;
        float angleErrorY = (pixErrorY/centerY)*cameraAngleY;
        Core.line(webcam_image, new Point(x,y), new Point(centerX,centerY), new Scalar(150,150,100)/*CV_BGR(100,10,10)*/, 3);  
        
        //angles = new float[]{angleErrorX, angleErrorY};
        angles.add(angleErrorX);
        angles.add(angleErrorY);

        }    
        return angles;
    }

    
    
    private void setColorTrackingValues() {
        double[] HsvMin = new double[]{this.hueMin, this.satMin, this.valMin}; //{35, 93, 74};
        hsv_min.set(HsvMin);
        double[] HsvMax = new double[]{this.hueMax, this.satMax, this.valMax}; //{74,223,14335};
        hsv_max.set(HsvMax);
        
        brightness = 1.0;
        contrast = 1.0;
    }
    
    
    
    public int getX(List<MatOfPoint> contours){
		List<Moments> mu = new ArrayList<>(contours.size());
		int x=0;
        for (int i = 0; i < contours.size(); i++) {
            mu.add(i, Imgproc.moments(contours.get(i), false));
            Moments p = mu.get(i);
            x = (int) (p.get_m10() / p.get_m00());
        }
        return x;
	}
        
	public int getY(List<MatOfPoint> contours){
		List<Moments> mu = new ArrayList<>(contours.size());
		int y=0;
        for (int i = 0; i < contours.size(); i++) {
            mu.add(i, Imgproc.moments(contours.get(i), false));
            Moments p = mu.get(i);
            y = (int) (p.get_m01() / p.get_m00());
        }
        return y;
	}

    
}
