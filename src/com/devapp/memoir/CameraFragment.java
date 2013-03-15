package com.devapp.memoir;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;


class CameraFragment extends Fragment {
	
	private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private Video mVideo = null; 

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		View rootView = inflater.inflate(R.layout.camera_activity, container, false);

        return rootView;
    }
    
    private boolean prepareVideoRecorder(){

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.lock();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // Step 4: Set output file
        long d = new Date().getTime();
        mVideo = new Video(0, d, MemoirApplication.getOutputMediaFile(getActivity()), true, 1);
        mMediaRecorder.setOutputFile(mVideo.path);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("asd", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("asd", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
        mCamera = getCameraInstance();
        

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this.getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) this.getActivity().findViewById(R.id.camera_preview);
        preview.addView(mPreview);

//        prepareVideoRecorder();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	// Add a listener to the Capture button
    	Button captureButton = (Button) this.getActivity().findViewById(R.id.button_capture);
    	captureButton.setOnClickListener(
    	    new View.OnClickListener() {
    	        @Override
    	        public void onClick(View v) {
    	        	Log.d("asd", "isREcoring > " + isRecording);
    	        	
    	            if (isRecording) {
    	                // stop recording and release camera
        	        	try {
        	                mMediaRecorder.stop();  // stop the recording
        	        	} catch(Exception e) {
        	        		Log.d("asd", "Illegal State Exception " + e);
        	        	}
    	                releaseMediaRecorder(); // release the MediaRecorder object
    	                //mCamera.lock();         // take camera access back from MediaRecorder

    	                // inform the user that recording has stopped
    	                isRecording = false;
    	                
    	                ((MemoirApplication)getActivity().getApplication()).getDBA().addVideo(mVideo);
    	                Log.d("asd", "Recording has stopped");
    	            } else {
    	                // initialize video camera
    	                if (prepareVideoRecorder()) {
    	                    // Camera is available and unlocked, MediaRecorder is prepared,
    	                    // now you can start recording
    	                    mMediaRecorder.start();

    	                    // inform the user that recording has started
    	                    isRecording = true;
    	                } else {
    	                    // prepare didn't work, release the camera
    	                    releaseMediaRecorder();
    	                    // inform user
    	                }
    	            }
    	        }
    	    }
    	);
    }
    
    @Override
	public void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
        	Log.d("asd", "2.1");

            mMediaRecorder.reset();   // clear recorder configuration
        	Log.d("asd", "2.2");
            mMediaRecorder.release(); // release the recorder object
        	Log.d("asd", "2.3");
            mMediaRecorder = null;
        	Log.d("asd", "2.4");
            mCamera.lock();           // lock camera for later use
        	Log.d("asd", "2.5");
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    
	// Check if this device has a camera 
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}	
	
	// A safe way to get an instance of the Camera object. 
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	        c.setDisplayOrientation(90);

	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    	Log.e("asd", "Camera is not available");
	    }
	    return c; // returns null if camera is unavailable
	}
	
	// A basic Camera preview class 
	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	    private SurfaceHolder mHolder;
	    private Camera mCamera;

	    public CameraPreview(Context context, Camera camera) {
	        super(context);
	        mCamera = camera;
	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        // The Surface has been created, now tell the camera where to draw the preview.
	        try {
	        	Log.d("asd", "Starting the preview");
	            mCamera.setPreviewDisplay(holder);
	            mCamera.startPreview();
	        } catch (IOException e) {
	            Log.d("asd", "Error setting camera preview: " + e.getMessage());
	        }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	        // empty. Take care of releasing the Camera preview in your activity.
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	        // If your preview can change or rotate, take care of those events here.
	        // Make sure to stop the preview before resizing or reformatting it.

	        if (mHolder.getSurface() == null){
	          // preview surface does not exist
	          return;
	        }

	        // stop preview before making changes
	        try {
	            mCamera.stopPreview();
	        } catch (Exception e){
	          // ignore: tried to stop a non-existent preview
	        }

	        // set preview size and make any resize, rotate or
	        // reformatting changes here

	        // start preview with new settings
	        try {
	            mCamera.setPreviewDisplay(mHolder);
	            mCamera.startPreview();

	        } catch (Exception e){
	            Log.d("asd", "Error starting camera preview: " + e.getMessage());
	        }
	    }
	}
}

/*

class RecorderPreview extends SurfaceView implements SurfaceHolder.Callback {
	// Create objects for MediaRecorder and SurfaceHolder.
	MediaRecorder recorder;
	SurfaceHolder holder;
	boolean mrecorderInitialized = true;
	Context context = null;

	// Create constructor of Preview Class. In this, get an object of
	// surfaceHolder class by calling getHolder() method. After that add
	// callback to the surfaceHolder. The callback will inform when surface is
	// created/changed/destroyed. Also set surface not to have its own buffers.

	public RecorderPreview(Context cxt, MediaRecorder temprecorder) {
		super(cxt);
		context = cxt;
		recorder = temprecorder;
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	// Implement the methods of SurfaceHolder.Callback interface

	// SurfaceCreated : This method gets called when surface is created.
	// In this, initialize all parameters of MediaRecorder object as explained
	// above.

	public void surfaceCreated(SurfaceHolder holder) {
		// Step 2: Set sources
		recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

		// Step 4: Set output file
		recorder.setOutputFile(MemoirApplication.getOutputMediaFile());

		recorder.setPreviewDisplay(holder.getSurface());

		try {
			recorder.prepare();
		} catch (Exception e) {
			mrecorderInitialized = false;
			String message = e.getMessage();
			Log.d("RecorderPreview", "Error message = " + message);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("RecorderPreview", "SurfaceHolder changed");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("RecorderPreview", "SurfaceHolder destroyed");
		if (recorder != null) {
			recorder.release();
			recorder = null;
		}

	}
}

public class CameraFragment extends Fragment {
	MediaRecorder mMediaRecorder;
	RecorderPreview preview;
	Camera mCamera;
	boolean start = true;
	
	private Camera getCameraInstance() {
		// A safe way to get an instance of the Camera object. 
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			Log.d("RecorderPreview", "Camera is unavailable");
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView = inflater.inflate(R.layout.camera_activity, container,
				false);

		mCamera = getCameraInstance();
		mMediaRecorder = new MediaRecorder();

		if (mCamera != null) {
			mCamera.unlock();
			mMediaRecorder.setCamera(mCamera);

			preview = new RecorderPreview(this.getActivity(), mMediaRecorder);
			FrameLayout framelayout = (FrameLayout) rootView.findViewById( R.id.camera_preview);
			framelayout.addView(preview);

			Button captureButton = (Button) rootView.findViewById( R.id.button_capture);
			captureButton.setOnClickListener(new View.OnClickListener() {
				boolean isRecording = false;

				@Override
				public void onClick(View v) {
					if (isRecording) {
						// stop recording and release camera
						mMediaRecorder.stop(); // stop the recording
						releaseMediaRecorder(); // release the MediaRecorder
												// object
						mCamera.lock(); // take camera access back from
										// MediaRecorder

						isRecording = false;
					} else {
						// initialize video camera
						if (preview.mrecorderInitialized) {
							// Camera is available and unlocked, MediaRecorder
							// is prepared,
							// now you can start recording
							mMediaRecorder.start();
						} else {
							// prepare didn't work, release the camera
							releaseMediaRecorder();
							// inform user
						}
					}
				}
			});
			
		} else
			Log.d("CameraActivity", "Camera is null");

		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it
								// first
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			if (mCamera != null)
				mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

}*/
