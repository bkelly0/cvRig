package com.bkelly.cvrig;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Grid {
	
	private List<List<Rectangle>> gridRects;
	private int gridSize;
	private int numCols;
	private int numRows;
	private int width;
	private int height;
	
	public Grid(int width, int height, int gridSize) {
		this.gridSize = gridSize;
		if (height % gridSize != 0) {
			System.out.println("Warning: grid height is not evenly divisible");
		}
		if (width % gridSize !=0) {
			System.out.println("Warning: grid width is not evenly divisible");
		}
		this.width = width;
		this.height = height;
		buildGridRects();
	}
	
	private void buildGridRects() {
		numRows = Math.round(height / gridSize);
		numCols = Math.round(width / gridSize);
		
		gridRects = new ArrayList<List<Rectangle>>(numCols);
		
		for (int row = 0; row < numRows; row++) {
			List<Rectangle> currentColumn = new ArrayList<Rectangle>(numRows);
			for (int col = 0; col < numCols; col++) {
				Rectangle rect = new Rectangle(col*gridSize, row*gridSize, gridSize, gridSize );
				currentColumn.add(rect);
			}
		}
	}
}
