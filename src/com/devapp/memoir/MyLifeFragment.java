package com.devapp.memoir;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.devapp.memoir.database.Video;
import com.devapp.memoir.services.TranscodingService;

public class MyLifeFragment extends Fragment {

	MyLifeDateListArrayAdapter mDateAdapter = null;

	TranscodingServiceBroadcastReceiver mDataBroadcastReceiver = null;
	private SharedPreferences mPrefs = null;
	public ImageView mMyLifeIV = null;
	public ProgressBar mMyLifePB = null;
	public TextView mMyLifeTV = null;

	public int mTransparent = 0;
	public Video mMyLifeVideo = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_my_life, container,
				false);

		mPrefs = this.getActivity().getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity activity = this.getActivity();

		mMyLifeIV = (ImageView) activity.findViewById(R.id.MyLifeIV);
		mMyLifePB = (ProgressBar) activity.findViewById(R.id.MyLifePB);
		mMyLifeTV = (TextView) activity.findViewById(R.id.MyLifeTV);
		// mTransparent = getResources().getColor(android.R.color.transparent);
		mTransparent = getResources().getColor(android.R.color.black);

		if(MemoirApplication.mWidth == 0 || MemoirApplication.mHeight == 0) {
			MemoirApplication.setDisplayMatrix(getActivity());
		}
		((FrameLayout) activity.findViewById(R.id.MyLifeFL))
				.setLayoutParams(new RelativeLayout.LayoutParams(
						MemoirApplication.mWidth,
						(int) (MemoirApplication.mWidth
								* MemoirApplication.mWidth / MemoirApplication.mHeight)));

		final VideoView mVv = (VideoView) activity.findViewById(R.id.MyLifeVV);
		final MediaController mMc = new MediaController(getActivity());
		mVv.setMediaController(mMc);
		mVv.requestFocus();

		mVv.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				if (mMyLifeVideo != null) {
					LinearLayout ll = (LinearLayout) getActivity()
							.findViewById(R.id.MyLifeLL);
					ll.setLayoutParams(new FrameLayout.LayoutParams(mVv
							.getWidth(), mVv.getHeight() - 155));
					mMc.setAnchorView(ll);
					updateMyLifeViews(R.drawable.play,
							mMyLifeVideo.thumbnailPath, View.VISIBLE,
							View.INVISIBLE, View.VISIBLE);
				}

				/*
				 * mp.setOnVideoSizeChangedListener(new
				 * OnVideoSizeChangedListener() {
				 * 
				 * @Override public void onVideoSizeChanged(MediaPlayer arg0,
				 * int arg1, int arg2) { Log.d("asd",
				 * "onVideoSizeChanged Called"); LinearLayout ll =
				 * (LinearLayout) getActivity() .findViewById(R.id.MyLifeLL);
				 * ll.setLayoutParams(new FrameLayout.LayoutParams(mVv
				 * .getWidth(), mVv.getHeight() - 155)); mMc.setAnchorView(ll);
				 * } });
				 */
			}
		});

		mVv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer vmp) {
				mMyLifeVideo = ((MemoirApplication) getActivity()
						.getApplication()).getMyLifeFile(getActivity()
						.getApplicationContext());
				// mMyLifeVideo = MemoirApplication.getMyLifeFile(getActivity()
				// .getApplicationContext());
				if (mMyLifeVideo != null) {
					updateMyLifeViews(R.drawable.play,
							mMyLifeVideo.thumbnailPath, View.VISIBLE,
							View.INVISIBLE, View.VISIBLE);
				} else {
					updateMyLifeViews(R.drawable.no_video, null, View.VISIBLE,
							View.INVISIBLE, View.INVISIBLE);
				}
			}
		});

		mVv.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				Log.e("asd", "Cant Play This Video :(");
				return true;
			}

		});

		mMyLifeIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (view.getTag() != null) {
					updateMyLifeViews(R.drawable.play, null, View.INVISIBLE,
							View.INVISIBLE, View.INVISIBLE);
					mVv.start();
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		((MemoirApplication) getActivity().getApplication()).getDBA()
				.setStartEndDates();

		List<List<Video>> mVideos = ((MemoirApplication) getActivity().getApplication()).getDBA()
				.getVideos(
						0,
						-1,
						false,
						mPrefs.getBoolean("com.devapp.memoir.showonlymultiple",
								false));
		mDateAdapter = new MyLifeDateListArrayAdapter(getActivity(), mVideos);
		ListView mDateList = (ListView) getActivity().findViewById(R.id.MyLifeDateLV);
		mDateList.setAdapter(mDateAdapter);
	}

	public void refreshLifeTimeVideo() {

		updateMyLifeViews(R.drawable.no_video, null, View.INVISIBLE,
				View.VISIBLE, View.INVISIBLE);

		Intent intent = new Intent(getActivity(), TranscodingService.class);
		intent.setAction(TranscodingService.ActionCreateMyLife);
		intent.putExtra("startDate",
				mPrefs.getLong("com.devapp.memoir.startselected", 0));
		intent.putExtra("endDate",
				mPrefs.getLong("com.devapp.memoir.endselected", 0));
		getActivity().startService(intent);
		mMyLifeVideo = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDataBroadcastReceiver == null)
			mDataBroadcastReceiver = new TranscodingServiceBroadcastReceiver();

		IntentFilter intentFilter = new IntentFilter(
				TranscodingService.ActionCreateMyLife);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mDataBroadcastReceiver, intentFilter);

		if (mPrefs.getBoolean("com.devapp.memoir.datachanged", true) == true) {
			mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", false)
					.commit();
			refreshLifeTimeVideo();
		} else {
			mMyLifeVideo = ((MemoirApplication) getActivity().getApplication())
					.getMyLifeFile(getActivity().getApplicationContext());
			if (mMyLifeVideo != null) {
				((VideoView) getActivity().findViewById(R.id.MyLifeVV)).setVideoPath(mMyLifeVideo.path);
				//mVv.setVideoPath(mMyLifeVideo.path);
			} else {
				updateMyLifeViews(R.drawable.no_video, null, View.VISIBLE,
						View.INVISIBLE, View.INVISIBLE);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataBroadcastReceiver != null)
			LocalBroadcastManager.getInstance(getActivity())
					.unregisterReceiver(mDataBroadcastReceiver);
	}

	public void updateMyLifeViews(int IVRes, String IVPath, int IVVis,
			int PBVis, int TVVis) {

		mMyLifeIV.setImageResource(IVRes);
		if (IVPath != null) {
			mMyLifeIV.setBackgroundDrawable(new BitmapDrawable(getResources(),
					BitmapFactory.decodeFile(IVPath)));
			mMyLifeIV.setTag(IVPath);
		} else {
			mMyLifeIV.setBackgroundColor(mTransparent);
			mMyLifeIV.setTag(null);
		}
		mMyLifeIV.requestLayout();
		mMyLifeIV.setVisibility(IVVis);

		mMyLifePB.setVisibility(PBVis);

		String text = "   Memoir - My Life\n"
				+ MemoirApplication.convertDate(
						mPrefs.getLong("com.devapp.memoir.startselected", 0),
						"Long Time Ago")
				+ " - "
				+ MemoirApplication.convertDate(
						mPrefs.getLong("com.devapp.memoir.endselected", 0),
						"Now");
		mMyLifeTV.setText(text);
		mMyLifeTV.setVisibility(TVVis);
	}

	public class TranscodingServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("OutputFileName")) {
				String outputFile = intent.getStringExtra("OutputFileName");

				if (!outputFile.isEmpty()) {
					mMyLifeVideo = ((MemoirApplication) getActivity()
							.getApplication()).getMyLifeFile(getActivity()
							.getApplicationContext());

					// Log.d("asd", "Setting video path here >"
					// + mMyLifeVideo.path);
					if(mMyLifeVideo != null) {
						((VideoView) getActivity().findViewById(R.id.MyLifeVV)).setVideoPath(mMyLifeVideo.path);
//						mVv.setVideoPath(mMyLifeVideo.path);
					}
				} else {
					if(intent.hasExtra("Error")) {
						Toast.makeText(
								getActivity(),
								intent.getStringExtra("Error"), Toast.LENGTH_LONG).show();

					}
					updateMyLifeViews(R.drawable.no_video, null, View.VISIBLE,
							View.INVISIBLE, View.INVISIBLE);
				}
			}
		}
	}

	public class MyLifeDateListArrayAdapter extends ArrayAdapter<List<Video>>
			implements View.OnClickListener, View.OnLongClickListener {

		private Context mContext;
		private List<List<Video>> mList;
		private LayoutInflater mInflater;
		private LinearLayout mLinearLayout;
		private Object mActionMode;
		private Video mSelectedVideo = null;
		private ImageView mSelectedVideoIV = null;
		private ViewGroup.MarginLayoutParams params = null;
		
		private HashMap<String, ImageView> IVCache = null;

		public MyLifeDateListArrayAdapter(Context context,
				List<List<Video>> List) {
			super(context, R.layout.fragment_my_life_list_item);

			this.mContext = context;
			this.mList = List;
			this.mInflater = LayoutInflater.from(context);
			this.params = new ViewGroup.MarginLayoutParams(
					(int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, 90, getResources()
									.getDisplayMetrics()), (int) TypedValue.applyDimension(
											TypedValue.COMPLEX_UNIT_DIP, 68, getResources()
											.getDisplayMetrics()));
			
			IVCache = new HashMap<String, ImageView>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			List<Video> VideoList = this.mList.get(position);
			long date = VideoList.get(0).date;
//			String date = MemoirApplication.getDateStringWRTToday(VideoList
//					.get(0).date);

			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.fragment_my_life_list_item, null);
			} else {
//				if (!((TextView) convertView
//						.findViewById(R.id.MyLifeListItemTV)).getText().equals(
//						date)) {
				if(Long.valueOf((Long)((TextView) convertView.findViewById(R.id.MyLifeListItemTV)).getTag()) !=
						date) {
					/**
					 * NOTE: Comparing dates of this list item and the item from
					 * position if they are same then continue otherwise needs
					 * to flush the view
					 */

					((LinearLayout) convertView
							.findViewById(R.id.MyLifeListItemInnerLL))
							.removeAllViews();
				} else if (convertView.getTag() != null
						&& convertView.getTag().equals("dirty")) {
					/**
					 * NOTE: Comparing even if the dates are same but if its
					 * marked dirty then redraw
					 */

					((LinearLayout) convertView
							.findViewById(R.id.MyLifeListItemInnerLL))
							.removeAllViews();
				} else {
					return convertView;
				}
			}
			/** NOTE: Setting the background color of tiles */
			if (position % 2 == 0) {
				convertView.setBackgroundResource(R.drawable.alterselector1);
			} else {
				convertView.setBackgroundResource(R.drawable.alterselector2);
			}

			((TextView) convertView.findViewById(R.id.MyLifeListItemTV))
					.setText(MemoirApplication.getDateStringWRTToday(date));
			((TextView) convertView.findViewById(R.id.MyLifeListItemTV)).setTag(new Long(date));

			mLinearLayout = (LinearLayout) convertView
					.findViewById(R.id.MyLifeListItemInnerLL);

			for (Video v : VideoList) {

/*				ImageView iv = null;
				if(IVCache.containsKey(v.path)) {
					Log.d("asd", "Reading IV from cache");
					iv = (ImageView) IVCache.get(v.path);
					if(((LinearLayout) iv.getParent()) != null)
						((LinearLayout) iv.getParent()).removeView(iv);
				} else {
					Log.d("asd", "Creating new IV");
					iv = (ImageView) mInflater.inflate(
							R.layout.fragment_my_life_video_item, null);
					MemoirApplication.mTL.loadImage(v.path, iv);
					IVCache.put(v.path, iv);
				}
				mLinearLayout.addView(iv, this.params);
*/

				
				ImageView iv = (ImageView) mInflater.inflate(
						R.layout.fragment_my_life_video_item, null);
				mLinearLayout.addView(iv, this.params);

				MemoirApplication.mTL.loadImage(v.path, iv);

				/*
				 * FrameLayout FL = (FrameLayout) mInflater.inflate(
				 * R.layout.fragment_my_life_video_item, null);
				 * mLinearLayout.addView(FL);
				 * 
				 * ImageView iv = (ImageView) FL.findViewById(R.id.imageView1);
				 * 
				 * if(v.thumbnailPath != null) {
				 * iv.setImageBitmap(BitmapFactory.decodeFile(v.thumbnailPath));
				 * } else { MemoirApplication.mTL.loadImage(v.path, iv); }
				 */
				iv.setTag(v);
				if (v.selected) {
					iv.setBackgroundColor(getResources().getColor(
							R.color.selectBlue));
				}

				iv.setOnClickListener(this);

				iv.setOnLongClickListener(this);
			}

			return convertView;
		}

		@Override
		public boolean onLongClick(View view) {
			if (mActionMode != null) {
				return false;
			}
			((ImageView) view)
					.setColorFilter(new LightingColorFilter(
							0xFFFFFF, 0x005050));

			mActionMode = getActivity().startActionMode(
					mActionModeCallback);
			mSelectedVideoIV = (ImageView) view;
			mSelectedVideo = (Video) mSelectedVideoIV.getTag();
			return true;
		}

		@Override
		public void onClick(View view) {
			Video v = (Video) view.getTag();
			Intent playIntent = new Intent();
			playIntent.setAction(Intent.ACTION_VIEW);
			playIntent.setDataAndType(
					Uri.fromFile(new File(v.path)), "video/*");

			getActivity().startActivity(playIntent);
		}

		
		private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.my_life_fragment_contextual_menu, menu);
				return true;
			}

			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				switch (item.getItemId()) {
				case R.id.select_button:
					mode.finish(); // Action picked, so close the CAB
					if (mSelectedVideo != null) {
						((MemoirApplication) getActivity().getApplication())
								.getDBA().selectVideo(mSelectedVideo);
						mSelectedVideoIV.setBackgroundColor(getResources()
								.getColor(R.color.selectBlue));

						for (List<Video> videoList : mList) {
							if (videoList.get(0).date == mSelectedVideo.date) {
								for (Video v : videoList) {
									if (!v.path.equals(mSelectedVideo.path)) {
										v.selected = false;
									}
								}
							}
						}
						mSelectedVideo.selected = true;
						LinearLayout container = (LinearLayout) mSelectedVideoIV
								.getParent().getParent().getParent();
						container.setTag("dirty");
						mDateAdapter.notifyDataSetChanged();
						mSelectedVideo = null;

						/** NOTE: Function call to refresh the LifeTime Video */
						refreshLifeTimeVideo();
					}
					return true;
				case R.id.delete_button:
					mode.finish(); // Action picked, so close the CAB
					if (mSelectedVideo != null) {
						((MemoirApplication) getActivity().getApplication())
								.getDBA().deleteVideo(mSelectedVideo);

						List<Video> videoListRef = null;
						Video videoRef = null;
						for (List<Video> videoList : mList) {
							if (videoList.get(0).date == mSelectedVideo.date) {
								videoListRef = videoList;
								videoRef = null;
								for (Video v : videoList) {
									if (v.path.equals(mSelectedVideo.path)) {
										videoRef = v;
										break;
									}
								}
								if(videoRef != null)
									break;
							}
						}
									
						if(videoRef != null) {
							videoListRef.remove(videoRef);
							if (mSelectedVideo.selected
									&& !videoListRef.isEmpty()) {
								Video tmpV = videoListRef.get(0);
								tmpV.selected = true;
								((MemoirApplication) getActivity()
										.getApplication()).getDBA()
										.selectVideo(tmpV);

								/**
								 * NOTE: Function call to refresh
								 * the LifeTime Video
								 */
								refreshLifeTimeVideo();
							} else if (videoListRef.isEmpty()) {
								mList.remove(videoListRef);

								/**
								 * NOTE: Function call to refresh
								 * the LifeTime Video
								 */
								refreshLifeTimeVideo();
							}
						}
						
						LinearLayout container = (LinearLayout) mSelectedVideoIV
								.getParent().getParent().getParent();
						container.setTag("dirty");
						mDateAdapter.notifyDataSetChanged();
						mSelectedVideo = null;
					}
					return true;
				default:
					return false;
				}
			}

			public void onDestroyActionMode(ActionMode mode) {
				mActionMode = null;
				mSelectedVideoIV.setColorFilter(null);
			}
		};

		@Override
		public int getCount() {
			if (mList == null)
				return 0;
			return mList.size();
		}

		@Override
		public List<Video> getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}
}
