package com.devapp.memoir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;

import com.coremedia.iso.IsoFile;
import com.devapp.memoir.R;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.splashanimations);
		LinearLayout ll = (LinearLayout) findViewById(R.id.splashLL);
		ll.startAnimation(animation);
		
		final int splashtime = 2500;
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
			        if(true/*!prefs.getBoolean("first_time", false)*/)
			        {
			            i = new Intent(Splash.this, WelcomeScreen.class);
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
