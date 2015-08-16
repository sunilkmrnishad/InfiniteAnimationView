package com.sun.infiniteanimationlib;
import android.graphics.Canvas;
import android.graphics.Paint;
public class Line{
	/*
	 * 
	 * */
	private float lineX;
	private float lineY;
	private float mWidth;
	private int lineNo;
	public Line(float x,float y, float width, int no) {
		lineX = x;
		lineY = y;
		mWidth = width;
		lineNo = no;
	}
	public int getLineNo() {
		return lineNo;
	}
	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}
	public float getLineX() {
		return lineX;
	}
	public void setLineX(float lineX) { 
		this.lineX = lineX;
	}
	public float getLineY() {
		return lineY;
	}
	public void setLineY(float lineY) {
		this.lineY = lineY;
	}
	public void resetLeft(float x) {
		lineX=x-mWidth;
	}
	public void resetRight(float x) {
		lineX=x+mWidth;
	}
	public void resetUp(float y) {
		lineY=y+mWidth;
	}
	public void resetDown(float y) {
		lineY=y-mWidth;
	}
}
