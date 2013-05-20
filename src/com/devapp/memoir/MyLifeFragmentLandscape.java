package com.devapp.memoir;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MyLifeFragmentLandscape extends MyLifeFragment{

	private SeekBar mDrawer = null;
	private FrameLayout mMyLifeContainerFL = null;
	private int gAdsHeight = 0;
	private ListView mMyLifeLV = null;
	private int listViewWidth = 0, mDrawerWidth = 0;
	private float mWidthInc = 0;
	private int originalProgress = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("asd", "On Create View of Landscape");
		View rootView = inflater.inflate(R.layout.fragment_my_life, container,
				false);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d("asd", "On Activity Created of Landscape");
		super.onActivityCreated(savedInstanceState);
		Activity activity = this.getActivity();

		mMyLifeFL = (FrameLayout) activity.findViewById(R.id.MyLifeFL);
		mMyLifeContainerFL = (FrameLayout) activity.findViewById(R.id.MyLifeContainerFL);
		mMyLifeLV = (ListView) activity.findViewById(R.id.MyLifeDateLV);
		
		mDrawer = (SeekBar) activity.findViewById(R.id.MyLifeSB);
		mDrawer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int position, boolean fromTouch) {
				Log.d("asd", "Position is " + position + " Incrementing by " + (100-position)*mWidthInc);
	            if(fromTouch){
					if ((position > (originalProgress + 10))
							|| (position < (originalProgress - 10))) {
						seekBar.setProgress(originalProgress);
					} else {
						originalProgress = position;
						mMyLifeLV.setLayoutParams(new FrameLayout.LayoutParams(
								(int) (listViewWidth + (100-position)*mWidthInc) , LayoutParams.MATCH_PARENT, Gravity.RIGHT));
					}
				}	  
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.d("asd", "Inside onStartTrackingTouch");
				originalProgress = seekBar.getProgress();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
		});
		
		gAdsHeight = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 50,
						getResources().getDisplayMetrics());

		mDrawerWidth = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 450,
				getResources().getDisplayMetrics());
		Log.d("asd", "Drawer width is supposed to be in terms of 400dp > " + mDrawerWidth);
		
		ViewTreeObserver vto = mMyLifeFL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				setDisplayMatrix();
				
				int newHeight = mMyLifeContainerFL.getHeight() - gAdsHeight;
				mMyLifeContainerFL.setLayoutParams(new RelativeLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, newHeight));
				
				Log.d("asd", "Video view width >" + newHeight * mWidth / mHeight);
				mMyLifeFL.setLayoutParams(new FrameLayout.LayoutParams(
						newHeight * mWidth / mHeight, LayoutParams.MATCH_PARENT, Gravity.LEFT));

				listViewWidth = mCWidth - (newHeight * mWidth / mHeight);
//				mMyLifeLV.setLayoutParams(new FrameLayout.LayoutParams(
//						1, LayoutParams.MATCH_PARENT, Gravity.RIGHT));
				mMyLifeLV.setLayoutParams(new FrameLayout.LayoutParams(
						listViewWidth, LayoutParams.MATCH_PARENT, Gravity.RIGHT));

				Log.d("asd", "ListView width >" + listViewWidth);

				mDrawerWidth = mDrawerWidth - listViewWidth;

				Log.d("asd", "mDrawerWidth" + mDrawerWidth);
				mWidthInc = (float)((float)mDrawerWidth/(float)100);
				Log.d("asd", "What is the width inc" + mWidthInc);

				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
						mDrawerWidth + (int) TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP, 20,
								getResources().getDisplayMetrics()), LayoutParams.WRAP_CONTENT, Gravity.RIGHT|Gravity.BOTTOM);
				params.setMargins(0, 0, listViewWidth - (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 2,
						getResources().getDisplayMetrics()), 0);
				mDrawer.setLayoutParams(params);
				mDrawer.setProgress(1000);
				
				
				ViewTreeObserver vto = mMyLifeFL.getViewTreeObserver();
				vto.removeOnGlobalLayoutListener(this);
			}
		});
		
		VideoView mVv = (VideoView) activity.findViewById(R.id.MyLifeVV);
		MemoirMediaController mmc = new MemoirMediaController(getActivity(), mMyLifeFL, mVv);
		mVv.requestFocus();

		mVv.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d("asd", "OnPrepared in Landscape got called");
				MyLifeFragmentLandscape.this.onVideoViewPrepared();
			}
		});

		mVv.setVideoPath("/storage/emulated/0/DCIM/Camera/VID_20130427_192004.mp4");
	}
	
}
