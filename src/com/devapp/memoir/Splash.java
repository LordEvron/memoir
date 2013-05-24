package com.devapp.memoir;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.devapp.memoir.database.MemoirDBA;

public class Splash extends Activity {

	private boolean mIsBackButtonPressed;
	private static final int SPLASH_DURATION = 2000;
	private SharedPreferences mPrefs = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(Splash.this);
		
		if (!mPrefs.getBoolean("com.devapp.memoir.firsttime", false)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		setContentView(R.layout.activity_splash);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (!mIsBackButtonPressed) {
					Intent i;
					if (!mPrefs.getBoolean("com.devapp.memoir.firsttime", false)) {
						mPrefs.edit().putBoolean("com.devapp.memoir.firsttime", true).commit();
						i = new Intent(Splash.this, WelcomeScreen.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					} else {
						i = new Intent(Splash.this, MainActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					}
					startActivity(i);
					finish();
				}
			}
		}, SPLASH_DURATION);

		new BackgroundTasks().execute(((MemoirApplication) getApplication()).getDBA());
		
		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.splashanimations);
		LinearLayout ll = (LinearLayout) findViewById(R.id.splashLL);
		ll.startAnimation(animation);
	}

	@Override
	public void onBackPressed() {

		// set the flag to true so the next activity won't start up
		mIsBackButtonPressed = true;
		super.onBackPressed();
	}
	
	public class BackgroundTasks extends AsyncTask<MemoirDBA, Void, Void> {

		@Override
		protected Void doInBackground(MemoirDBA... arg0) {
			MemoirApplication.setDisplayMatrix(Splash.this);

			MemoirDBA dba = arg0[0];
			dba.updateDatabase();
			dba.getVideos(0, -1, false, PreferenceManager
					.getDefaultSharedPreferences(Splash.this).getBoolean("com.devapp.memoir.showonlymultiple", false));
			return null;
		}
	}
}
