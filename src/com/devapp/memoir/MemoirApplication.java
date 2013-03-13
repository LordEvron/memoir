package com.devapp.memoir;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

public class MemoirApplication extends Application {
	
	private MemoirDBA mDBA;
	private static boolean useExternal = true;
	
	@Override
	public void onCreate () {
		mDBA = new MemoirDBA(getApplicationContext());
	}

	public MemoirDBA getDBA() {
		return mDBA;
	}
	
	public static String getOutputMediaFile() {
		
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;

		if(useExternal) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			    
			    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Memoir");
			    if (! mediaStorageDir.exists()){
			        if (! mediaStorageDir.mkdirs()){
			            Log.d("Memoir", "failed to create directory");
			            return null;
			        }
			    }
			    
			    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		        return mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4";
			    
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
			
		}
		
	    return null;
	}

	
}
