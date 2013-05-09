package com.example.importvideo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.VideoView;

public class ImportVideoActivity extends Activity {

	private FrameLayout mFrameLayout1 = null, mFrameLayout2 = null;
	private VideoView mVideoView = null;
	private int mHeight = 0, mWidth = 0;
	

	@SuppressLint("NewApi") 
	private void getDisplay() {
		/** Note : For getting the height and width of the screen */
		if (android.os.Build.VERSION.SDK_INT >= 14
				&& android.os.Build.VERSION.SDK_INT <= 16) {
			Display display = this.getWindowManager()
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
			Display display = this.getWindowManager()
					.getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getRealMetrics(outMetrics);
			mHeight = outMetrics.heightPixels;
			mWidth = outMetrics.widthPixels;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("asd", "Inside on create");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getDisplay();

		setContentView(R.layout.activity_import_video);
		mFrameLayout1 = (FrameLayout)findViewById(R.id.ImportViewFL1);
		mFrameLayout2 = (FrameLayout)findViewById(R.id.ImportViewFL2);
		mVideoView = (VideoView) findViewById(R.id.ImportViewVV1);
		Log.d("asd", "(mHeight*4/5) >" + (mHeight*4/5) + "  (mHeight*1/5)> " + (mHeight*1/5) + "  (mWidth*4/5)" + (mWidth*4/5));
		mFrameLayout1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int)(mHeight*5/6)));
		mFrameLayout2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int)(mHeight*1/6)));
		mVideoView.setLayoutParams(new FrameLayout.LayoutParams((int)(mWidth*5/6),LayoutParams.MATCH_PARENT, Gravity.CENTER));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();

		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
			}
		});

		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer vmp) {
				Log.d("qwe", "On Completion listener");
			}
		});
		
		mVideoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				Log.e("asd", "Cant Play This Video :(");
				return true;
			}
			
		});
		
		mVideoView.setVideoPath("/storage/emulated/legacy/DCIM/Camera/VID_20130510_150310.mp4");
		mVideoView.requestFocus();
//		mVideoView.start();
		
		mVideoView.seekTo(4000);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
