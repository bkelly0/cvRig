package com.bkelly.cvrig;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class DynamicCameraMotionMode implements Runnable {
	
	public final static int MIN_AREA = 500;
	public final static int MOVEMENT_DELAY_TIME = 500;
	public final static int THRESHOLD = 34;
	
	private Point middlePoint;
	private ServoControl servoControl;
	private double pixelDegree;
	private Point captureCenter;
	public DebugWindow debug1;
	public DebugWindow debug2;
	public Scalar debugColor;
	public int angleOfView;

	public DynamicCameraMotionMode() {
		this(60, "COM5");
	}
	
	public DynamicCameraMotionMode(int angleOfView, String serialPortStr) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.angleOfView = angleOfView;
		servoControl = new ServoControl(serialPortStr);
		debugColor = new Scalar(0,0,255);
		pixelDegree = -1;
	}
	
	@Override
	public void run() {
		Size blurSize = new Size(30, 30);
		VideoCapture camera = new VideoCapture(0);
		Mat vFrameBuffer = new Mat();
		Mat colorFrame = new Mat();
		Mat bwFrame1 = new Mat();
		Mat bwFrame2 = new Mat();
		Mat thFrame = new Mat();
		middlePoint = new Point();
		
		setupDebug();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        servoControl.disconnect();
		    }
		});
		
		while(true) {
			
			if (camera.read(vFrameBuffer)) {
				 Imgproc.cvtColor(vFrameBuffer, bwFrame1, Imgproc.COLOR_BGR2GRAY);
			}
			
			if (pixelDegree < 0) {
				pixelDegree = Util.pixelDegreeValue(vFrameBuffer.width(), vFrameBuffer.height(), angleOfView);
				captureCenter = new Point(vFrameBuffer.width()/2, vFrameBuffer.height()/2);
				System.out.println("pixelDegree: " + pixelDegree);
			}

			if (camera.read(colorFrame)) {
				 Imgproc.cvtColor(colorFrame, bwFrame2, Imgproc.COLOR_BGR2GRAY);
			}
			Core.absdiff(bwFrame1, bwFrame2, vFrameBuffer);
			Imgproc.threshold(vFrameBuffer, thFrame, THRESHOLD, 255, Imgproc.THRESH_BINARY);
     
			Imgproc.blur(thFrame, vFrameBuffer, blurSize);
			Imgproc.threshold(vFrameBuffer,thFrame, THRESHOLD, 255, Imgproc.THRESH_TOZERO);
			debug1.update(thFrame);
			
			Rect rect = findObject(thFrame);
			if (rect != null && !(rect.width == vFrameBuffer.width() && rect.height == vFrameBuffer.height())) {
				middlePoint.x = rect.x + rect.width/2;
				middlePoint.y = rect.y + rect.height/2;
				Imgproc.circle(colorFrame, middlePoint, 5, debugColor);
				Imgproc.rectangle(colorFrame, rect.tl(), rect.br(), debugColor,1, 8,0);
				
				//x servo is backwards
				long degreeX = Math.round(pixelDegree * (captureCenter.x - middlePoint.x));
				long degreeY = Math.round(pixelDegree * (captureCenter.y - middlePoint.y));
				
				System.out.println(degreeX + "," + degreeY );
				
				//send to serial			
				try {
					//Thread.sleep(4000);
					servoControl.update(degreeX, degreeY);
					Thread.sleep(MOVEMENT_DELAY_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				debug2.update(colorFrame);
			}
		}	
	}
	

	private void setupDebug() {
		debug1 = new DebugWindow();
		debug2 = new DebugWindow();
		
		Rectangle bounds = debug1.getBounds();
		bounds.x += bounds.width;
		debug2.setBounds(bounds);
	}
	
	private Rect findObject(Mat m) {
		List<MatOfPoint> list = new ArrayList<MatOfPoint>();
		Mat temp  = new Mat();
		Imgproc.findContours(m, list, temp, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		if (list.size() >0) {
			int largestIndex = 0;
			int currIndex = 0;
			double largestArea = 0;
			double currArea;
			
			for (MatOfPoint mp : list) {
				currArea = Imgproc.contourArea(mp);
				if (currArea > MIN_AREA && currArea > largestArea) {
					largestArea = currArea;
					largestIndex = currIndex;
				}
				currIndex++;
			}
			Imgproc.drawContours(m, list, largestIndex, debugColor, 4);
			//debug.update(m);
			return Imgproc.boundingRect(list.get(largestIndex));
		}
		return null;
	
	}
}
