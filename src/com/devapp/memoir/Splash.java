package com.devapp.memoir;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class Splash extends Activity {

	private boolean mIsBackButtonPressed;
	private static final int SPLASH_DURATION = 2500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				finish();
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(Splash.this);
				if (!mIsBackButtonPressed) {
					Intent i;
					if (!prefs.getBoolean("first_time", false)) {
						i = new Intent(Splash.this, WelcomeScreen.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					} else {
						i = new Intent(Splash.this, MainActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					}
					startActivity(i);
				}
			}
		}, SPLASH_DURATION);

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
}
