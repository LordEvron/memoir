package com.devapp.memoir;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class CameraLandscapeDummyActivity extends Activity {

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e("asd", "On activity result of Camera Landscape Dummy Activity");
		if(getParent() == null) {
			setResult(resultCode, data);
		} else {
			getParent().setResult(resultCode, data);
		}
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		Log.e("asd", "On Create of Camera Landscape Dummy Activity");
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		Intent i = this.getIntent();
		intent.putExtras(i.getExtras());
		startActivityForResult(intent, 0);
	}

}
