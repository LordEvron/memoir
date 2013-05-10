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
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

public class MemoirApplication extends Application {

	private MemoirDBA mDBA;
	private static boolean useExternal = true;
	private SharedPreferences mPrefs = null;
	public static Object sync = null;
	
	@Override
	public void onCreate() {
		mDBA = new MemoirDBA(getApplicationContext());
		mPrefs = this.getSharedPreferences("com.devapp.memoir", Context.MODE_PRIVATE);

		if(!mPrefs.contains("com.devapp.memoir.startall")) {
			SharedPreferences.Editor editor = mPrefs.edit();			
			SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
			long date = Long.parseLong(ft.format(new Date()));
			editor.putLong("com.devapp.memoir.startall", date);
			editor.putLong("com.devapp.memoir.endall", date);
			editor.putLong("com.devapp.memoir.startselected", date);
			editor.putLong("com.devapp.memoir.endselected", date);
			editor.putBoolean("com.devapp.memoir.datachanged", false);
			editor.putBoolean("com.devapp.memoir.showonlymultiple", false);
			editor.commit();
			
			Video v = getMyLifeFile(this);
			if(v != null) {
				File f = new File(v.path);
				f.delete();
			}
			Log.d("zxc", "Setting all prefernces to this date" + date);
		} else {
			Log.d("asd", "com.devapp.memoir.startall > " + mPrefs.getLong("com.devapp.memoir.startall", 0));
			Log.d("asd", "com.devapp.memoir.endall > " + mPrefs.getLong("com.devapp.memoir.endall", 0));
			Log.d("asd", "com.devapp.memoir.startselected > " + mPrefs.getLong("com.devapp.memoir.startselected", 0));
			Log.d("asd", "com.devapp.memoir.endselected > " + mPrefs.getLong("com.devapp.memoir.endselected", 0));
		}
		
		mDBA.updateDatabase();
	}
	
	public static String convertDate(long date, String defaultS) {
		if(date == 0) {
			return defaultS;
		}
		String dateS = String.valueOf(date);
		dateS = dateS.substring(6,8) + "/" + dateS.substring(4,6) + "/" + dateS.substring(0, 4);
		return dateS;
	}

	public MemoirDBA getDBA() {
		return mDBA;
	}

	public static Video getMyLifeFile(Context c) {
		String outputFilename = null;

		if (useExternal) {
//			outputFilename = Environment.getExternalStoragePublicDirectory(
//					Environment.DIRECTORY_MOVIES).getPath();
			outputFilename = c.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
		} else
			outputFilename = c.getFilesDir().getAbsolutePath();

		outputFilename = outputFilename.concat("/Memoir/MyLife.mp4");
		File f = new File(outputFilename);
		
		if(f.exists()) {
			return new Video(c, outputFilename);
		} else {
			return null;
		}
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
//						Environment
//								.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
								c.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath(),
						"Memoir");
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						//Log.d("Memoir", "failed to create directory");
						return null;
					}
				}

				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
						.format(new Date());
				String fileName = mediaStorageDir.getPath() + File.separator
						+ "VID_" + timeStamp + ".mp4";
				//Log.d("asd", "FileName is " + fileName);
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
					//Log.d("Memoir", "failed to create directory");
					return null;
				}
			}
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
			String fileName = mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4";
			//Log.d("asd", "FileName is " + fileName);
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
//						Environment
//								.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
						c.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath(),
						"Memoir/.thumbnails");
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						//Log.d("Memoir", "failed to create directory");
						return null;
					}
				}

				Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,
						MediaStore.Video.Thumbnails.MINI_KIND);
				if(bitmap != null) {
					try {
						newPath = path.substring(0, path.length() - 3) + "png";
						Log.d("asd", newPath);
						FileOutputStream out = new FileOutputStream(newPath);
						bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
			//Log.d("asd", "getDataDirectory > "
			//		+ c.getFilesDir().getAbsolutePath());
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
	
	public static String getFilePathFromContentUri(Uri selectedVideoUri,
	        ContentResolver contentResolver) {
	    String filePath;
	    String[] filePathColumn = {MediaColumns.DATA};

	    Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
	    cursor.moveToFirst();

	    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	    filePath = cursor.getString(columnIndex);
	    cursor.close();
	    return filePath;
	}

}
