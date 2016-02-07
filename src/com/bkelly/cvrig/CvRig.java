package com.bkelly.cvrig;

public class CvRig {

	public static void main(String[] args) {
		String comPort = null;
		Integer angleOfView = null;
		if (args.length > 0) {
			comPort = args[0];
		}
		if (args.length > 1) {
			try {
				angleOfView = Integer.parseInt(args[1]);
			} catch ( NumberFormatException e) {
				printUsage();
				System.out.printf("Invalid angleOfView: %s, using default...\n", args[1]);
			}
		}
		
		DynamicCameraMotionMode camMode;
		if (comPort!=null && angleOfView != null) {
			camMode = new DynamicCameraMotionMode(comPort, angleOfView);
		} else if (comPort != null) {
			camMode = new DynamicCameraMotionMode(comPort);
		} else {
			System.out.println("No arguments provided, using default...");
			printUsage();
			camMode = new DynamicCameraMotionMode();
		}
				
		camMode.run();
	}
	
	public static void printUsage() {
		System.out.println("USAGE: java -jar CvRig.jar [com_port_string] [camera_angle_of_view]");
	}
	

}
