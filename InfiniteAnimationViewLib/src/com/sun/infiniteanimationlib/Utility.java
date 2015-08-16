package com.sun.infiniteanimationlib;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class Utility {

	public static Bitmap getBitmap(Activity mActivity,Uri uri)
	{
		Bitmap bitmap,newBitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(mActivity.getContentResolver().openInputStream(uri));
			if (bitmap != null) {
				newBitmap = Bitmap.createScaledBitmap(bitmap, 213, 160,true);
				bitmap.recycle();
				return newBitmap;
			}
		} catch (IOException e) {
		}
		return newBitmap;
	}
}
