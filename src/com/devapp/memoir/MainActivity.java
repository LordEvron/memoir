package com.devapp.memoir;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

public class MainActivity extends Activity {
	private ShareActionProvider mShareActionProvider;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		mShareActionProvider = (ShareActionProvider) menu.findItem(
				R.id.action_share_video).getActionProvider();
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("video/mp4");
		Context c = this.getApplicationContext();
		shareIntent.putExtra(Intent.EXTRA_STREAM,
				Uri.parse(MemoirApplication.getConcatenatedOutputFile(c)));
		mShareActionProvider.setShareIntent(shareIntent);
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
			intent = new Intent(this, CameraActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
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

	/*
	 * public static class TabListener<T extends Fragment> implements
	 * ActionBar.TabListener { private Fragment mFragment; private final
	 * Activity mActivity; private final String mTag; private final Class<T>
	 * mClass;
	 * 
	 * public TabListener(Activity activity, String tag, Class<T> clz) {
	 * mActivity = activity; mTag = tag; mClass = clz; }
	 * 
	 * public void onTabSelected(Tab tab, FragmentTransaction ft) { // Check if
	 * the fragment is already initialized if (mFragment == null) { // If not,
	 * instantiate and add it to the activity mFragment =
	 * Fragment.instantiate(mActivity, mClass.getName());
	 * ft.add(android.R.id.content, mFragment, mTag); } else { // If it exists,
	 * simply attach it in order to show it ft.attach(mFragment); } }
	 * 
	 * public void onTabUnselected(Tab tab, FragmentTransaction ft) { if
	 * (mFragment != null) { // Detach the fragment, because another one is
	 * being attached ft.detach(mFragment); } }
	 * 
	 * public void onTabReselected(Tab tab, FragmentTransaction ft) { // User
	 * selected the already selected tab. Usually do nothing. } }
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * requestWindowFeature(Window.FEATURE_NO_TITLE);
		 * getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		 * WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 */
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);

		/*
		 * Tab tab = actionBar.newTab() .setText("My Life") .setTabListener(new
		 * TabListener<MyLifeFragment>( this, "artist", MyLifeFragment.class));
		 * actionBar.addTab(tab);
		 */

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
	}
}
