package com.devapp.memoir;
import com.devapp.memoir.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		final int splashtime = 3000;
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
					startActivity (new Intent (Splash.this, Welcome.class));
					finish();
				}
			}
		};
		splashthread.start();
	}

}
