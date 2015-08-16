package com.sun.infiniteanimationlib;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
public class Cell implements VideoManager {
	/*
	 * 
	 * */
	private Bitmap mVideoThumnail;
	private RectF mVideoRect;
	private Line mX;
	private Line mY;
	private final int PADDING=4;
	public boolean isImageLoaded;
	private float mVideoWidthHalf;
	private float mVideoHeightHalf;
	private GalleryView mGallaryView;
	private Bitmap mVideoTitleBG;
	private RectF mVideoTitleRect;
	private String mVideoTitle="";
	private String mArtistName="";

	private LoadedImage loadedImage;
	
	public interface VideoClickListener {
		/*
		 * 
		 * */
		public void onVideoClick(Cell video);
	}
	public LoadedImage getLoadedImage() {
		return loadedImage;
	}
	public Cell(GalleryView gallaryView, Line x, Line y, float imgWidth, float imgHeight, Bitmap img, Bitmap videoTitleBG) {
		mVideoThumnail = img;
		mVideoTitleBG = videoTitleBG;
		mGallaryView = gallaryView;
		mX=x;
		mY=y;
		mVideoHeightHalf=imgHeight/2;
		mVideoWidthHalf=(imgWidth)/2;
		mVideoRect=new RectF(mX.getLineX()-mVideoWidthHalf+PADDING, mY.getLineY()-mVideoHeightHalf+PADDING, mX.getLineX()+mVideoWidthHalf-PADDING, mY.getLineY()+mVideoHeightHalf-PADDING);
		mVideoTitleRect=new RectF(mVideoRect.left+(PADDING*2), mVideoRect.bottom-(mVideoHeightHalf/2),
				mVideoRect.right-(PADDING*2), mVideoRect.bottom-(PADDING*2));
	}
	public Cell getVideo(){
		return null;
	}
	
//	public void setVideo(VideoDetail detail){
//		mDetail=detail;
//		
//		if(mDetail.VideoType==1)
//		{
//			//Video is a movie
//			mVideoTitle=mDetail.EnglishTitle;
//		}
//		else if(mDetail.VideoType==2)
//		{
//			//Video is a music
//			mVideoTitle=mDetail.EnglishTitle;
//			mArtistName = mDetail.ArtistEnglishName;
//		}
//		else if(mDetail.VideoType==3)
//		{
//			if(mDetail.NumberofSeasons==1)
//			{
//				mVideoTitle= mDetail.SeriesName +" - Ep"+mDetail.EpisodeNumber;
//			}
//			else
//			{
//				mVideoTitle= mDetail.SeriesName +" - Sn "+mDetail.Season+" - Ep "+mDetail.EpisodeNumber;
//			}
//		}
//		else
//		{
//			mVideoTitle=mDetail.EnglishTitle;
//		}
//		
//		mGallaryView.loadThumail(mDetail.ThumbNail,this);
//	}
	
	@Override
	public void render(Canvas canvas,Paint paint) {
		canvas.drawBitmap(mVideoThumnail, null, mVideoRect, paint);
		canvas.drawBitmap(mVideoTitleBG, null, mVideoTitleRect, paint);
		
		if(mArtistName.trim().length()==0)
		{
			canvas.drawText(mVideoTitle, mVideoTitleRect.left+10, mVideoTitleRect.centerY()+(getTextHeight(mVideoTitle, paint)/2), paint);
		}
		else
		{
			canvas.drawText(mVideoTitle, mVideoTitleRect.left+10, mVideoTitleRect.centerY()-3, paint);
			canvas.drawText(mArtistName, mVideoTitleRect.left+10, mVideoTitleRect.centerY()+1+(getTextHeight(mArtistName, paint)), paint);
		}
		
		if (!isImageLoaded) {
//			mGallaryView.loadThumail(mDetail.ThumbNail,this);
		}
	}
	private int getTextWidth(String text, Paint paint) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		int width = bounds.left + bounds.width();
		return width;
	}

	private int getTextHeight(String text, Paint paint) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
//		int height = bounds.bottom + bounds.height();
		int height = bounds.bottom - bounds.top;
		return height;
	}

	public RectF getImgRect() {
		return mVideoRect;
	}

	public void setImgRect(RectF imgRect) {
		this.mVideoRect = imgRect;
	}
	@Override
	public void reset(float x,float y) {
		mX.setLineX(x);
		mY.setLineY(y);
		move();
	}
	public Line getmX() {
		return mX;
	}

	public void setmX(Line mX) {
		this.mX = mX;
	}

	public Line getmY() {
		return mY;
	}

	public void setmY(Line mY) {
		this.mY = mY;
	}

	public boolean isContain(float x, float y) {
		return mVideoRect.contains(x, y);
	}
	@Override
	public void move() {
		mVideoRect.set(mX.getLineX()-mVideoWidthHalf+PADDING, mY.getLineY()-mVideoHeightHalf+PADDING, mX.getLineX()+mVideoWidthHalf-PADDING, mY.getLineY()+mVideoHeightHalf-PADDING);
		mVideoTitleRect.set(mVideoRect.left+(PADDING*2), mVideoRect.bottom-(mVideoHeightHalf/2),
				mVideoRect.right-(PADDING*2), mVideoRect.bottom-(PADDING*2));
	}
	public void setVideoThumnail(Bitmap loadedImage) {
		if(loadedImage!=null){
			isImageLoaded=true;
			mVideoThumnail=loadedImage;
		}
	}

	public void setVideo(Activity  mActivity,LoadedImage image) {
		if(image!=null){
			loadedImage = image;
			mVideoThumnail=Utility.getBitmap(mActivity, image.getmUri());
			isImageLoaded=true;
		}
	}
}
