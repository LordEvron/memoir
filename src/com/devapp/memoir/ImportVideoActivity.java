package com.devapp.memoir;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.VideoView;

import com.devapp.memoir.services.TranscodingService;


public class ImportVideoActivity extends Activity implements OnPreparedListener {

	private FrameLayout mFrameLayoutVV = null;
	private RelativeLayout mRelativeLayoutScroll = null;
	private LinearLayout mLinearLayoutContainer = null;
	private VideoView mVideoView = null;
	private ImageView mImageViewPlay = null;
	private SeekBar mSeekBar = null;
	private float mdistanceToTimeRatio = 0, mDuration = 0;
	private int mVideoWidth = 0, mVideoHeight = 0;
	private int mWidth = 0, mHeight = 0;
	private int mPosition = 0;
	private String mPath = null, mVideoDate = null;
	private static int VIDEO_IMPORT_FROM_GALLERY = 0;
	private TranscodingServiceBroadcastReceiver mDataBroadcastReceiver = null;
	private MediaMetadataRetriever mMediaRetriever = null;
	private SharedPreferences mPrefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("asd", "Start of onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mPrefs = this.getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);
		Log.d("asd", "End of onCreate");
	}

	@SuppressLint("NewApi")
	public void setDisplayMatrix() {
		/** Note : For getting the height and width of the screen */
		Display display = getWindowManager()
				.getDefaultDisplay();

		if (android.os.Build.VERSION.SDK_INT >= 14
				&& android.os.Build.VERSION.SDK_INT <= 16) {
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
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getRealMetrics(outMetrics);
			mHeight = outMetrics.heightPixels;
			mWidth = outMetrics.widthPixels;
		}
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("asd", "Start of onActivityResult");

		if (requestCode == VIDEO_IMPORT_FROM_GALLERY && resultCode == RESULT_OK) {
			Uri selectedVideoLocation = data.getData();
			mPath = MemoirApplication.getFilePathFromContentUri(
					selectedVideoLocation, getContentResolver());
			
			if(mPath == null) {
				Toast.makeText(
						this,
						"This video can not be imported as it is not local on the phone, Please select another video",
						Toast.LENGTH_LONG).show();
				return;
			}

			mMediaRetriever = new MediaMetadataRetriever();
			mMediaRetriever.setDataSource(this, selectedVideoLocation);
			mVideoWidth = Integer
					.parseInt(mMediaRetriever
							.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
			mVideoHeight = Integer
					.parseInt(mMediaRetriever
							.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
			
			/** Note: Retrieving video date from Content URI is more acurate than from MetadataRetriever*/
			mVideoDate = MemoirApplication.getDateFromContentUri(selectedVideoLocation, getContentResolver());
			if(mVideoDate == null) {
				mVideoDate = mMediaRetriever.extractMetadata(
						MediaMetadataRetriever.METADATA_KEY_DATE).substring(0, 8);
			}

			mDuration = Float
					.parseFloat(mMediaRetriever
							.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;

			setContentView(R.layout.activity_import_video);
			mFrameLayoutVV = (FrameLayout) findViewById(R.id.ImportVideoFL1);
			mRelativeLayoutScroll = (RelativeLayout) findViewById(R.id.ImportVideoRL1);
			mVideoView = (VideoView) findViewById(R.id.ImportVideoVV1);

			mLinearLayoutContainer = (LinearLayout) findViewById(R.id.ImportVideoLLContainer);
			mImageViewPlay = (ImageView) findViewById(R.id.ImportVideoIVPlay);
			mSeekBar = (SeekBar) findViewById(R.id.ImportVideoSB);

			//Log.d("asd", "(mHeight*5/6) >" + (mHeight * 5 / 6)
			//		+ "  (mHeight*1/6)> " + (mHeight * 1 / 6)
			//		+ "  (mWidth*5/6)" + (mWidth * 5 / 6));
			
			setDisplayMatrix();
			mFrameLayoutVV.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, (int) (mWidth * 5 / 6)));
			mRelativeLayoutScroll
					.setLayoutParams(new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT, (int) (mWidth * 1 / 6)));
			mVideoView.setLayoutParams(new FrameLayout.LayoutParams(
					(int) (mHeight * 5 / 6), LayoutParams.MATCH_PARENT,
					Gravity.CENTER));
		} else {
			mPath = null;
			finish();
		}
		Log.d("asd", "End of onActivityResult");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d("asd", "Start of onStart");
		if (mPath == null) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("video/*");
			startActivityForResult(intent, VIDEO_IMPORT_FROM_GALLERY);
		} else {
			mVideoView.setVideoPath(mPath);
			mVideoView.requestFocus();

			mVideoView.setOnPreparedListener(this);

			/*mVideoView
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer vmp) {
							Log.d("qwe", "On Completion listener");
						}
					});*/

			mVideoView.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
					Log.e("asd", "Cant Play This Video :(");
					return true;
				}

			});

			mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar view, int position,
						boolean arg2) {
					mVideoView.seekTo(position * 100);
					mPosition = position;
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}
			});

			mImageViewPlay.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					mImageViewPlay.setVisibility(View.INVISIBLE);
					mVideoView.start();
					mVideoView.postDelayed(new Runnable() {

						@Override
						public void run() {
							mVideoView.pause();
							mVideoView.seekTo(mPosition * 100);
							mImageViewPlay.setVisibility(View.VISIBLE);
						}
					}, mPrefs.getInt("com.devapp.memoir.noofseconds",1) * 1000);
				}
			});

			((ImageView) findViewById(R.id.ImportVideoIVLeft))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							mSeekBar.setProgress(mSeekBar.getProgress() - 1);
						}
					});

			((ImageView) findViewById(R.id.ImportVideoIVRight))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							mSeekBar.setProgress(mSeekBar.getProgress() + 1);
						}
					});
			((ImageView) findViewById(R.id.ImportVideoIVSelect))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							Intent intent = new Intent(
									ImportVideoActivity.this,
									TranscodingService.class);
							intent.setAction(TranscodingService.ActionTrimVideo);
							intent.putExtra("filePath", mPath);
							intent.putExtra("startTime", (float) mPosition / 10);
							intent.putExtra(
									"endTime",
									(float) ((float) ((float) mPosition + (mPrefs
											.getInt("com.devapp.memoir.noofseconds",
													1) * 10.0)) / 10.0));
							intent.putExtra(
									"outputFilePath",
									MemoirApplication
											.getOutputMediaFile(ImportVideoActivity.this));
							startService(intent);
						}
					});

			((ImageView) findViewById(R.id.ImportVideoIVCancel))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							mPath = null;
							finish();
						}
					});
		}
		Log.d("asd", "end of onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("asd", "start of onResume");

		if (mDataBroadcastReceiver == null)
			mDataBroadcastReceiver = new TranscodingServiceBroadcastReceiver();

		IntentFilter intentFilter = new IntentFilter(
				TranscodingService.ActionTrimVideo);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mDataBroadcastReceiver, intentFilter);
		Log.d("asd", "end of onResume");
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		Log.d("asd", "start of onPrepared");

		int containerImageHeight = mLinearLayoutContainer.getHeight();
		int containerImageWidth = containerImageHeight * mVideoWidth
				/ mVideoHeight;
		int containerWidth = mLinearLayoutContainer.getWidth();
		int containerHeight = mLinearLayoutContainer.getHeight();
		double noOfFrames = Math.floor(containerWidth / containerImageWidth);
		float secondInterval = containerImageWidth * mDuration / containerWidth;

/*		Log.d("asd", "videoWidth>" + mVideoWidth + "  videoHeight>"
				+ mVideoHeight + "  containerImageHeight"
				+ containerImageHeight);
		Log.d("asd", "containerImageWidth>" + containerImageWidth
				+ "   mLinearLayoutContainer.getWidth()"
				+ mLinearLayoutContainer.getWidth());
		Log.d("asd", "noOfFrames >" + noOfFrames);
		Log.d("asd", "New FrameLayout width should be" + noOfFrames
				* containerImageWidth);
		Log.d("asd", "Duration of Video in milliseconds is >" + mDuration);
*/
		mdistanceToTimeRatio = mLinearLayoutContainer.getWidth() / mDuration;

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				containerImageWidth, containerImageHeight);

		int i = 0;
		for (i = 0; i < noOfFrames; i++) {
			ImageView iv = new ImageView(this);
			mLinearLayoutContainer.addView(iv, params);
			new getFrameTask().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR,(new FrameIVStruct(Math.round(i * secondInterval * 1000000), iv)));
		}

		mLinearLayoutContainer.setLayoutParams(new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		//Log.d("asd", "mLinearLayoutContainer.getWidth()"
		//		+ mLinearLayoutContainer.getWidth());

		//Log.d("asd", "mdistanceToTimeRatio" + mdistanceToTimeRatio);
		Bitmap bm = Bitmap.createBitmap(
				Math.round(mdistanceToTimeRatio
						* mPrefs.getInt("com.devapp.memoir.noofseconds", 1)),
				containerHeight, Bitmap.Config.ARGB_8888);
		new Canvas(bm).drawColor(getResources().getColor(
				R.color.selectTransparentBlue));
		Drawable drawable = new BitmapDrawable(getResources(), bm);
		mSeekBar.setThumb(drawable);
		mSeekBar.setThumbOffset((int) mdistanceToTimeRatio);
		mSeekBar.setPadding((int) mdistanceToTimeRatio, 0,
				-(int) mdistanceToTimeRatio, 0);
		mSeekBar.setLayoutParams(new FrameLayout.LayoutParams(
				(int) (noOfFrames * containerImageWidth),
				LayoutParams.MATCH_PARENT));
		mSeekBar.setMax((int) Math.floor(mDuration * 10));
		Log.d("asd", "end of onPrepared");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mDataBroadcastReceiver != null)
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					mDataBroadcastReceiver);
	}

	public class TranscodingServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("OutputFileName")) {
				String outputFile = intent.getStringExtra("OutputFileName");
				intent.putExtra("videoDate", mVideoDate);
				if (!outputFile.isEmpty()) {
					if (getParent() == null) {
						ImportVideoActivity.this.setResult(Activity.RESULT_OK,
								intent);
					} else {
						getParent().setResult(Activity.RESULT_OK, intent);
					}
				} else {
					ImportVideoActivity.this.setResult(
							Activity.RESULT_CANCELED, null);
				}
			} else {
				ImportVideoActivity.this.setResult(Activity.RESULT_CANCELED,
						null);
			}
			mPath = null;
			finish();
		}
	}
	
	public class FrameIVStruct {
		int frameAt = 0;
		ImageView iv = null;
		Bitmap b = null;
		
		FrameIVStruct(int fa, ImageView iv) {
			this.frameAt = fa;
			this.iv = iv;
		}
	}
	
	public class getFrameTask extends AsyncTask<FrameIVStruct, Void, FrameIVStruct> {
		
		@Override
		protected FrameIVStruct doInBackground(FrameIVStruct... arg0) {
			FrameIVStruct struct = arg0[0];
			struct.b = mMediaRetriever.getFrameAtTime(struct.frameAt);
			return struct;
		}

		@Override
		protected void onPostExecute(FrameIVStruct result) {
			if (result != null) {
				result.iv.setImageBitmap(result.b);
			}
			super.onPostExecute(result);
		}
		
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
