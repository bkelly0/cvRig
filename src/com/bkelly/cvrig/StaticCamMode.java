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

public class StaticCamMode implements Runnable {
	public static final String SERIAL_PORT_STR = "COM5";
	
	public final static int MIN_AREA = 500;
	
	public final static int HORIZONTAL_FIELD_OF_VIEW = 40;
	public final static int VERTICAL_FIELD_OF_VIEW = 30;
	public final static int HORIZONTAL_START_DEGREE = (180 - HORIZONTAL_FIELD_OF_VIEW) /2;
	public final static int VERTICAL_START_DEGREE = (180 - VERTICAL_FIELD_OF_VIEW) /2;

	public final static int MOVEMENT_DELAY_TIME = 200;
	
	public DebugWindow debug;
	public DebugWindow normalDebug;
	
	public Scalar debugColor;
	private Point middlePoint;
	private ServoControl servoControl;
	
	public StaticCamMode() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		servoControl = new ServoControl(SERIAL_PORT_STR);		
		debugColor = new Scalar(0,0,255);	
	}
	
	@Override
	public void run() {
	
		Size blurSize = new Size(30, 30);
		double threshold = 34;
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

			if (camera.read(colorFrame)) {
				 Imgproc.cvtColor(colorFrame, bwFrame2, Imgproc.COLOR_BGR2GRAY);
			}
			Core.absdiff(bwFrame1, bwFrame2, vFrameBuffer);
			Imgproc.threshold(vFrameBuffer, thFrame, threshold ,255, Imgproc.THRESH_BINARY);
			/*
			Imgproc.adaptiveThreshold(vFrameBuffer, thFrame, 255,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY_INV, threshold, 10);
              */      
			Imgproc.blur(thFrame, vFrameBuffer, blurSize);
			Imgproc.threshold(vFrameBuffer,thFrame,threshold,255, Imgproc.THRESH_TOZERO);
			debug.update(thFrame);
			Rect rect = findObject(thFrame);
			if (rect != null) {
				middlePoint.x = rect.x + rect.width/2;
				middlePoint.y = rect.y + rect.height/2;
				Imgproc.circle(colorFrame, middlePoint, 5, debugColor);
				Imgproc.rectangle(colorFrame, rect.tl(), rect.br(), debugColor,1, 8,0);
				
				//x servo is backwards
				long degreeY = HORIZONTAL_START_DEGREE + Math.round((1-middlePoint.y/colorFrame.height())* HORIZONTAL_FIELD_OF_VIEW);
				long degreeX = VERTICAL_START_DEGREE + +Math.round((1-middlePoint.x/colorFrame.width()) * VERTICAL_FIELD_OF_VIEW);
				System.out.println(degreeX + "," + degreeY );
				
				//send to serial			
				try {
					servoControl.update(degreeX, degreeY);
					Thread.sleep(MOVEMENT_DELAY_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				normalDebug.update(colorFrame);
			}
		}	
	}
	

	private void setupDebug() {
		debug = new DebugWindow();
		normalDebug = new DebugWindow();
		
		Rectangle bounds = debug.getBounds();
		bounds.x += bounds.width;
		normalDebug.setBounds(bounds);
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
