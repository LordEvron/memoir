package com.devapp.memoir;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.devapp.memoir.database.Video;
import com.devapp.memoir.services.TranscodingService;

public class MainActivity extends Activity {
	private ShareActionProvider mShareActionProvider;
	public static int VIDEO_CAPTURE = 0;
	public static int VIDEO_IMPORT = 1;
	public static Video mVideo = null;
	public SharedPreferences mPrefs = null;
	TranscodingServiceBroadcastReceiver mDataBroadcastReceiver = null;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		mShareActionProvider = (ShareActionProvider) menu.findItem(
				R.id.action_share_video).getActionProvider();

		//new shareActionProviderTask().execute();
		shareActionProviderTask();
		return true;
//		return super.onCreateOptionsMenu(menu);
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
			if (((MemoirApplication) getApplication()).getDBA()
					.checkVideoInLimit()) {

				SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
				long d = Long.parseLong(ft.format(new Date()));
				mVideo = new Video(0, d,
						MemoirApplication.getOutputMediaFile(this), false, 2,
						true);

				Intent takeVideoIntent = new Intent(
						MediaStore.ACTION_VIDEO_CAPTURE);
//				Intent takeVideoIntent = new Intent(this, CameraLandscapeDummyActivity.class);

				takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, mPrefs.getInt(
						"com.devapp.memoir.noofseconds", 2));
				takeVideoIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
						ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				File videoFile = new File(mVideo.path);
				Log.d("asd", "Vido PAth is " + mVideo.path);
				takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(videoFile));

				takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				startActivityForResult(takeVideoIntent, VIDEO_CAPTURE);
			} else {
				Toast.makeText(
						MainActivity.this,
						"More than 5 Videos are not allowed for a day, Please delete some videos to shoot more videos.",
						Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.action_import_video:
			intent = new Intent(this, ImportVideoActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, VIDEO_IMPORT);
			return true;
		case R.id.action_settings:
			intent = new Intent(this, SettingsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.action_help:
			intent = new Intent(this, HelpActivity.class);
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
		
		Log.d("asd", "OnCreate of Main Activity");
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Log.d("asd", "orintation > " + getResources().getConfiguration().orientation + "  >" + Configuration.ORIENTATION_PORTRAIT);
		Log.d("asd", "Activity Layout main 's reference " + R.layout.activity_main_portrait);

		/** NOTE: for some reason my activity was not alke to pick up correct xml file based on orientation hence I have done this*/
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d("asd", "Launching landscape activity");
			setContentView(R.layout.activity_main_landscape);
		} else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.d("asd", "Launching portrait activity");
			setContentView(R.layout.activity_main_portrait);
		}
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(false);

		mPrefs = this.getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);
	}

	@Override
	public void onStart() {
		super.onStart();
//		if(mShareActionProvider != null) {
//			new shareActionProviderTask().execute();
//		}
	}

	public class TranscodingServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("OutputFileName")) {
				String outputFile = intent.getStringExtra("OutputFileName");

				if (!outputFile.isEmpty()) {
					if(mShareActionProvider != null) {
						shareActionProviderTask();
						//new shareActionProviderTask().execute();
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

		Log.d("asd", " On Activity result for activity " + this);
		if (requestCode == VIDEO_CAPTURE && resultCode == RESULT_OK) {
			Uri VideoUri = data.getData();

			Log.d("zxc", "VideoUri.getPath() >" + VideoUri.getPath()
					+ " mVideo.path>" + mVideo.path + " Video URI >" + VideoUri);

			if (VideoUri.getPath().equals(mVideo.path) || MemoirApplication.getFilePathFromContentUri(VideoUri,
					getContentResolver()).equals(mVideo.path)) {
				
				MediaMetadataRetriever mMediaRetriever = new MediaMetadataRetriever();
				mMediaRetriever.setDataSource(mVideo.path);

				if (android.os.Build.VERSION.SDK_INT >= 14
						&& android.os.Build.VERSION.SDK_INT <= 16) {

					Bitmap bmp = mMediaRetriever.getFrameAtTime(0);
					if (bmp.getHeight() > bmp.getWidth()) {
						Toast.makeText(
								this,
								"This video is in portrait mode and can not be imported",
								Toast.LENGTH_LONG).show();
						findViewById(R.id.action_shoot_video).callOnClick();
						return;
					}

				} else if (android.os.Build.VERSION.SDK_INT >= 17) {
					int rotationAngle = Integer
							.parseInt(mMediaRetriever
									.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

					if (rotationAngle == 90 || rotationAngle == 270) {
						Toast.makeText(
								this,
								"This video is in portrait mode and can not be imported",
								Toast.LENGTH_LONG).show();
						findViewById(R.id.action_shoot_video).callOnClick();
						return;
					}
				}
				
				((MemoirApplication) getApplication()).getDBA()
						.addVideo(mVideo);
				((MemoirApplication) getApplication()).getDBA().selectVideo(
						mVideo);

				SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
				long date = Long.parseLong(ft.format(new Date()));
				mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", true)
						.putLong("com.devapp.memoir.endall", date)
						.putLong("com.devapp.memoir.endselected", date).commit();
/*				
				List<List<Video>> videos1 = ((MemoirApplication) this.getApplication()).getDBA().getVideos(0, -1, false,
						mPrefs.getBoolean("com.devapp.memoir.showonlymultiple", false));
				
				List<List<Video>> videos2 = ((MyLifeFragment)this.getFragmentManager().findFragmentByTag("Fragment")).mVideos;

				if(videos1.size() != videos2.size()) {
					Log.d("asd", "Calling on Start again");
					//((MyLifeFragment)this.getFragmentManager().findFragmentByTag("Fragment")).onStart();
				} else {
					int len = videos1.size();
					for(int i = 0; i < len ; i++) {
						if(videos1.get(i).size() != videos2.get(i).size()) {
							Log.d("asd", "Calling on Start again");
							//((MyLifeFragment)this.getFragmentManager().findFragmentByTag("Fragment")).onStart();
						}
					}
				}*/
			}
		} else if (requestCode == VIDEO_IMPORT && resultCode == RESULT_OK) {
			//Log.d("asd",
			//		"video after import is > "
			//				+ data.getStringExtra("OutputFileName"));
			//Log.d("asd", "video date is > " + data.getStringExtra("videoDate"));

			long d = Long.parseLong(data.getStringExtra("videoDate"));
			mVideo = new Video(0, d, data.getStringExtra("OutputFileName"),
					false, 2, true);
			((MemoirApplication) getApplication()).getDBA().addVideo(mVideo);
			((MemoirApplication) getApplication()).getDBA().selectVideo(mVideo);
			
			mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", true).commit();
			if(mPrefs.getLong("com.devapp.memoir.startall", 0) > d) {
				mPrefs.edit().putLong("com.devapp.memoir.startall", d).commit();
			}
			if(mPrefs.getLong("com.devapp.memoir.startselected", 0) > d) {
				mPrefs.edit().putLong("com.devapp.memoir.startselected", d).commit();
			}
		}
	}

	public void shareActionProviderTask() {
		Video v = ((MemoirApplication)getApplication()).getMyLifeFile(getApplicationContext());
		
		if (v != null && mShareActionProvider != null) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("video/mp4");
			shareIntent.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(v.path)));
			mShareActionProvider.setShareIntent(shareIntent);
		}
	}
	
/*	public class shareActionProviderTask extends AsyncTask<Void, Void, String> {

		public String copy(String inputFile) {
			String filePath = getApplicationContext()
					.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
					.getAbsolutePath()
					+ "/Memoir/Memoir-"
					+ MemoirApplication.convertDate(mPrefs.getLong("com.devapp.memoir.startselected",0), "Day 1")
							.replaceAll("/", "-")
					+ "-"
					+ MemoirApplication.convertDate(mPrefs.getLong("com.devapp.memoir.endselected",0), "Now")
							.replaceAll("/", "-") + ".mp4";

			try {
				InputStream in = new FileInputStream(new File(inputFile));
				OutputStream out = new FileOutputStream(new File(filePath));

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			} catch (IOException e) {
				Log.e("asd", "Exception While Copying" );
				e.printStackTrace();
			}
			return filePath;
		}
		
		@Override
		protected String doInBackground(Void... arg0) {

			Video v = ((MemoirApplication)getApplication()).getMyLifeFile(getApplicationContext());
			
			if (v != null) {
				return copy(v.path);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(result!= null && mShareActionProvider != null) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("video/mp4");
				shareIntent.putExtra(Intent.EXTRA_STREAM,
						Uri.fromFile(new File(result)));
				mShareActionProvider.setShareIntent(shareIntent);
			}
			super.onPostExecute(result);
		}
	}*/
}
