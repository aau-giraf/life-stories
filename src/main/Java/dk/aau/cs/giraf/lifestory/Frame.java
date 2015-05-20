package dk.aau.cs.giraf.lifestory;

import android.graphics.Point;

public class Frame {
	private int width;
	private int height;
	private Point position;
	
	public Frame(int width, int height, Point position) {
		setWidth(width);
		setHeight(height);
		setPosition(position);
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

}
