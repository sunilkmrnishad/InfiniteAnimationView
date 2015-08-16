package com.sun.infiniteanimationlib;
import android.graphics.Canvas;
import android.graphics.Paint;
public interface VideoManager {
	/*
	 * 
	 * */

	public void render(Canvas canvas,Paint paint);
	public void reset(float x,float y);
	public void move();
}
