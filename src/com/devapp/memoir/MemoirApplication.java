package com.devapp.memoir;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
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
	public static ThumbnailLoader mTL = null;
	private static String[] getMonth = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	private static String mExtFileDirectory = null;

	@Override
	public void onCreate() {
		mDBA = new MemoirDBA(getApplicationContext());
		mTL = ThumbnailLoader.initialize(this, mDBA);
		mPrefs = this.getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);

		mExtFileDirectory = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
		
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

			setDefaultCameraResolution();

			//Not adding shortcut for now
			//addShortcut(this);
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

		Log.d("asd", "What is the difference >" + difference);
		if (difference >= DAY_IN_MILLIS - 10000/*Tolerance*/) {
			ago = (int) (difference / DAY_IN_MILLIS);
			if(ago == 1) {
				daysAgo = String.format(Locale.ENGLISH, "%d day ago", ago);
			} else if(ago <= 10) {
				daysAgo = String.format(Locale.ENGLISH, "%d days ago", ago);
			} else {
				daysAgo = String.format(Locale.ENGLISH, "%d %s %d", day , getMonth[month], year);
			}
		} else if(difference < -10000 /*Tolerance*/) {
			daysAgo = String.format(Locale.ENGLISH, "%d %s %d", day , getMonth[month], year);
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
	
	public static Video getMyLifeFileStatic() {
		String outputFilename = mExtFileDirectory.concat("/Memoir/MyLife.mp4");
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

	public static String getFilePathFromContentUri(Uri selectedVideoUri,
			ContentResolver contentResolver) {
		String filePath;
		Cursor cursor = null;
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			String[] filePathColumn = { MediaColumns.DATA, MediaColumns.HEIGHT, MediaColumns.WIDTH };
			cursor = contentResolver.query(selectedVideoUri, filePathColumn,
					null, null, null);
		} else {
			String[] filePathColumn = { MediaColumns.DATA };
			cursor = contentResolver.query(selectedVideoUri, filePathColumn,
					null, null, null);
		}

		cursor.moveToFirst();

		filePath = cursor.getString(0);
		
		if (android.os.Build.VERSION.SDK_INT == 16) {
			int height = cursor.getInt(1);
			int width = cursor.getInt(2);
			cursor.close();
			Log.d("asd", "width in content provider width >" + width + "  height " + height);
			if(width < height)
				return null;
		}
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
	
	public void setDefaultCameraResolution() {

		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			Log.e("asd", "Camera is not available");
		}

		Parameters param = c.getParameters();
		List<Camera.Size> list = null;
		int maxHeight = 0, maxWidth = 0;
		
/*		NOTE: This didnt work for ashish :(
 * 		size = param.getPreferredPreviewSizeForVideo();
		if(size == null) {
			list = param.getSupportedVideoSizes();
			if(list != null && list.size() > 0) {
				maxWidth = 0;
				for(Size s : list) {
					if(s.width > maxWidth) {
						maxWidth = s.width;
						size = s;
					}
				}
			} else {
				list = param.getSupportedPreviewSizes();
				if(list != null && list.size() > 0) {
					maxWidth = 0;
					for(Size s : list) {
						if(s.width > maxWidth) {
							maxWidth = s.width;
							size = s;
						}
					}
				}
			}
		}*/

		list = param.getSupportedVideoSizes();
		if(list != null && list.size() > 0) {
			maxWidth = 0;
			for(Size s : list) {
				if(s.width > maxWidth) {
					maxWidth = s.width;
				}
			}
		}
		list = param.getSupportedPreviewSizes();
		if(list != null && list.size() > 0) {
			for(Size s : list) {
				Log.d("asd", "Height " + s.height + " Width" + s.width);
				if(s.width > maxWidth) {
					maxWidth = s.width;
				}
			}
		}
		
		Log.d("asd", "maxWidth " + maxWidth);
		if(maxWidth == 1920) {
			maxHeight = 1080;
		} else if(maxWidth == 1280) {
			maxHeight = 720;
		} else if(maxWidth == 960) {
			maxHeight = 720;
		} else if(maxWidth == 800) {
			maxHeight = 480;
		} else if(maxWidth == 768) {
			maxHeight = 576;
		} else if(maxWidth == 720) {
			maxHeight = 480;
		} else if(maxWidth == 640) {
			maxHeight = 480;
		} else if(maxWidth == 352) {
			maxHeight = 288;
		} else if(maxWidth == 320) {
			maxHeight = 240;
		} else if(maxWidth == 240) {
			maxHeight = 160;
		} else if(maxWidth == 176) {
			maxHeight = 144;
		} else if(maxWidth == 128) {
			maxHeight = 96;
		}
		Log.e("asd", "Height being written as " + maxHeight + "   width " + maxWidth);
		
		mPrefs.edit().putInt("com.devapp.memoir.standardheight", maxHeight).putInt("com.devapp.memoir.standardwidth", maxWidth).commit();
	}
	
	public static void addShortcut(Context context)
	{
	    Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

	    ApplicationInfo appInfo = context.getApplicationInfo();

	    // Shortcut name
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appInfo.name);
//	    shortcut.putExtra("duplicate", false); // Just create once

	    // Setup activity shoud be shortcut object 
//	    ComponentName component = new ComponentName(appInfo.packageName, appInfo.className);
	    Intent i = new Intent(Intent.ACTION_MAIN);
	    i.setClassName(context, appInfo.className);
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);

	    // Set shortcut icon
	    ShortcutIconResource iconResource = Intent.ShortcutIconResource.fromContext(context, appInfo.icon);
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

	    context.sendBroadcast(shortcut);
	    Log.d("asd", "Firing add shortcut intent");
	    
	    
/*	    
	    
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());
        shortcutIntent.putExtra(EXTRA_KEY, "ApiDemos Provided This Shortcut");

        // Then, set up the container intent (the response to the caller)

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_name));
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
                this,  R.drawable.app_sample_code);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        */

	}

	public static void deleteShortcut(Context context)
	{
	    Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");

	    ApplicationInfo appInfo = context.getApplicationInfo();

	    // Shortcut name
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appInfo.name);

	    ComponentName comp = new ComponentName(appInfo.packageName, appInfo.className);
	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));

	    context.sendBroadcast(shortcut);
	}
	/** NOTE: This receiver needs to be there somewhere.. dont know where.. 
	<receiver android:name="YOUR.PACKAGE.PackageReplacedReceiver">
    <intent-filter>
        <action android:name="android.intent.action.PACKAGE_REPLACED" />
        <data android:scheme="package" android:path="YOUR.PACKAGE" />
    </intent-filter>
	</receiver>
	 */

}
