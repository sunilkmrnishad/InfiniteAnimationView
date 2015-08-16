package com.sun.infiniteanimationviewdemo;

import android.app.Activity;
import android.os.Bundle;

import com.sun.infiniteanimationlib.Cell;
import com.sun.infiniteanimationlib.GalleryView;
public class MainActivity extends Activity implements Cell.VideoClickListener{
	private GalleryView galleryView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        galleryView = new GalleryView(this,true,true);
		setContentView(galleryView);
    }
    protected void onResume() {
	    super.onResume();
	    galleryView.setRunThread(true);
	}
    protected void onPause() {
	    super.onPause(); 
	    galleryView.setRunThread(false);
	}
	@Override
	public void onVideoClick(Cell video) {
		
	}
}