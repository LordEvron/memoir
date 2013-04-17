package com.devapp.memoir;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class MemoirApplication extends Application {

	private MemoirDBA mDBA;
	private static boolean useExternal = true;

	@Override
	public void onCreate() {
		mDBA = new MemoirDBA(getApplicationContext());

	}

	public MemoirDBA getDBA() {
		return mDBA;
	}

	public static String getConcatenatedOutputFile(Context c) {
		String outputFilename = null;

		if (useExternal) {
			outputFilename = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_MOVIES).getPath();
		} else
			outputFilename = c.getFilesDir().getAbsolutePath();

		return outputFilename.concat("/Memoir/output.mp4");
	}

	public static String getOutputMediaFile(Context c) {

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;

		if (useExternal) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				// We can read and write the media
				mExternalStorageAvailable = mExternalStorageWriteable = true;

				File mediaStorageDir = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
						"Memoir");
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						Log.d("Memoir", "failed to create directory");
						return null;
					}
				}

				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
						.format(new Date());
				String fileName = mediaStorageDir.getPath() + File.separator
						+ "VID_" + timeStamp + ".mp4";
				Log.d("asd", "FileName is " + fileName);
				return fileName;

			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				// We can only read the media
				mExternalStorageAvailable = true;
				mExternalStorageWriteable = false;
			} else {
				// Something else is wrong. It may be one of many other states,
				// but all we need
				// to know is we can neither read nor write
				mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
		} else {
			Log.d("asd", "getDataDirectory > "
					+ c.getFilesDir().getAbsolutePath());
			File mediaStorageDir = new File(c.getFilesDir().getAbsolutePath(),
					"Memoir");
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					Log.d("Memoir", "failed to create directory");
					return null;
				}
			}
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
			String fileName = mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4";
			Log.d("asd", "FileName is " + fileName);
			return fileName;
		}

		return null;
	}

	public static String storeThumbnail(Context c, String path) {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String newPath = null;

		if (useExternal) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				// We can read and write the media
				mExternalStorageAvailable = mExternalStorageWriteable = true;

				File mediaStorageDir = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
						"Memoir/.thumbnails");
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						Log.d("Memoir", "failed to create directory");
						return null;
					}
				}

				Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,
						MediaStore.Video.Thumbnails.MINI_KIND);
				try {
					newPath = path.substring(0, path.length() - 3) + "png";
					Log.d("asd", newPath);
					FileOutputStream out = new FileOutputStream(newPath);
					bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return newPath;

			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				// We can only read the media
				mExternalStorageAvailable = true;
				mExternalStorageWriteable = false;
			} else {
				// Something else is wrong. It may be one of many other states,
				// but all we need
				// to know is we can neither read nor write
				mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
		} else {
			Log.d("asd", "getDataDirectory > "
					+ c.getFilesDir().getAbsolutePath());
			File mediaStorageDir = new File(c.getFilesDir().getAbsolutePath(),
					"Memoir/.thumbnails");
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					Log.d("Memoir", "failed to create directory");
					return null;
				}
			}
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,
					MediaStore.Video.Thumbnails.MINI_KIND);
			try {
				newPath = path.substring(0, path.length() - 3) + "png";
				FileOutputStream out = new FileOutputStream(newPath);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return newPath;
		}

		return null;
	}

	public static Bitmap getImageBitmap(String url) {
		Bitmap bm = null;
		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (IOException e) {
			Log.e("asd", "Error getting bitmap", e);
		}
		return bm;
	}

}
