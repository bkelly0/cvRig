package com.bkelly.cvrig;

import org.opencv.core.Point;

public class ServoControl {
	
	private byte[] values = {0,0};
	private long currentX;
	private long currentY;
	
	
	private Serial serial;
	
	public ServoControl(String portName) {
		currentX = 0;
		currentY = 0;
		serial = new Serial();
		serial.connect(portName);
	}
	
	public void update(long x, long y) {
		currentX = currentX + x;
		currentY = currentY + y;
		
		if (currentY > 120) {
			currentY = 120;
			System.out.println("exceeds max y");
		} else if (currentY < 60) {
			currentY = 60;
			System.out.println("exceeds min y");
		}
		if (currentX > 180) {
			currentX = 180;
		} else if (currentX < 0) {
			currentX = 0;
		}
		
		values[0] = (byte)currentX;
		values[1] = (byte)currentY;

		//TODO: some validation here
		if (serial.isConnected()) {
			serial.write(values);
		}
	}
	
	public void disconnect() {
		System.out.println("disconnecting serial");
		serial.disconnect();
	}
}
