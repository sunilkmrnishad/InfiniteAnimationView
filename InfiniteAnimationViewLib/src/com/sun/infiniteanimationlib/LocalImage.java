package com.sun.infiniteanimationlib;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

class LocalImage {

	private ImageNotifier notifier;
	private Activity mActivity;

	public interface ImageNotifier {
		public void imageLoaded(LoadedImage video);
	}

	public LocalImage(Context mContext, GalleryView galleryView) {
		notifier = galleryView;
		mActivity=(Activity) mContext;
		 new LoadImagesFromSDCard().execute();
	}


	private class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

		/**
		 * Load images from SD Card in the background, and display each image on
		 * the screen.
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Object doInBackground(Object... params) {

			String[] projection = { MediaStore.Images.Thumbnails._ID };
			// Create the cursor pointing to the SDCard
			Cursor cursor = mActivity.managedQuery(
					MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
					projection, // Which columns to return
					null, // Return all rows
null, null);
			int columnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
			int size = cursor.getCount();
			// If size is 0, there are no images on the SD Card.
			if (size == 0) {
				// No Images available, post some message to the user
			}
			for (int i = 0; i < size; i++) {
				cursor.moveToPosition(i);
							publishProgress(new LoadedImage(Uri.withAppendedPath(
									MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, ""
											+ cursor.getInt(columnIndex))));
			}
			cursor.close();
			return null;
		}

		/**
		 * Add a new LoadedImage in the images grid.
		 * @param value
		 *            The image.
		 */
		@Override
		public void onProgressUpdate(LoadedImage... value) {
			addImage(value);
			
		}
	    /**
	     * Add image(s) to the grid view adapter.
	     * 
	     * @param value Array of LoadedImages references
	     */
	    private void addImage(LoadedImage... value) {
	        for (LoadedImage image : value) {
	        	notifier.imageLoaded(image);
	        }
	    }
		
	}
}