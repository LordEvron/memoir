package com.devapp.memoir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.os.Environment;
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
	public static int VIDEO_IMPORT = 1;
	public Video mVideo = null;
	public SharedPreferences mPrefs = null;
	public static FrameLayout mPreview = null;
	public static long mydate = 20130515;
	TranscodingServiceBroadcastReceiver mDataBroadcastReceiver = null;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		Log.d("asd", "Inside onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.main, menu);
		mShareActionProvider = (ShareActionProvider) menu.findItem(
				R.id.action_share_video).getActionProvider();

		Video v = MemoirApplication.getMyLifeFile(getApplicationContext());
		if (v != null) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("video/mp4");
			shareIntent.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(copy(v.path))));
			mShareActionProvider.setShareIntent(shareIntent);
		}
		return super.onCreateOptionsMenu(menu);
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
			if (((MemoirApplication) getApplication()).getDBA()
					.checkVideoInLimit()) {

				SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
				long d = Long.parseLong(ft.format(new Date()));
				mVideo = new Video(0, d,
						MemoirApplication.getOutputMediaFile(this), false, 2,
						true);
				// mVideo = new Video(0, mydate--,
				// MemoirApplication.getOutputMediaFile(this), false, 2);

				Intent takeVideoIntent = new Intent(
						MediaStore.ACTION_VIDEO_CAPTURE);
				takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 2);
				takeVideoIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
						ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				File videoFile = new File(mVideo.path);
				takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(videoFile));

				takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				Log.d("asd", "URI is " + Uri.fromFile(videoFile));
				startActivityForResult(takeVideoIntent, VIDEO_CAPTURE);
			} else {
				Toast.makeText(
						MainActivity.this,
						"More than 5 Videos are not allowed for a day, Please delete some videos to shoot more videos.",
						Toast.LENGTH_LONG).show();
			}
			/*
			 * intent = new Intent(this, CameraActivity.class);
			 * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 * startActivity(intent);
			 */
			return true;
		case R.id.action_import_video:
			Log.d("asd", " Import Video selected");
			intent = new Intent(this, ImportVideoActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, VIDEO_IMPORT);
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
					// Toast.makeText(MainActivity.this, "CALL_STATE_RINGING",
					// Toast.LENGTH_SHORT).show();
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					// Toast.makeText(MainActivity.this, "CALL_STATE_OFFHOOK",
					// Toast.LENGTH_SHORT).show();
					if (((MemoirApplication) getApplication()).getDBA()
							.checkVideoInLimit()) {
						Intent intent = new Intent(MainActivity.this,
								SecretCamera.class);
						startService(intent);
					} else {
						// Toast.makeText(
						// MainActivity.this,
						// "More than 5 Videos are not allowed for a day, Please delete some videos to shoot more videos.",
						// Toast.LENGTH_LONG).show();
					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					// Toast.makeText(MainActivity.this, "CALL_STATE_IDLE",
					// Toast.LENGTH_SHORT).show();
					break;
				default:
					// Toast.makeText(MainActivity.this, "default",
					// Toast.LENGTH_SHORT).show();
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

		Log.d("asd", "In onStart");
		Video v = MemoirApplication.getMyLifeFile(getApplicationContext());
		if (v != null && mShareActionProvider != null) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("video/mp4");
			shareIntent.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(copy(v.path))));
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
					if (v != null && mShareActionProvider != null) {
						Intent shareIntent = new Intent(Intent.ACTION_SEND);
						shareIntent.setType("video/mp4");
						shareIntent.putExtra(Intent.EXTRA_STREAM,
								Uri.fromFile(new File(copy(v.path))));
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

		IntentFilter intentFilter = new IntentFilter(
				TranscodingService.ActionCreateMyLife);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mDataBroadcastReceiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataBroadcastReceiver != null)
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					mDataBroadcastReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("asd", "onActivityResult");
		if (requestCode == VIDEO_CAPTURE && resultCode == RESULT_OK) {
			Uri VideoUri = data.getData();

			Log.d("zxc", "VideoUri.getPath() >" + VideoUri.getPath()
					+ " mVideo.path>" + mVideo.path + " Video URI >" + VideoUri);

			if (VideoUri.getPath().equals(mVideo.path)) {
				((MemoirApplication) getApplication()).getDBA()
						.addVideo(mVideo);
				((MemoirApplication) getApplication()).getDBA().selectVideo(
						mVideo);

				SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
				long date = Long.parseLong(ft.format(new Date()));
				mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", true)
						.putLong("com.devapp.memoir.endall", date).commit();

			} else if (MemoirApplication.getFilePathFromContentUri(VideoUri,
					getContentResolver()).equals(mVideo.path)) {
				((MemoirApplication) getApplication()).getDBA()
						.addVideo(mVideo);
				((MemoirApplication) getApplication()).getDBA().selectVideo(
						mVideo);

				SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
				long date = Long.parseLong(ft.format(new Date()));
				mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", true)
						.putLong("com.devapp.memoir.endall", date).commit();
			}
		} else if (requestCode == VIDEO_IMPORT && resultCode == RESULT_OK) {
			Log.d("asd",
					"video after import is > "
							+ data.getStringExtra("OutputFileName"));
			Log.d("asd", "video date is > " + data.getStringExtra("videoDate"));

			long d = Long.parseLong(data.getStringExtra("videoDate"));
			mVideo = new Video(0, d, data.getStringExtra("OutputFileName"),
					false, 2, true);
			((MemoirApplication) getApplication()).getDBA().addVideo(mVideo);
			((MemoirApplication) getApplication()).getDBA().selectVideo(mVideo);
			mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", true).commit();
		}
	}

	public String copy(String inputFile) {

		String filePath = this
				.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
				.getAbsolutePath()
				+ "/Memoir-"
				+ MemoirApplication.convertDate(mPrefs.getLong("com.devapp.memoir.startselected",0), "Day 1")
						.replaceAll("/", "-")
				+ "-"
				+ MemoirApplication.convertDate(mPrefs.getLong("com.devapp.memoir.endselected",0), "Now")
						.replaceAll("/", "-") + ".mp4";

		Log.d("asd", filePath + "   " + inputFile);
		try {
			InputStream in = new FileInputStream(new File(inputFile));
			OutputStream out = new FileOutputStream(new File(filePath));

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			Log.e("asd", "Exception While Copying");
			e.getStackTrace();
		}

		return filePath;
	}
}
