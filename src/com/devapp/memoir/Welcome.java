package com.devapp.memoir;

import com.devapp.memoir.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Welcome extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_welcome);
    }
	public void LaunchTabView (View v) {
		startActivity (new Intent (Welcome.this, MainActivity.class));
	}
}
