package com.devapp.memoir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class SecretCamera extends Service {

	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;
	private Video mVideo = null;
	private WindowManager mWindowManager = null;

	public class CamPreview extends TextureView implements
			SurfaceTextureListener {

		private Camera mCamera;

		public CamPreview(Context context, Camera camera) {
			super(context);
			mCamera = camera;
		}

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface,
				int width, int height) {

			Log.d("asd", "here2");

			Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
			Log.d("asd", "here3");
			setLayoutParams(new FrameLayout.LayoutParams(previewSize.width,
					previewSize.height, Gravity.CENTER));
			Log.d("asd", "here4");

			try {
				Log.d("asd", "here5");
				mCamera.setPreviewTexture(surface);
			} catch (IOException t) {
			}

			Log.d("asd", "here6");
			mCamera.startPreview();
			prepareVideoRecorder();
			this.setVisibility(INVISIBLE); // Make the surface invisible as soon
											// as it is created
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
				int width, int height) {
			// Put code here to handle texture size change if you want to
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			// Update your view here!
		}
	}

	public class CamCallback implements Camera.PreviewCallback {
		public void onPreviewFrame(byte[] data, Camera camera) {
			// Process the camera data here

			Log.d("asd", "Here10");
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d("asd", "Here1");
		// Setup the camera and the preview object
		
//		mCamera = getCameraInstance();
//		CamPreview camPreview = new CamPreview(this, mCamera);
//		camPreview.setSurfaceTextureListener(camPreview); 
		Log.d("asd","Here7");
		
		//camPreview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		 
		 // Connect the preview object to a FrameLayout in your UI 
		 // You'll have to create a FrameLayout object in your UI to place this preview in 
		 //FrameLayout preview = (FrameLayout)findViewById(R.id.cameraView); 
		 //preview.addView(camPreview);

//		mWindowManager = (WindowManager) this
//				.getSystemService(Context.WINDOW_SERVICE);
//		LayoutParams params = new WindowManager.LayoutParams(
//				WindowManager.LayoutParams.WRAP_CONTENT,
//				WindowManager.LayoutParams.WRAP_CONTENT,
//				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//				PixelFormat.TRANSLUCENT);
		
//		mWindowManager.addView(camPreview, params);

		 Log.d("asd", "Here8"); 
		 // Attach a callback for preview 
//		 CamCallback camCallback = new CamCallback();
//		 mCamera.setPreviewCallback(camCallback);
		 
		Log.d("asd", "Here9");

		
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this.getApplicationContext() , mCamera);

		mWindowManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
/*		LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);*/
		LayoutParams params = new WindowManager.LayoutParams(
				1,
				1,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);

		mPreview.setZOrderOnTop(true);
		mPreview.mHolder.setFormat(PixelFormat.TRANSPARENT);
		mWindowManager.addView(mPreview, params);
		
		// FrameLayout preview = (FrameLayout) this
		// .findViewById(R.id.camera_preview);
		// preview.addView(mPreview);
	}

	private boolean prepareVideoRecorder() {

		mMediaRecorder = new MediaRecorder();

		mCamera.lock();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		//mCamera.enableShutterSound(false);

		AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	    mgr.setStreamMute(AudioManager.STREAM_MUSIC, true);
		
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));

		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
		long d = Long.parseLong(ft.format(new Date()));

		// mVideo = new Video(0, d, MemoirApplication.getOutputMediaFile(this),
		// false, 2);
		mMediaRecorder
				.setOutputFile("/storage/emulated/0/Movies/Memoir/temp.mp4");

		//mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
		mMediaRecorder.setMaxDuration(2000);
		mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
					Log.d("asd","Calling stop recording :) ");
					stopRecording();
				}
			}
		});

		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d("asd",
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d("asd",
					"IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	public void startRecording() {
		if (prepareVideoRecorder()) {
			mMediaRecorder.start();
		} else {
			releaseMediaRecorder();
		}
	}

	public void stopRecording() {
		try {
			mMediaRecorder.stop();
		} catch (Exception e) {
			Log.d("asd", "Illegal State Exception " + e);
		}
		releaseMediaRecorder();
		releaseCamera();
		mWindowManager.removeView(mPreview);
		AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	    mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);

		// ((MemoirApplication) getApplication()).getDBA().addVideo(
		// mVideo);
		// ((MemoirApplication) getApplication()).getDBA().selectVideo(mVideo);
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			// mCamera.lock();
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			Log.e("asd", "Camera is not available");
		}
		return c;
	}
	
	public class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private Camera mCamera;
		boolean previewing = false;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			mCamera = camera;
			mHolder = getHolder();
			mHolder.addCallback(this);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.d("asd", "On surfaceCreated called");
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			
			
			Log.d("asd", "On surface changed called");
			try {
				if ((mCamera != null) && (previewing == false)) {
					mCamera.setPreviewDisplay(holder);
					mCamera.startPreview();
					startRecording();
					previewing = true;
				}
			} catch (Exception e) {
				Log.d("asd", "Error starting camera preview: " + e.getMessage());
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("asd", "Recording 2 secnods :p");
		
		Log.d("asd", "10");

		return START_NOT_STICKY;
	}

}

/*
 * public class SecretCamera extends Service {
 * 
 * private Camera mCamera; private MediaRecorder mMediaRecorder; // private
 * Video mVideo = null;
 * 
 * @Override public void onCreate() { super.onCreate(); mCamera =
 * getCameraInstance();
 * 
 * record();
 * 
 * }
 * 
 * @Override public IBinder onBind(Intent arg0) { return null; }
 * 
 * // @Override // public int onStartCommand(Intent intent, int flags, int
 * startId) { // Log.d("asd", "Recording 2 secnods :p"); // record(); //
 * Log.d("asd", "10");
 * 
 * // Log.d("asd", "11"); // Log.d("asd", "12"); // return START_NOT_STICKY; //
 * }
 * 
 * private boolean prepareVideoRecorder() { Log.d("asd", "1"); mMediaRecorder =
 * new MediaRecorder();
 * 
 * Log.d("asd", "2"); mCamera.lock(); mCamera.unlock(); Log.d("asd", "3");
 * mMediaRecorder.setCamera(mCamera); Log.d("asd", "4");
 * 
 * Log.d("asd", "5");
 * mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 * mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
 * Log.d("asd", "6"); mMediaRecorder.setProfile(CamcorderProfile
 * .get(CamcorderProfile.QUALITY_HIGH));
 * 
 * Log.d("asd", "7"); SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
 * long d = Long.parseLong(ft.format(new Date()));
 * 
 * // mVideo = new Video(0, d, MemoirApplication.getOutputMediaFile(this), //
 * false, 2);
 * mMediaRecorder.setOutputFile("/storage/emulated/0/Movies/Memoir/temp.mp4");
 * 
 * // mMediaRecorder.setMaxDuration(2000); mMediaRecorder.setOnInfoListener(new
 * MediaRecorder.OnInfoListener() {
 * 
 * @Override public void onInfo(MediaRecorder mr, int what, int extra) {
 * 
 * Log.d("asd", "10"); if (what ==
 * MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) { Log.d("asd", "11");
 * stop(); } } });
 * 
 * try { Log.d("asd", "8");
 * 
 * mMediaRecorder.prepare(); } catch (IllegalStateException e) { Log.d("asd",
 * "IllegalStateException preparing MediaRecorder: " + e.getMessage());
 * releaseMediaRecorder(); return false; } catch (IOException e) { Log.d("asd",
 * "IOException preparing MediaRecorder: " + e.getMessage());
 * releaseMediaRecorder(); return false; } Log.d("asd", "9"); return true; }
 * 
 * public void record() { if (prepareVideoRecorder()) { Log.d("asd", "9.1");
 * mMediaRecorder.start(); Log.d("asd", "9.2"); } else { releaseMediaRecorder();
 * } }
 * 
 * public void stop() { try { Log.d("asd", "12"); mMediaRecorder.stop(); } catch
 * (Exception e) { Log.d("asd", "Illegal State Exception " + e); }
 * releaseMediaRecorder(); releaseCamera();
 * 
 * // ((MemoirApplication) getApplication()).getDBA().addVideo(mVideo); //
 * ((MemoirApplication) getApplication()).getDBA().selectVideo(mVideo);
 * Log.d("asd", "Recording has stopped"); }
 * 
 * private void releaseMediaRecorder() { if (mMediaRecorder != null) {
 * mMediaRecorder.reset(); mMediaRecorder.release(); mMediaRecorder = null; //
 * mCamera.lock(); } }
 * 
 * private void releaseCamera() { if (mCamera != null) { mCamera.stopPreview();
 * mCamera.release(); mCamera = null; } }
 * 
 * private boolean checkCameraHardware(Context context) { if
 * (context.getPackageManager().hasSystemFeature(
 * PackageManager.FEATURE_CAMERA)) { return true; } else { return false; } }
 * 
 * public static Camera getCameraInstance() { Camera c = null; try { c =
 * Camera.open(); } catch (Exception e) { Log.e("asd",
 * "Camera is not available"); } return c; } }
 */
