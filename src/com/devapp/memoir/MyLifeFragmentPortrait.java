package com.devapp.memoir;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class MyLifeFragmentPortrait extends MyLifeFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("asd", "On Create View of portrait");
		View rootView = inflater.inflate(R.layout.fragment_my_life, container,
				false);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d("asd", "On Activity Create of Portrait");
		Activity activity = this.getActivity();

		mMyLifeIV = (ImageView) activity.findViewById(R.id.MyLifeIV);
		mMyLifeFullscreenIV = (ImageView) activity
				.findViewById(R.id.MyLifeFullscreenIV);
		mMyLifePB = (ProgressBar) activity.findViewById(R.id.MyLifePB);
		mMyLifeTV = (TextView) activity.findViewById(R.id.MyLifeTV);
		mMyLifeFL = (FrameLayout) activity.findViewById(R.id.MyLifeFL);
		// mTransparent = getResources().getColor(android.R.color.transparent);
		mTransparent = getResources().getColor(android.R.color.black);


		ViewTreeObserver vto = mMyLifeFL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				setDisplayMatrix();

				mMyLifeFL.setLayoutParams(new RelativeLayout.LayoutParams(
						mWidth, (int) (mWidth * mWidth / mHeight)));

				ViewTreeObserver vto = mMyLifeFL.getViewTreeObserver();
				vto.removeOnGlobalLayoutListener(this);
			}
		});

		final VideoView mVv = (VideoView) activity.findViewById(R.id.MyLifeVV);
		mVv.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d("asd", "OnPrepared in portrait got called");
			}
		});


		super.onActivityCreated(savedInstanceState);
	}
}
