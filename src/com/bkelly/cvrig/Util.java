package com.bkelly.cvrig;

public class Util {
	public static double pixelDegreeValue(int width, int height, int viewAngle) {
		return viewAngle / Math.sqrt(Math.pow(width,2) + Math.pow(height,2));
	}
}
