package com.devapp.memoir;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

// TODO : Add orientation support right now if the phone is held in right hand the videos would be 180 degree reversed.
// Remove the camera sound 
// Add a notification to the user that video is recorded and he can come and see it 

public class SecretCamera extends Service {

	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;
	private Video mVideo = null;
	private WindowManager mWindowManager = null;

	@Override
	public void onCreate() {
		super.onCreate();

		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this.getApplicationContext(), mCamera);

		mWindowManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutParams params = new WindowManager.LayoutParams(1, 1,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);

		mPreview.setZOrderOnTop(true);
		mPreview.mHolder.setFormat(PixelFormat.TRANSPARENT);
		mWindowManager.addView(mPreview, params);
	}

	private boolean prepareVideoRecorder() {

		mMediaRecorder = new MediaRecorder();

		mCamera.lock();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		//mCamera.setDisplayOrientation(180);
		// mCamera.enableShutterSound(false);

		AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mgr.setStreamMute(AudioManager.STREAM_MUSIC, true);

		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));

		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
		long d = Long.parseLong(ft.format(new Date()));

		mVideo = new Video(0, d, MemoirApplication.getOutputMediaFile(this),
				false, 2);
		mMediaRecorder
				.setOutputFile(mVideo.path);

		mMediaRecorder.setMaxDuration(2000);
		mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
					Log.d("asd", "Calling stop recording :) ");
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
		AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);

		((MemoirApplication) getApplication()).getDBA().addVideo(mVideo);
		((MemoirApplication) getApplication()).getDBA().selectVideo(mVideo);
		
		SharedPreferences mPrefs = this.getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);
		mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", true)
		.commit();
		stopSelf();
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
		return START_NOT_STICKY;
	}
}
