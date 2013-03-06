package com.devapp.memoir;

import com.devapp.memoir.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

public class Welcome extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_welcome);
    }
	public void LaunchTabView (View v) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("first_time", true);
        editor.commit();
        Intent i = new Intent (Welcome.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity (i);
		finish();
	}
}
