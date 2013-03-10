package com.devapp.memoir;
import com.devapp.memoir.R;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.splashanimations);
		TextView tv = (TextView) findViewById(R.id.splashtitle);
		tv.startAnimation(animation);
		
		final int splashtime = 3000;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Thread splashthread =  new Thread() {
			int wait = 0;
			
			@Override
			public void run() {
				try {
					super.run();
					while (wait < splashtime){
						sleep(100);
						wait += 100;
					}
				} catch (Exception e) {
					System.out.println ("Exception = " + e);
				} finally {
					Intent i;
			        if(!prefs.getBoolean("first_time", false))
			        {
			            i = new Intent(Splash.this, Welcome.class);
			            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        }
			        else
			        {
			        	i = new Intent(Splash.this, MainActivity.class);
			        	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        }
					startActivity (i);
					finish();
				}
			}
		};
		splashthread.start();
	}

}
