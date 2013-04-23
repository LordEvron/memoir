package com.devapp.memoir;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ShareActionProvider;

public class MainActivity extends Activity {
	private ShareActionProvider mShareActionProvider;
	public static int VIDEO_CAPTURE = 0;
	public Video mVideo = null;
	public SharedPreferences mPrefs = null;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		Video v = MemoirApplication.getMyLifeFile(getApplicationContext());
		if (v != null) {
			mShareActionProvider = (ShareActionProvider) menu.findItem(
					R.id.action_share_video).getActionProvider();
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

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
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
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
