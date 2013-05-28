package com.devapp.memoir;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.devapp.memoir.database.MemoirDBA;

public class Splash extends Activity {

	private boolean mIsBackButtonPressed;
	private static final int SPLASH_DURATION = 2000;
	private SharedPreferences mPrefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(Splash.this);

		if (!mPrefs.getBoolean("com.devapp.memoir.firsttime", false)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		if (!mPrefs.getBoolean("com.devapp.memoir.agreement", false)) {
			new EndUserLicenseAgreement(this).show(
					new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							mPrefs.edit()
									.putBoolean("com.devapp.memoir.agreement",
											true).commit();
							proceed();
						}

					}, new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Splash.this.finish();
						}

					});
		} else {
			proceed();
		}

		setContentView(R.layout.activity_splash);
		ImageView iv = (ImageView) findViewById(R.id.splashIV);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			iv.setImageResource(R.drawable.backgroundlandscape);
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			iv.setImageResource(R.drawable.backgroundportrait);
		}
		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.splashanimations);
		iv.startAnimation(animation);

	}

	public void proceed() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (!mIsBackButtonPressed) {
					Intent i;
					if (!mPrefs
							.getBoolean("com.devapp.memoir.firsttime", false)) {
						mPrefs.edit()
								.putBoolean("com.devapp.memoir.firsttime", true)
								.commit();
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

		new BackgroundTasks().execute(((MemoirApplication) getApplication())
				.getDBA());
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
			MemoirDBA dba = arg0[0];
			dba.updateDatabase();
			dba.updateDatabaseForOlderEntries(45);
			dba.getVideos(
					0,
					-1,
					false,
					PreferenceManager.getDefaultSharedPreferences(Splash.this)
							.getBoolean("com.devapp.memoir.showonlymultiple",
									false));
			return null;
		}
	}
}
