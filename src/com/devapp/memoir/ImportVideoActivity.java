package com.devapp.memoir;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.devapp.memoir.MyLifeFragment.TranscodingServiceBroadcastReceiver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
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
import android.widget.VideoView;

public class ImportVideoActivity extends Activity implements OnPreparedListener {

	private FrameLayout mFrameLayoutVV = null, mFrameLayoutScroll = null;
	private RelativeLayout mRelativeLayoutScroll = null;
	private LinearLayout mLinearLayoutContainer = null;
	private VideoView mVideoView = null;
	private ImageView mImageViewPlay = null;
	private SeekBar mSeekBar = null;
	private float mdistanceToTimeRatio = 0, mDuration = 0;
	private int mHeight = 0, mWidth = 0, mVideoWidth = 0, mVideoHeight = 0;
	private int mPosition = 0;
	private String mPath = null, mVideoDate = null;
	private static int VIDEO_IMPORT_FROM_GALLERY = 0;
	private TranscodingServiceBroadcastReceiver mDataBroadcastReceiver = null;
	private MediaMetadataRetriever mMediaRetriever = null;

	@SuppressLint("NewApi")
	private void getDisplay() {
		/** Note : For getting the height and width of the screen */
		if (android.os.Build.VERSION.SDK_INT >= 14
				&& android.os.Build.VERSION.SDK_INT <= 16) {
			Display display = this.getWindowManager().getDefaultDisplay();
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
			Display display = this.getWindowManager().getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getRealMetrics(outMetrics);
			mHeight = outMetrics.heightPixels;
			mWidth = outMetrics.widthPixels;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("asd", "onCreate");

		Log.d("asd", "Inside on create");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getDisplay();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("asd", "onActivityResult");
		if (requestCode == VIDEO_IMPORT_FROM_GALLERY && resultCode == RESULT_OK) {
			Uri selectedVideoLocation = data.getData();
			mPath = MemoirApplication.getFilePathFromContentUri(
					selectedVideoLocation, getContentResolver());
			
			mMediaRetriever = new MediaMetadataRetriever();
			mMediaRetriever.setDataSource(this, selectedVideoLocation);
			mVideoWidth = Integer
					.parseInt(mMediaRetriever
							.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
			mVideoHeight = Integer
					.parseInt(mMediaRetriever
							.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
			mVideoDate = mMediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE).substring(0, 8);
			
			Log.d("asd", "Video date is > " + mVideoDate);
			mDuration = Float.parseFloat(mMediaRetriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;

			setContentView(R.layout.activity_import_video);
			mFrameLayoutVV = (FrameLayout) findViewById(R.id.ImportVideoFL1);
			mRelativeLayoutScroll = (RelativeLayout) findViewById(R.id.ImportVideoRL1);
			mVideoView = (VideoView) findViewById(R.id.ImportVideoVV1);

			mFrameLayoutScroll = (FrameLayout) findViewById(R.id.ImportVideoFL);
			mLinearLayoutContainer = (LinearLayout) findViewById(R.id.ImportVideoLLContainer);
			mImageViewPlay = (ImageView) findViewById(R.id.ImportVideoIVPlay);
			mSeekBar = (SeekBar) findViewById(R.id.ImportVideoSB);

			Log.d("asd", "(mHeight*5/6) >" + (mHeight * 5 / 6)
					+ "  (mHeight*1/6)> " + (mHeight * 1 / 6)
					+ "  (mWidth*5/6)" + (mWidth * 5 / 6));
			mFrameLayoutVV.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, (int) (mHeight * 5 / 6)));
			mRelativeLayoutScroll
					.setLayoutParams(new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT, (int) (mHeight * 1 / 6)));
			mVideoView.setLayoutParams(new FrameLayout.LayoutParams(
					(int) (mWidth * 5 / 6), LayoutParams.MATCH_PARENT,
					Gravity.CENTER));
		} else {
			mPath = null;
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d("asd", "onStart");
		if (mPath == null) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("video/*");
			startActivityForResult(intent, VIDEO_IMPORT_FROM_GALLERY);
		} else {
			mVideoView.setVideoPath(mPath);
			mVideoView.requestFocus();

			mVideoView.setOnPreparedListener(this);

			mVideoView
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

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

			mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar view, int position,
						boolean arg2) {
					Log.d("asd", "Seek bar value >" + position);
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
					}, 2000);
				}
			});

			/*
			 * mSeekBar.setOnTouchListener(new OnTouchListener() {
			 * 
			 * @Override public boolean onTouch(View v, MotionEvent event) {
			 * if(event.getAction() == MotionEvent.ACTION_MOVE) {
			 * mChangedPosition = true;
			 * mSeekBar.setProgress(mSeekBar.getProgress()); return false; }
			 * else if (event.getAction() == MotionEvent.ACTION_UP) {
			 * if(!mChangedPosition) { mVideoView.start();
			 * mVideoView.postDelayed(new Runnable() {
			 * 
			 * @Override public void run() { mVideoView.pause(); } }, 2000);
			 * mChangedPosition = false; } } else if (event.getAction() ==
			 * MotionEvent.ACTION_DOWN) { mChangedPosition = false; } return
			 * false; } });
			 */

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
							Log.d("asd", "mPosition >" + mPosition);
							intent.putExtra("startTime", (float) mPosition/10);
							intent.putExtra("endTime", (float) ((float) ((float)mPosition + 20.0)/10.0));
							intent.putExtra("outputFilePath", MemoirApplication.getOutputMediaFile(ImportVideoActivity.this));
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
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mDataBroadcastReceiver == null)
			mDataBroadcastReceiver = new TranscodingServiceBroadcastReceiver();

		IntentFilter intentFilter = new IntentFilter(TranscodingService.ActionTrimVideo);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mDataBroadcastReceiver, intentFilter);
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		Log.d("asd", "onPrepared");

		int containerImageHeight = mLinearLayoutContainer.getHeight();
		int containerImageWidth = containerImageHeight * mVideoWidth
				/ mVideoHeight;
		int containerWidth = mLinearLayoutContainer.getWidth();
		int containerHeight = mLinearLayoutContainer.getHeight();
		double noOfFrames = Math.floor(containerWidth / containerImageWidth);
		float secondInterval = containerImageWidth * mDuration / containerWidth;

		Log.d("asd", "videoWidth>" + mVideoWidth + "  videoHeight>"
				+ mVideoHeight + "  containerImageHeight" + containerImageHeight);
		Log.d("asd", "containerImageWidth>" + containerImageWidth
				+ "   mLinearLayoutContainer.getWidth()"
				+ mLinearLayoutContainer.getWidth());
		Log.d("asd", "noOfFrames >" + noOfFrames);
		Log.d("asd", "New FrameLayout width should be" + noOfFrames
				* containerImageWidth);
		Log.d("asd", "Duration of Video in milliseconds is >" + mDuration);

		mdistanceToTimeRatio = mLinearLayoutContainer.getWidth() / mDuration;

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				containerImageWidth, containerImageHeight);

		int i = 0;
		for (i = 0; i < noOfFrames; i++) {
			ImageView iv = new ImageView(this);
			Bitmap b = mMediaRetriever.getFrameAtTime(Math.round(i * secondInterval
					* 1000000));
			if (b != null) {
				iv.setImageBitmap(b);
				mLinearLayoutContainer.addView(iv, 0, params);
			}
		}

		mLinearLayoutContainer.setLayoutParams(new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		Log.d("asd", "mLinearLayoutContainer.getWidth()"
				+ mLinearLayoutContainer.getWidth());

		Log.d("asd", "mdistanceToTimeRatio" + mdistanceToTimeRatio);
		Bitmap bm = Bitmap.createBitmap(Math.round(mdistanceToTimeRatio * 2),
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
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mDataBroadcastReceiver != null)
			LocalBroadcastManager.getInstance(this)
					.unregisterReceiver(mDataBroadcastReceiver);
	}

	public class TranscodingServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("qwe", "OnReceive :) ");
			if (intent.hasExtra("OutputFileName")) {
				String outputFile = intent.getStringExtra("OutputFileName");
				intent.putExtra("videoDate", mVideoDate);
				if (!outputFile.isEmpty()) {
					if (getParent() == null) {
					    ImportVideoActivity.this.setResult(Activity.RESULT_OK, intent);
					} else {
					    getParent().setResult(Activity.RESULT_OK, intent);
					}
				} else {
					ImportVideoActivity.this.setResult(Activity.RESULT_CANCELED, null);
				}
			} else {
				ImportVideoActivity.this.setResult(Activity.RESULT_CANCELED, null);
			}
			mPath = null;
			finish();
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
