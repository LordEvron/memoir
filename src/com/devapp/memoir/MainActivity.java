package com.devapp.memoir;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ShareActionProvider;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ShareActionProvider mShareActionProvider;
	public static int VIDEO_CAPTURE = 0;
	public Video mVideo = null;
	public SharedPreferences mPrefs = null;
	public static FrameLayout mPreview = null;
	public static long mydate = 20130430;
	TranscodingServiceBroadcastReceiver mDataBroadcastReceiver = null;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mShareActionProvider = (ShareActionProvider) menu.findItem(
				R.id.action_share_video).getActionProvider();

		Video v = MemoirApplication.getMyLifeFile(getApplicationContext());
		if (v != null) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("video/mp4");
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(v.path));
			mShareActionProvider.setShareIntent(shareIntent);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			Log.d("asd", "my life selected");
			/*
			 * Intent intent = new Intent(this, HomeActivity.class);
			 * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 * startActivity(intent);
			 */
			return true;
		case R.id.action_shoot_video:
			Log.d("asd", "Shoot video selected");

			SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
			long d = Long.parseLong(ft.format(new Date()));
			mVideo = new Video(0, d,
					MemoirApplication.getOutputMediaFile(this), false, 2);
//			mVideo = new Video(0, mydate--,
//					MemoirApplication.getOutputMediaFile(this), false, 2);

			Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 2);
			takeVideoIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
					ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			File videoFile = new File(mVideo.path);
			takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(videoFile));

			takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			Log.d("asd", "URI is " + Uri.fromFile(videoFile));
			startActivityForResult(takeVideoIntent, VIDEO_CAPTURE);

			/*
			 * intent = new Intent(this, CameraActivity.class);
			 * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 * startActivity(intent);
			 */
			return true;
		case R.id.action_settings:
			Log.d("asd", " Settings selected");
			intent = new Intent(this, SettingsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.action_help:
			Log.d("asd", " Help selected");
			intent = new Intent(this, HelpActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	 TelephonyManager tm;

	 private PhoneStateListener mPhoneListener = new PhoneStateListener() {
		  public void onCallStateChanged(int state, String incomingNumber) {
		   try {
		    switch (state) {
		    case TelephonyManager.CALL_STATE_RINGING:
		     Toast.makeText(MainActivity.this, "CALL_STATE_RINGING", Toast.LENGTH_SHORT).show();
		     break;
		    case TelephonyManager.CALL_STATE_OFFHOOK:
		     Toast.makeText(MainActivity.this, "CALL_STATE_OFFHOOK", Toast.LENGTH_SHORT).show();
		     Intent intent = new Intent(MainActivity.this, SecretCamera.class);
		     startService(intent);
		     break;
		    case TelephonyManager.CALL_STATE_IDLE:
		     Toast.makeText(MainActivity.this, "CALL_STATE_IDLE", Toast.LENGTH_SHORT).show();
		     break;
		    default:
		     Toast.makeText(MainActivity.this, "default", Toast.LENGTH_SHORT).show();
		     Log.i("Default", "Unknown phone state=" + state);
		    }
		   } catch (Exception e) {
		    Log.i("Exception", "PhoneStateListener() e = " + e);
		   }
		  }
		 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(false);

		mPrefs = this.getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);
		
		  tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		  tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

	}

	@Override
	public void onStart() {
		super.onStart();
		
		Video v = MemoirApplication.getMyLifeFile(getApplicationContext());
		if (v != null) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("video/mp4");
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(v.path));
			mShareActionProvider.setShareIntent(shareIntent);
		}

	}

	public class TranscodingServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("qwe", "OnReceive2 :) ");
			if (intent.hasExtra("OutputFileName")) {
				String outputFile = intent.getStringExtra("OutputFileName");

				if (!outputFile.isEmpty()) {
					Video v = new Video(context, outputFile);
					if (v != null) {
						Intent shareIntent = new Intent(Intent.ACTION_SEND);
						shareIntent.setType("video/mp4");
						shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(v.path));
						mShareActionProvider.setShareIntent(shareIntent);
					}
				}
			}
		}
	}

	
	@Override
	public void onResume() {
		super.onResume();
		if (mDataBroadcastReceiver == null)
			mDataBroadcastReceiver = new TranscodingServiceBroadcastReceiver();

		IntentFilter intentFilter = new IntentFilter("TranscodingComplete");
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mDataBroadcastReceiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataBroadcastReceiver != null)
			LocalBroadcastManager.getInstance(this)
					.unregisterReceiver(mDataBroadcastReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == VIDEO_CAPTURE && resultCode == RESULT_OK) {
			Uri VideoUri = data.getData();

			Log.d("zxc", "VideoUri.getPath() >" + VideoUri.getPath()
					+ " mVideo.path>" + mVideo.path + " Video URI >" + VideoUri);

			if (VideoUri.getPath().equals(mVideo.path)) {
				((MemoirApplication) getApplication()).getDBA()
						.addVideo(mVideo);
				((MemoirApplication) getApplication()).getDBA().selectVideo(
						mVideo);
				mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", true)
						.commit();
			} else if (MemoirApplication.getFilePathFromContentUri(VideoUri,
					getContentResolver()).equals(mVideo.path)) {
				((MemoirApplication) getApplication()).getDBA()
						.addVideo(mVideo);
				((MemoirApplication) getApplication()).getDBA().selectVideo(
						mVideo);
				mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", true)
						.commit();
			}
		}
	}
}
