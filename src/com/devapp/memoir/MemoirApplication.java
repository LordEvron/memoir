package com.devapp.memoir;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.devapp.memoir.database.MemoirDBA;
import com.devapp.memoir.database.Video;
import com.devapp.memoir.services.ThumbnailLoader;

public class MemoirApplication extends Application {

	private MemoirDBA mDBA;
	public static boolean useExternal = true;
	private SharedPreferences mPrefs = null;
	public static int mWidth = 0, mHeight = 0;
	public static ThumbnailLoader mTL = null;
	private static String[] getMonth = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

	@Override
	public void onCreate() {
		mDBA = new MemoirDBA(getApplicationContext());
		mTL = ThumbnailLoader.initialize(this, mDBA);
		mPrefs = this.getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);

		if (!mPrefs.contains("com.devapp.memoir.startall")) {
			SharedPreferences.Editor editor = mPrefs.edit();
			SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
			long date = Long.parseLong(ft.format(new Date()));
			editor.putLong("com.devapp.memoir.startall", date);
			editor.putLong("com.devapp.memoir.endall", date);
			editor.putLong("com.devapp.memoir.startselected", date);
			editor.putLong("com.devapp.memoir.endselected", date);
			editor.putBoolean("com.devapp.memoir.datachanged", false);
			editor.putBoolean("com.devapp.memoir.showonlymultiple", false);
			editor.putBoolean("com.devapp.memoir.shootoncall", true);
			editor.putInt("com.devapp.memoir.noofseconds", 2);
			editor.commit();

			Video v = getMyLifeFile(this);
			if (v != null) {
				File f = new File(v.path);
				f.delete();
			}
		}/* else {
			Log.d("asd",
					"com.devapp.memoir.startall > "
							+ mPrefs.getLong("com.devapp.memoir.startall", 0));
			Log.d("asd",
					"com.devapp.memoir.endall > "
							+ mPrefs.getLong("com.devapp.memoir.endall", 0));
			Log.d("asd",
					"com.devapp.memoir.startselected > "
							+ mPrefs.getLong("com.devapp.memoir.startselected",
									0));
			Log.d("asd",
					"com.devapp.memoir.endselected > "
							+ mPrefs.getLong("com.devapp.memoir.endselected", 0));
		}*/

		this.sendBroadcast(new Intent(
				"com.devapp.memoir.BootupBroadcastReceiver"));

	}

	public static String convertDate(long date, String defaultS) {
		if (date == 0) {
			return defaultS;
		}
		String dateS = String.valueOf(date);
		dateS = dateS.substring(6, 8) + " " + getMonth[(int) ((date % 10000) / 100) - 1] + " "
				+ dateS.substring(0, 4);
		return dateS;
	}	

	public static String getDateStringWRTToday(long date) {
		String daysAgo = null;

		long DAY_IN_MILLIS = 86400000;
		long now = System.currentTimeMillis();

		Calendar cal1 = Calendar.getInstance();
		int day = (int) (date % 100);
		int month = (int) ((date % 10000) / 100) - 1;
		int year = (int) (date / 10000);
		cal1.set(year, month, day);
		
		long then = cal1.getTimeInMillis();
		long difference = now - then;
		int ago = 0;

		if (difference >= DAY_IN_MILLIS) {
			ago = (int) (difference / DAY_IN_MILLIS);
			if(ago <= 10) {
				daysAgo = String.format(Locale.ENGLISH, "%d days ago", ago);
			} else {
				daysAgo = String.format(Locale.ENGLISH, "%d %s %d", day , getMonth[month], year);
			}
		} else if(difference < 0) {
			daysAgo = String.format(Locale.ENGLISH, "In Future - %d %s %d", day , getMonth[month], year);
		} else {
			daysAgo = String.format("Today");
		}
		//Log.d("asd", daysAgo);
		return daysAgo;
	}

	public static String convertPath(String path) {
		return path.substring(0, path.length() - 3) + "png";
	}
	
	public MemoirDBA getDBA() {
		return mDBA;
	}

	public Video getMyLifeFile(Context c) {
		String outputFilename = null;

		if (useExternal) {
			// outputFilename = Environment.getExternalStoragePublicDirectory(
			// Environment.DIRECTORY_MOVIES).getPath();
			outputFilename = c
					.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
					.getAbsolutePath();
		} else
			outputFilename = c.getFilesDir().getAbsolutePath();

		outputFilename = outputFilename.concat("/Memoir/MyLife.mp4");
		File f = new File(outputFilename);

		if (f.exists()) {
			Video v = new Video(outputFilename);
			if(!(new File(convertPath(outputFilename)).exists())) {
				MemoirApplication.mTL.convertThumbnail(outputFilename, MediaStore.Video.Thumbnails.MINI_KIND);
			}
			v.thumbnailPath = convertPath(outputFilename);
			return v;
		}
		return null;
	}

	public static String getOutputMediaFile(Context c) {

		//boolean mExternalStorageAvailable = false;
		//boolean mExternalStorageWriteable = false;

		if (useExternal) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				// We can read and write the media
				//mExternalStorageAvailable = mExternalStorageWriteable = true;

				File mediaStorageDir = new File(
				// Environment
				// .getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
						c.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
								.getAbsolutePath(), "Memoir");
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						// Log.d("Memoir", "failed to create directory");
						return null;
					}
				}

				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
						.format(new Date());
				String fileName = mediaStorageDir.getPath() + File.separator
						+ "VID_" + timeStamp + ".mp4";
				// Log.d("asd", "FileName is " + fileName);
				return fileName;

			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				//mExternalStorageAvailable = true;
				//mExternalStorageWriteable = false;
			} else {
				//mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
		} else {
			File mediaStorageDir = new File(c.getFilesDir().getAbsolutePath(),
					"Memoir");
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					return null;
				}
			}
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
			String fileName = mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4";
			return fileName;
		}

		return null;
	}

	public class storeThumbnailTask extends AsyncTask<Void, Void, Void> {


		
		public void updateDatebaseForThumbnail() {
			
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static String getFilePathFromContentUri(Uri selectedVideoUri,
			ContentResolver contentResolver) {
		String filePath;
		String[] filePathColumn = { MediaColumns.DATA };

		Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn,
				null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		filePath = cursor.getString(columnIndex);
		cursor.close();
		return filePath;
	}

	public static String getDateFromContentUri(Uri selectedVideoUri,
			ContentResolver contentResolver) {
		long date;
		String[] dateColumn = { MediaColumns.DATE_ADDED };

		Cursor cursor = contentResolver.query(selectedVideoUri, dateColumn,
				null, null, null);
		cursor.moveToFirst();

		date = cursor.getLong(cursor.getColumnIndex(dateColumn[0]));
		cursor.close();
		String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date(date * 1000L));
		return timeStamp;
	}

	
	@SuppressLint("NewApi")
	public static void setDisplayMatrix(Activity a) {
		/** Note : For getting the height and width of the screen */
		if (android.os.Build.VERSION.SDK_INT >= 14
				&& android.os.Build.VERSION.SDK_INT <= 16) {
			Display display = a.getWindowManager()
					.getDefaultDisplay();
			try {
				Method mGetRawH = Display.class.getMethod("getRawHeight");
				Method mGetRawW = Display.class.getMethod("getRawWidth");
				mWidth = (Integer) mGetRawW.invoke(display);
				mHeight = (Integer) mGetRawH.invoke(display);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			Display display = a.getWindowManager()
					.getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getRealMetrics(outMetrics);
			mHeight = outMetrics.heightPixels;
			mWidth = outMetrics.widthPixels;
		}
	}
}
