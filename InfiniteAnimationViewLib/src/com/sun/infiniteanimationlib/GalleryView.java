package com.sun.infiniteanimationlib;
import java.util.ArrayList;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.WindowManager;

import com.sun.infiniteanimationlib.Cell.VideoClickListener;
import com.sun.infiniteanimationlib.LocalImage.ImageNotifier;
import com.sun.infiniteanimationviewlib.R;
public class GalleryView extends SurfaceView implements SurfaceHolder.Callback,ImageNotifier,SensorEventListener{
	/*
	 * 
	 * */
	private Cell moveCell;
	private Line left,right,tempLeft,tempRight;
	private int leftLineNo,rightLineNo;
	private SurfaceHolder holder;
	private ArrayList<Cell> videoList;
	
	private LinkedList<Line> cols;
	private LinkedList<Line> rows;
	
	private float screenWidth,screenHeight;
	private float sensorX,sensorY;
	private float imgw,imgh;
	private float eventX,eventY,downX;
	
	private Rect viewBoundaryRect;
	private Paint paint;
	
	private CustomImageLoader imageLoader;
	private Context mContext;
	private boolean isAutoMove=true;
	
	private AnimationThread galleryThread;
	
	protected VelocityTracker mVelocity;
	protected VelocityDecelerator mVelocityDecelerator;
	private VideoClickListener activityListener;
	
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	
	private int cellID;
	private ArrayList<LoadedImage> imageUri;

	
	public GalleryView(Context context,boolean loadLocalData,boolean isSensor)  {
		super(context);
		//activity listener
		activityListener=(VideoClickListener) context;
		//Application context
		mContext = context;
		
		//view holder
		holder = getHolder();
		
		//Universal image objects
//		imageLoader=new CustomImageLoader(context);
		//initial setup of matrix
		init(((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay());
		//paint setup for draw video rectangle
		paint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
		paint.setColor(Color.WHITE);
		paint.setTextSize(getActualDimensionsX(getRatioX(10)));
		//set listener for SurfaceHolder.Callback
		holder.addCallback(this);
		
		if (loadLocalData) {
			imageUri=new  ArrayList<LoadedImage>();
			new LocalImage(mContext,this);
		}
		
		if (isSensor) {
			senSensorManager=(SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
			senAccelerometer=senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
	}
	public float getRatioX(float value) {
		//for calculate font size in %
		return ((float) (value * 100) / 320) / 100;
	}
	private float getActualDimensionsX(float ratio) {
		//for calculate font size according to screen width
		return ratio * screenWidth;
	}
	private void init(Display display) {
		//getting width and height of screen 
		screenWidth=display.getWidth();
		screenHeight=display.getHeight();
		//slice screen width in 3 parts
		imgw=imgh=screenWidth/3;
		imgw=imgw+(imgw/3);
		//Default image of video 
		Bitmap defoultVideoImage=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
		//title image 
		Bitmap videoTitleBG=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
		int counter=0;
		float tempY=0;
		//slice screen height
		while (tempY<screenHeight) {
			tempY=tempY+imgh;
			counter++;
		}
		//number of rows of matrix
		int row=counter+4;
		//number of cols of matrix
		int col=7;
		videoList=new ArrayList<Cell>();
		cols=new LinkedList<Line>();
		rows=new LinkedList<Line>();
		//starting point of lines
		float xx=-(imgh/2+imgh);
		//video boundary for recycle video list 
		viewBoundaryRect=new  Rect(-Math.round(imgh*3), -Math.round(imgh*3), Math.round(screenWidth+imgh*3), Math.round(screenHeight+imgw*2));
		//maintaining starting point of x and y 
		float yy=xx;
		tempY=xx;

		//creating line for cols 
		for (int index1 = 0; index1 < col; index1++) {
			cols.add(new Line(xx, yy,imgw,index1));  
			xx=xx+imgw;
		}
		//creating line for rows 
		xx=yy=tempY;
		for (int index = 0; index < row; index++) {
			rows.add(new Line(xx, yy,imgh,index)); 
			yy=yy+imgh;
		}
		//creating matrix of video files by using cols and rows lines
		for (int index = 0; index < rows.size(); index++) {
			for (int index1 = 0; index1 < cols.size(); index1++) { 
				
				videoList.add(new Cell(this,cols.get(index1),rows.get(index),imgw,imgh,defoultVideoImage,videoTitleBG));
			}
		}
	} 

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		System.out.println("GalleryView.surfaceChanged()");
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		moveCell=videoList.get(2);
		//initialize thread
				galleryThread = new AnimationThread(getHolder(), this);
				galleryThread.setThreadState(true);
				galleryThread.setRunning(true);
				galleryThread.start();
				
				if (senSensorManager!=null) {
					senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
				}
	}

	private void drawGallary(Canvas canvas) {
		//draw all object on their respected coordinates
		if(canvas==null)
			return;
		
		canvas.drawRGB(0, 0, 0);
		
		for(Cell video:videoList){
			video.render(canvas, paint);
			if (!video.isImageLoaded) {
				if (cellID>videoList.size()) {
					cellID=0;
				}
				video.setVideo((Activity) mContext, imageUri.get(cellID));
				cellID++;
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		galleryThread.setThreadState(false);
		while(retry) {
			try {
				galleryThread.join();
				retry = false;
			} catch(InterruptedException ie) {
				//Try again and again and again
			}
			break;
		}
		galleryThread = null;
		
		if (senSensorManager!=null) {
			senSensorManager.unregisterListener(this);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				downX=eventX=event.getX();
				eventY=event.getY();
				for(Cell cell:videoList){
					if (cell.isContain(event.getX(),event.getY())) {
						moveCell=cell;
					}
				}
				//pause thread
				galleryThread.setRunning(false);
				//getting velocity for handling scrolling on down
				if (mVelocityDecelerator != null) mVelocityDecelerator.stop();
				mVelocity = VelocityTracker.obtain();
				mVelocity.addMovement(event);
				break;
	
			case MotionEvent.ACTION_MOVE:
				//getting velocity for handling scrolling on move
				mVelocity.addMovement(event);
				//dragged of all object
				moveVideoMatrix(moveCell.getmX().getLineX()+(event.getX()-eventX),moveCell.getmY().getLineY()+(event.getY()-eventY));
				eventX=event.getX();
				eventY=event.getY();
				//draw
				setCanvasForDraw();
				//disable auto movement of view
				isAutoMove=false;
				break;
			case MotionEvent.ACTION_UP:
//				moveCell.onUp(event.getX(),event.getY());
				//getting velocity for handling scrolling on up
				mVelocity.addMovement(event);
				mVelocity.computeCurrentVelocity(1);
				if (mVelocityDecelerator == null) {
					mVelocityDecelerator = new VelocityDecelerator(mVelocity.getXVelocity(), mVelocity.getYVelocity());
				}else {
					mVelocityDecelerator.start(mVelocity.getXVelocity(), mVelocity.getYVelocity());
				}
				mVelocity.recycle();
				//resume thread
				galleryThread.setRunning(true);
				//handle onClick
				if (downX==event.getX()) {
					for(Cell cell:videoList){
						if (cell.isContain(event.getX(),event.getY())) {
							activityListener.onVideoClick(moveCell.getVideo());
						}
					}
				}
				break;
		}
		return true;
	}
	public class AnimationThread extends Thread {
		/*Animation thread handle auto movement and scroll.
		 * Update position of object.
		 * 
		 * */
		//handle thread pause and resume 
		protected boolean mRun;  
		protected boolean pause;  
		//SurfaceHolder object
		protected SurfaceHolder mSurfaceHolder;
		//scroll x coordinates
		private int mDeltaX;
		//scroll y coordinates
		private int mDeltaY;   
		public AnimationThread(SurfaceHolder surfaceHolder, GalleryView galleryView) {
			mSurfaceHolder = surfaceHolder;
		}

		@Override
		public void run() { 
			while (mRun) {
				if (pause) { 
					if(isAutoMove&&moveCell!=null){
						//on auto moving
						moveVideoMatrix(moveCell.getmX().getLineX()+sensorX, moveCell.getmY().getLineY());
						setCanvasForDraw();
					}else {
						//on scrolling
						updatePosition();
						setCanvasForDraw();
					}
				}
			}
		}
		public void setThreadState(boolean thread) {
			mRun = thread;
		}
		public void setRunning(boolean thread) {
			pause = thread;
		}

		public void updatePosition() {
			if (mVelocityDecelerator != null && mVelocityDecelerator.isMoving()) {
				//when get scrolling value from onTouch method
				mVelocityDecelerator.calculateFreezeFrameData();
				mDeltaX = ((Math.round(mVelocityDecelerator.getDeltaDistanceX())*mVelocityDecelerator.getDirectionX())/2)/2;
				mDeltaY = ((Math.round(mVelocityDecelerator.getDeltaDistanceY())*mVelocityDecelerator.getDirectionY())/2)/2;
				moveVideoMatrix(moveCell.getmX().getLineX()+mDeltaX,moveCell.getmY().getLineY()+mDeltaY);
			}else {
							isAutoMove=true;
			}
		}
	}
	private void moveVideoMatrix(float x,float y) {
		//find object that contain x and  y coordinates.After that move found object
		if (moveCell==null) {
			for(Cell cell:videoList){
				if (cell.isContain(x,y)) {
					moveCell=cell;
				}
			}
		}
		moveCell.reset(x,y);
		moveAllVideo();
	}


	public void setCanvasForDraw() {
		//acquire canvas holder and hit draw method for draw all object
		synchronized (holder) {  
			Canvas mCanvas = holder.lockCanvas();
			drawGallary(mCanvas);
			holder.unlockCanvasAndPost(mCanvas);
		}
	}
	private void moveAllVideo() {
		//this method handle view cycle
		
		//Managing left and right movement of list
		leftLineNo=moveCell.getmX().getLineNo();
		rightLineNo=leftLineNo;
		tempLeft=moveCell.getmX();
		tempRight=moveCell.getmX();
		for (int index = 0; index < cols.size(); index++) {
			//right side move
			left=getLineFromBack((leftLineNo-1),cols); 
			if(left!=null){
				left.setLineX(tempLeft.getLineX()-imgw);
				tempLeft=left;
				leftLineNo=left.getLineNo();
			}
			right=getLineFromBack((rightLineNo+1),cols);
			if(right!=null){
				right.setLineX(tempRight.getLineX()+imgw);
				rightLineNo=right.getLineNo();
				tempRight=right;
			}

		}
		if(getLineFromBack((cols.size()-1),cols).getLineX()>viewBoundaryRect.right){
			left=getLineFromBack(0,cols);
			tempLeft=getLineFromBack((cols.size()-1),cols);
			tempLeft.setLineX(left.getLineX()-imgw);
			tempLeft.setLineNo(0);
			left.setLineNo(-1);
			for(Line line:cols){
				if(line.getLineNo()==-1){
					line.setLineNo(1);
				}else if(line.getLineNo()!=0){
					line.setLineNo(line.getLineNo()+1);
				}
			}
		}
		if(getLineFromBack(0,cols).getLineX()<viewBoundaryRect.left){
			right=getLineFromBack(0,cols);
			tempRight=getLineFromBack((cols.size()-1),cols);
			right.setLineX(tempRight.getLineX()+imgw);
			tempRight.setLineNo(-1);
			right.setLineNo((cols.size()-1));
			for(Line line:cols){
				if(line.getLineNo()==-1){
					line.setLineNo((cols.size()-2));
				}else if(line.getLineNo()!=(cols.size()-1)){
					line.setLineNo(line.getLineNo()-1);
				}
			}
		}

		//Managing up and down  movement of list
		leftLineNo=moveCell.getmY().getLineNo();
		rightLineNo=leftLineNo;
		tempLeft=moveCell.getmY();
		tempRight=moveCell.getmY();
		for (int index = 0; index < rows.size(); index++) {
			left=getLineFromBack((leftLineNo-1),rows); 
			if(left!=null){
				left.setLineY(tempLeft.getLineY()-imgh);
				tempLeft=left;
				leftLineNo=left.getLineNo();
			}
			right=getLineFromBack((rightLineNo+1),rows);
			if(right!=null){
				right.setLineY(tempRight.getLineY()+imgh);
				rightLineNo=right.getLineNo();
				tempRight=right;
			}
		}
		if(getLineFromBack((rows.size()-1),rows).getLineY()>viewBoundaryRect.bottom){
			left=getLineFromBack(0,rows);
			tempLeft=getLineFromBack((rows.size()-1),rows);
			tempLeft.setLineY(left.getLineY()-imgh);
			tempLeft.setLineNo(0);
			left.setLineNo(-1);
			for(Line line:rows){
				if(line.getLineNo()==-1){
					line.setLineNo(1);
				}else if(line.getLineNo()!=0){
					line.setLineNo(line.getLineNo()+1);
				}
			}
		}
		if(getLineFromBack(0,rows).getLineY()<viewBoundaryRect.top){
			right=getLineFromBack(0,rows);
			tempRight=getLineFromBack((rows.size()-1),rows);
			right.setLineY(tempRight.getLineY()+imgh);
			tempRight.setLineNo(-1);
			right.setLineNo((rows.size()-1));
			for(Line line:rows){
				if(line.getLineNo()==-1){
					line.setLineNo((rows.size()-2));
				}else if(line.getLineNo()!=(rows.size()-1)){
					line.setLineNo(line.getLineNo()-1);
				}
			}
		}
		for(Cell cell:videoList){
			cell.move();
		}
	}
	private Line getLineFromBack(int lineNo, LinkedList<Line> lines) {
		if (lineNo<0) 
			return null;
		for(Line line:lines){
			if(lineNo==line.getLineNo()){
				return line;
			}
		}
		return null;  
	} 

	public void loadThumail(String thumbNail, Cell video) { 
//		imageLoader.getImage(thumbNail,video);
	}
	public void setSensorCoordinates(float x, float y) {
		sensorX=x;
		sensorY=y;
		
	}
	public void killThread() {
		galleryThread.setThreadState(false);
	}
	public void setRunThread(boolean thread) {
		if(galleryThread!=null)
		galleryThread.setRunning(thread);
	}
   
	
	@Override
	public void imageLoaded(LoadedImage uri) {
		if (cellID<videoList.size()) {
			imageUri.add(uri);
			videoList.get(cellID).setVideo((Activity) mContext,uri);
			cellID++;
		}
	}
	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		 Sensor mySensor = sensorEvent.sensor;
		 if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			 float x = sensorEvent.values[0];
		        float y = sensorEvent.values[1];
		        setSensorCoordinates(x,y);
		 }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
}
