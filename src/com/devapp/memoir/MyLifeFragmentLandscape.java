package com.devapp.memoir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class MyLifeFragmentLandscape extends MyLifeFragment {

	private SeekBar mDrawer = null;
	private FrameLayout mMyLifeContainerFL = null, mMyLifeFL = null;
	private RelativeLayout mMyLifeDrawerContainerRL = null;
	private int gAdsHeight = 0;
	private ListView mMyLifeLV = null;
	private int mDrawerContainerOrigWidth = 0, mDrawerContainerWidth = 0, mGlobalDrawerContainerWidth = 0;
	private float mWidthInc = 0;
	private int originalProgress = 0, startProgress = 0;
	private boolean isExpanded = false;
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d("asd", "On Activity Created of Landscape");
		super.onActivityCreated(savedInstanceState);
		Activity activity = this.getActivity();

		mMyLifeFL = (FrameLayout) activity.findViewById(R.id.MyLifeFL);
		mMyLifeContainerFL = (FrameLayout) activity.findViewById(R.id.MyLifeContainerFL);
		mMyLifeLV = (ListView) activity.findViewById(R.id.MyLifeDateLV);
		mMyLifeDrawerContainerRL = (RelativeLayout)activity.findViewById(R.id.MyLifeDrawerContainer);
		
		ImageView myLifeDrawerIV = (ImageView)activity.findViewById(R.id.MyLifeDrawerIV);
		myLifeDrawerIV.setImageResource(R.drawable.drawer);
		myLifeDrawerIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if(isExpanded) {
					isExpanded = false;
					mMyLifeDrawerContainerRL.setLayoutParams(new FrameLayout.LayoutParams(
							mDrawerContainerOrigWidth, LayoutParams.MATCH_PARENT, Gravity.RIGHT));
					((ImageView)view).setImageResource(R.drawable.drawer);
				} else {
					isExpanded = true;
					mMyLifeDrawerContainerRL.setLayoutParams(new FrameLayout.LayoutParams(
							mDrawerContainerWidth, LayoutParams.MATCH_PARENT, Gravity.RIGHT));
					((ImageView)view).setImageResource(R.drawable.drawerreverse);
				}
			}
		});
		
/*		mDrawer = (SeekBar) activity.findViewById(R.id.MyLifeSB);
		mDrawer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int position, boolean fromTouch) {
				Log.d("asd", "Position is " + position + " Incrementing by " + (100-position)*mWidthInc);
	            if(fromTouch) {
					if ((position > (originalProgress + 10))
							|| (position < (originalProgress - 10))) {
						seekBar.setProgress(originalProgress);
					} else {
						if(startProgress > (position + 10) || startProgress < (position - 10)) {
							startProgress = -1;
						}
						originalProgress = position;
						mMyLifeLV.setLayoutParams(new FrameLayout.LayoutParams(
								(int) (listViewWidth + (100-position)*mWidthInc) , LayoutParams.MATCH_PARENT, Gravity.RIGHT));
					}
				}	  
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//Log.d("asd", "Inside onStartTrackingTouch");
				startProgress = originalProgress = seekBar.getProgress();
				Log.d("asd", "setting start progress to " + startProgress);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d("asd", "startProgress > " + startProgress + "  currentProgress" + seekBar.getProgress());
				if(startProgress != -1 && seekBar.getProgress() < (startProgress + 5) && seekBar.getProgress() > startProgress - 5) {
					Log.d("asd", "Assume Click");
					if(startProgress <= 50) {
						startProgress = 100;
					} else {
						startProgress = 0;
					}
					seekBar.setProgress(startProgress);
					mMyLifeLV.setLayoutParams(new FrameLayout.LayoutParams(
							(int) (listViewWidth + (100 - startProgress)*mWidthInc) , LayoutParams.MATCH_PARENT, Gravity.RIGHT));
				}
				startProgress = -1;
			}
			
		});*/
		
		gAdsHeight = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 32,
						getResources().getDisplayMetrics());

		mDrawerContainerWidth = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 480,
				getResources().getDisplayMetrics());
		Log.d("asd", "Drawer width is supposed to be in terms of 450dp > " + mDrawerContainerWidth);
		
		ViewTreeObserver vto = mMyLifeFL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressLint("NewApi") @Override
			public void onGlobalLayout() {
				
				draw();
				ViewTreeObserver vto = mMyLifeFL.getViewTreeObserver();

				if (android.os.Build.VERSION.SDK_INT >= 14
						&& android.os.Build.VERSION.SDK_INT <= 16) {
					vto.removeGlobalOnLayoutListener(this);
				} else if(android.os.Build.VERSION.SDK_INT >= 17) {
					vto.removeOnGlobalLayoutListener(this);
				}
			}
		});
		
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d("asd", "OnPrepared in Landscape got called");
				draw();
				MyLifeFragmentLandscape.this.onVideoViewPrepared();
			}
		});
	}
	
	public void draw() {
		setDisplayMatrix();
		
		int dp20px = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 20,
				getResources().getDisplayMetrics());
		
		int newHeight = mMyLifeContainerFL.getHeight();
//		int newHeight = mMyLifeContainerFL.getHeight() - gAdsHeight;
		mMyLifeContainerFL.setLayoutParams(new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, newHeight));
		
		Log.d("asd", "Video view width >" + newHeight * mWidth / mHeight);
		mMyLifeFL.setLayoutParams(new FrameLayout.LayoutParams(
				newHeight * mWidth / mHeight, LayoutParams.MATCH_PARENT, Gravity.LEFT));

		mDrawerContainerOrigWidth = mCWidth - (newHeight * mWidth / mHeight);
//		mMyLifeLV.setLayoutParams(new FrameLayout.LayoutParams(
//				1, LayoutParams.MATCH_PARENT, Gravity.RIGHT));
		mMyLifeDrawerContainerRL.setLayoutParams(new FrameLayout.LayoutParams(
				mDrawerContainerOrigWidth, LayoutParams.MATCH_PARENT, Gravity.RIGHT));
		
//		mMyLifeLV.setLayoutParams(new FrameLayout.LayoutParams(
//				listViewWidth, LayoutParams.MATCH_PARENT, Gravity.RIGHT));

//		Log.d("asd", "ListView width >" + listViewWidth);

//		mDrawerWidth = mGlobalDrawerWidth - listViewWidth;

//		Log.d("asd", "mDrawerWidth" + mDrawerWidth);
//		mWidthInc = (float)((float)mDrawerWidth/(float)100);
//		Log.d("asd", "What is the width inc" + mWidthInc);

/*		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				mDrawerWidth + (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 20,
						getResources().getDisplayMetrics()), LayoutParams.WRAP_CONTENT, Gravity.RIGHT|Gravity.CENTER_VERTICAL);
		params.setMargins(0, 0, listViewWidth - (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 2,
				getResources().getDisplayMetrics()), 0);
		mDrawer.setLayoutParams(params);
		mDrawer.setProgress(1000);*/
	}
	
}
