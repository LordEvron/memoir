package com.devapp.memoir;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends Activity {

	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;
	private boolean isRecording = false;
	private Video mVideo = null;
	Button captureButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.camera_activity);

		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) this
				.findViewById(R.id.camera_preview);
		preview.addView(mPreview);
	}

	private boolean prepareVideoRecorder() {

		mMediaRecorder = new MediaRecorder();

		mCamera.lock();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);

		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));

		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
		long d = Long.parseLong(ft.format(new Date()));

		mVideo = new Video(0, d, MemoirApplication.getOutputMediaFile(this),
				false, 2, false);
		mMediaRecorder.setOutputFile(mVideo.path);

		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
		mMediaRecorder.setMaxDuration(2000); 
		mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
					captureButton.performClick();
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

	@Override
	public void onStart() {
		super.onStart();
		captureButton = (Button) this.findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("asd", "isREcoring > " + isRecording);

				if (isRecording) {
					try {
						mMediaRecorder.stop();
					} catch (Exception e) {
						Log.d("asd", "Illegal State Exception " + e);
					}
					releaseMediaRecorder();
					isRecording = false;
					((MemoirApplication) getApplication()).getDBA().addVideo(
							mVideo);
					((MemoirApplication) getApplication()).getDBA().selectVideo(mVideo);
					Log.d("asd", "Recording has stopped");
				} else {
					if (prepareVideoRecorder()) {

						/*new CountDownTimer(3000, 1000) {

							public void onTick(long millisUntilFinished) {
								captureButton.setText(String
										.valueOf(millisUntilFinished / 1000));
							}

							public void onFinish() {
								Toast toast1 = Toast.makeText(getApplicationContext(),
										"Video Recorded successfully",
										Toast.LENGTH_SHORT);
								toast1.show();
							}
						}.start();*/
						

						mMediaRecorder.start();
						isRecording = true;
					} else {
						releaseMediaRecorder();
					}
				}
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		releaseMediaRecorder();
		releaseCamera();
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
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			try {
				if ((mCamera != null) && (previewing == false)) {
					mCamera.setPreviewDisplay(holder);
					mCamera.startPreview();
					previewing = true;
				}
			} catch (Exception e) {
				Log.d("asd", "Error starting camera preview: " + e.getMessage());
			}
		}
	}
}
