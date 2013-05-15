package com.devapp.memoir.services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.devapp.memoir.MemoirApplication;
import com.devapp.memoir.database.MemoirDBA;

public class ThumbnailLoader {

	private class imageObject {
		String imageUrl;
		Bitmap imageBitmap;
		ConcurrentLinkedQueue<ImageView> imageViews;
	}

	static ThumbnailLoader TLRef;
	static LinkedHashMap<String, imageObject> imageCache;
	static final Object mTaskLock = new Object();
	static int mTaskCounter = 0;
	static ConcurrentLinkedQueue<Object> fastQueue;

	static MemoirDBA mDBA = null;

	private ThumbnailLoader() {
	}

	public static ThumbnailLoader initialize(Context context, MemoirDBA DBA) {
		if (TLRef == null) {
			TLRef = new ThumbnailLoader();
			mDBA = DBA;
			imageCache = new LinkedHashMap<String, imageObject>(200, 0.75f,
					true);
			fastQueue = new ConcurrentLinkedQueue<Object>();
			mTaskCounter = 0;
		}
		return TLRef;
	}

	public void loadImage(String imageUrl, ImageView imageView) {

		if ((imageUrl == null) || (imageUrl.length() == 0)) {
			Log.e("asd", "Image URL is null or Empty");
			return;
		}

		//Log.d("asd", "For Image URL " + imageUrl + " and imageView >" + imageView.toString());
		if (imageCache.containsKey(imageUrl) == true) {
			//Log.d("asd", "Found it in cache");
			imageObject iO = imageCache.get(imageUrl);
			if (iO != null && iO.imageBitmap != null) {

				if (imageView != null) {
					imageView.setImageBitmap(iO.imageBitmap);
				}
				ImageView iv;
				while ((iv = (ImageView) iO.imageViews.poll()) != null) {
					iv.setImageBitmap(iO.imageBitmap);
				}
				return;
			} else if (iO != null) {
				boolean existsFlag = false;
				for (Iterator<ImageView> it = iO.imageViews.iterator(); it
						.hasNext();) {
					ImageView iv = (ImageView) it.next();
					if (iv == imageView) {
						existsFlag = true;
						break;
					}
				}
				if (existsFlag == false && imageView != null) {
					iO.imageViews.add(imageView);
				}
				// imageCache.remove(imageUrl);
				return;
			} else {
				// Log.d(TAG,
				// "Did the softrefernce clear my data ?>?????? for image url "
				// + imageUrl);
			}
		}

		File f = new File(MemoirApplication.convertPath(imageUrl));

		if (f.exists()) {

			//Log.d("asd", "png exists .. reading manually");
			imageObject iO1 = new imageObject();
			iO1 = new imageObject();
			iO1.imageUrl = imageUrl;

			iO1.imageViews = new ConcurrentLinkedQueue<ImageView>();

			iO1.imageBitmap = BitmapFactory.decodeFile(MemoirApplication.convertPath(imageUrl));

			if (imageView != null) {
				imageView.setImageBitmap(iO1.imageBitmap);
			}

			synchronized (getClass()) {
				imageCache.put(imageUrl, iO1);
			}

			return;
		}

		/** Create the thumbnail */
		imageObject iO1 = new imageObject();
		iO1 = new imageObject();
		iO1.imageUrl = imageUrl;
		iO1.imageViews = new ConcurrentLinkedQueue<ImageView>();

		if (imageView != null) {
			iO1.imageViews.add(imageView);
		}

		synchronized (getClass()) {
			imageCache.put(imageUrl, iO1);
		}

		fastQueue.add(iO1);
		//Log.d("asd", "sending request to create png");

		synchronized (mTaskLock) {
			if (mTaskCounter < 5) {
				mTaskCounter++;
				new GetThumbnailTask().executeOnExecutor(
						AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) (null));
			}
		}
	}

	public Bitmap convertThumbnail(String path, int kind) {
		/** Note: kind can be MediaStore.Video.Thumbnails.MICRO_KIND, 
		 * MediaStore.Video.Thumbnails.MINI_KIND
		 */
//		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,
//				MediaStore.Video.Thumbnails.MINI_KIND);
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,kind);
				
		if (bitmap != null) {
			try {
				FileOutputStream out = new FileOutputStream(
						MemoirApplication.convertPath(path));
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	private class GetThumbnailTask extends AsyncTask<Void, imageObject, Void> {

		@Override
		protected Void doInBackground(Void... urls) {

			imageObject iO = null;

			while (true) {
				if ((iO = (imageObject) fastQueue.poll()) != null) {
					iO.imageBitmap = convertThumbnail(iO.imageUrl, MediaStore.Video.Thumbnails.MICRO_KIND);
					publishProgress(iO);
					iO = null;
					continue;
				}
				synchronized (mTaskLock) {
					mTaskCounter--;
					break;
				}
			}
			return null;
		}

		protected void onProgressUpdate(imageObject... values) {
			super.onProgressUpdate(values);

			mDBA.updateDatabaseForThumbnail(values[0].imageUrl,
					MemoirApplication.convertPath(values[0].imageUrl));
			ImageView iv = null;
			if (values[0].imageBitmap != null) {
				while ((iv = (ImageView) values[0].imageViews.poll()) != null) {
					// Log.d(TAG, "setting imageUrl > " + values[0].imageUrl +
					// "   imageView > " + iOP.iv);
					iv.setImageBitmap(values[0].imageBitmap);
				}
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// Log.d(TAG , "" + System.currentTimeMillis());
		}
	}
}
