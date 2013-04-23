package com.devapp.memoir;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class MyLifeFragment extends Fragment {

	ListView mDateList = null;
	MyLifeDateListArrayAdapter mDateAdapter = null;
	List<List<Video>> mVideos = null;
	View mRootView = null;
	MediaController mMc = null;
	VideoView mVv = null;
	TranscodingServiceBroadcastReceiver mDataBroadcastReceiver = null;
	private SharedPreferences mPrefs = null;
	public ImageView mMyLifeIV = null;
	public ProgressBar mMyLifePB = null;
	public TextView mMyLifeTV = null;

	public int mTransparent = 0;
	public Video mMyLifeVideo = null;
	public int mHeight = 0, mWidth = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_my_life, container,
				false);
		mRootView = rootView;
		mPrefs = this.getActivity().getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);

		return rootView;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mDateList = (ListView) mRootView.findViewById(R.id.MyLifeDateLV);
		mMyLifeIV = (ImageView) mRootView.findViewById(R.id.MyLifeIV);
		mMyLifePB = (ProgressBar) mRootView.findViewById(R.id.MyLifePB);
		mMyLifeTV = (TextView) mRootView.findViewById(R.id.MyLifeTV);
		mTransparent = getResources().getColor(android.R.color.transparent);

		/** Note : For getting the height and width of the screen */
		if (android.os.Build.VERSION.SDK_INT >= 14
				&& android.os.Build.VERSION.SDK_INT <= 16) {
			Display display = getActivity().getWindowManager()
					.getDefaultDisplay();
			try {
				Method mGetRawH = Display.class.getMethod("getRawHeight");
				Method mGetRawW = Display.class.getMethod("getRawWidth");
				mWidth = (Integer) mGetRawW.invoke(display);
				mHeight = (Integer) mGetRawH.invoke(display);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			Display display = getActivity().getWindowManager()
					.getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getRealMetrics(outMetrics);
			mHeight = outMetrics.heightPixels;
			mWidth = outMetrics.widthPixels;
		}
		((FrameLayout) mRootView.findViewById(R.id.MyLifeFL))
				.setLayoutParams(new LinearLayout.LayoutParams(mWidth,
						(int) (mWidth * mWidth / mHeight)));

		mVv = (VideoView) mRootView.findViewById(R.id.MyLifeVV);
		mMc = new MediaController(getActivity());
		mVv.setMediaController(mMc);
		mVv.requestFocus();

		mVv.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d("qwe", "in onPreapraed");

				LinearLayout ll = (LinearLayout) getActivity().findViewById(
						R.id.MyLifeLL);
				ll.setLayoutParams(new FrameLayout.LayoutParams(mVv.getWidth(),
						mVv.getHeight() - 155));
				mMc.setAnchorView(ll);

				updateMyLifeViews(R.drawable.play, mMyLifeVideo.thumbnailPath,
						View.VISIBLE, View.INVISIBLE, View.VISIBLE);

				// mRootView.findViewById(R.id.MyLifePlayIV).setVisibility(
				// View.VISIBLE);

				mp.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
					@Override
					public void onVideoSizeChanged(MediaPlayer arg0, int arg1,
							int arg2) {
						LinearLayout ll = (LinearLayout) getActivity()
								.findViewById(R.id.MyLifeLL);
						ll.setLayoutParams(new FrameLayout.LayoutParams(mVv
								.getWidth(), mVv.getHeight() - 155));
						mMc.setAnchorView(ll);
					}
				});
			}
		});

		mVv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer vmp) {
				Log.d("qwe", "On Completion listener");
				updateMyLifeViews(R.drawable.play, mMyLifeVideo.thumbnailPath,
						View.VISIBLE, View.INVISIBLE, View.VISIBLE);
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

		// Log.d("qwe", "OnStart of my Fragement");
		mVideos = ((MemoirApplication) getActivity().getApplication()).getDBA()
				.getVideos(0, -1, false);
		mDateAdapter = new MyLifeDateListArrayAdapter(getActivity(), mVideos);
		mDateList.setAdapter(mDateAdapter);

		// PendingIntent createPendingResult(int requestCode, Intent data, int
		// flags)
		/*
		 * Intent dataIntent = new Intent(); PendingIntent pendingIntent =
		 * this.getActivity().createPendingResult(0, dataIntent,
		 * PendingIntent.FLAG_ONE_SHOT); Intent intent = new
		 * Intent(this.getActivity(), TranscodingService.class); Bundle b = new
		 * Bundle(); b.putParcelable("pendingIntent", pendingIntent);
		 * intent.putExtras(b); this.getActivity().startService(intent);
		 */

		/*
		 * Intent broadcastReceiverIntent = new Intent(this.getActivity(),
		 * DataBroadcastReceiver.class); //create pending intent for
		 * broadcasting the DataBroadcastReceiver PendingIntent pi =
		 * PendingIntent.getBroadcast(context, 0, broadcastReceiverIntent, 0);
		 * Bundle bundle = new Bundle(); bundle.putParcelable("receiver", pi);
		 * //we want to start our service (for handling our time-consuming
		 * operation) Intent serviceIntent = new Intent(context,
		 * DataRequestService.class); serviceIntent.putExtras(bundle);
		 * context.startService(serviceIntent);
		 */

		if (mPrefs.getBoolean("com.devapp.memoir.datachanged", true) == true) {
			mPrefs.edit().putBoolean("com.devapp.memoir.datachanged", false)
					.commit();
			refreshLifeTimeVideo();
		} else {
			mMyLifeVideo = MemoirApplication.getMyLifeFile(getActivity()
					.getApplicationContext());
			if (mMyLifeVideo != null) {
				mVv.setVideoPath(mMyLifeVideo.path);

				MediaMetadataRetriever mm = new MediaMetadataRetriever();
				mm.setDataSource(mMyLifeVideo.path);
				String h = mm
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
				String w = mm
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

				Log.d("asd", "Video width and height are " + w + "   " + h);
			} else {
				updateMyLifeViews(R.drawable.no_video, null, View.VISIBLE,
						View.INVISIBLE, View.INVISIBLE);
			}
		}
	}

	public void refreshLifeTimeVideo() {

		updateMyLifeViews(R.drawable.no_video, null, View.INVISIBLE,
				View.VISIBLE, View.VISIBLE);

		Intent intent = new Intent(getActivity(), TranscodingService.class);
		getActivity().startService(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDataBroadcastReceiver == null)
			mDataBroadcastReceiver = new TranscodingServiceBroadcastReceiver();

		IntentFilter intentFilter = new IntentFilter("TranscodingComplete");
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mDataBroadcastReceiver, intentFilter);
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

		String text = "       Memoir - My Life            "
				+ mPrefs.getString("com.devapp.memoir.startselected",
						"Day 1")
				+ " - "
				+ mPrefs.getString("com.devapp.memoir.endselected",
						"Now");
		mMyLifeTV.setText(text);
		mMyLifeTV.setVisibility(TVVis);
	}

	public class TranscodingServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("qwe", "OnReceive :) ");
			String outputFile = intent.getStringExtra("OutputFileName");
			if (!outputFile.isEmpty()) {
				mMyLifeVideo = new Video(context, outputFile);
				Log.d("asd", "Setting video path here >" + mMyLifeVideo.path);
				mVv.setVideoPath(mMyLifeVideo.path);
			} else {
				updateMyLifeViews(R.drawable.no_video, null, View.VISIBLE,
						View.INVISIBLE, View.INVISIBLE);
			}
		}
	}

	public class MyLifeDateListArrayAdapter extends ArrayAdapter<List<Video>> {

		private Context mContext;
		private List<List<Video>> mList;
		private LayoutInflater mInflater;
		private LinearLayout mLinearLayout;
		private Object mActionMode;
		private Video mSelectedVideo = null;
		private ImageView mSelectedVideoIV = null;

		public MyLifeDateListArrayAdapter(Context context,
				List<List<Video>> List) {
			super(context, R.layout.fragment_my_life_list_item);

			this.mContext = context;
			this.mList = List;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Log.d("asd", "in getView for position " + position);

			// NOTE: assuming VideoList can never be null here.
			List<Video> VideoList = this.mList.get(position);
			String date = String.valueOf(VideoList.get(0).date);

			if (convertView == null) {
				// Log.d("asd", "convertView turned out to be null ");

				convertView = mInflater.inflate(
						R.layout.fragment_my_life_list_item, null);
			} else {
				// Log.d("asd", "ConvertView exists");

				if (!((TextView) convertView
						.findViewById(R.id.MyLifeListItemTV)).getText().equals(
						date.substring(6) + "/" + date.substring(4, 6) + "/"
								+ date.substring(0, 4))) {
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
					.setText(date.substring(6) + "/" + date.substring(4, 6)
							+ "/" + date.substring(0, 4));

			mLinearLayout = (LinearLayout) convertView
					.findViewById(R.id.MyLifeListItemInnerLL);

			for (Video v : VideoList) {
				FrameLayout FL = (FrameLayout) mInflater.inflate(
						R.layout.fragment_my_life_video_item, null);
				mLinearLayout.addView(FL);

				ImageView iv = (ImageView) FL.findViewById(R.id.imageView1);
				iv.setImageBitmap(BitmapFactory.decodeFile(v.thumbnailPath));
				iv.setTag(v);
				if (v.selected) {
					iv.setBackgroundColor(getResources().getColor(
							R.color.selectBlue));
				}

				iv.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						Video v = (Video) view.getTag();
						Intent playIntent = new Intent();
						playIntent.setAction(Intent.ACTION_VIEW);
						playIntent.setDataAndType(
								Uri.fromFile(new File(v.path)), "video/*");
						getActivity().startActivity(playIntent);
					}
				});

				iv.setOnLongClickListener(new View.OnLongClickListener() {
					// Called when the user long-clicks on someView
					public boolean onLongClick(View view) {
						if (mActionMode != null) {
							return false;
						}

						// Start the CAB using the ActionMode.Callback defined
						// above
						mActionMode = getActivity().startActionMode(
								mActionModeCallback);
						mSelectedVideoIV = (ImageView) view;
						mSelectedVideo = (Video) mSelectedVideoIV.getTag();
						return true;
					}
				});
			}

			return convertView;
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
								.getParent().getParent().getParent()
								.getParent();
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

						for (List<Video> videoList : mList) {
							if (videoList.get(0).date == mSelectedVideo.date) {
								for (Video v : videoList) {
									if (v.path.equals(mSelectedVideo.path)) {
										videoList.remove(v);
										if (mSelectedVideo.selected
												&& !videoList.isEmpty()) {
											Video tmpV = videoList.get(0);
											tmpV.selected = true;
											((MemoirApplication) getActivity()
													.getApplication()).getDBA()
													.selectVideo(tmpV);

											/**
											 * NOTE: Function call to refresh
											 * the LifeTime Video
											 */
											refreshLifeTimeVideo();
										} else if (videoList.isEmpty()) {
											mList.remove(videoList);

											/**
											 * NOTE: Function call to refresh
											 * the LifeTime Video
											 */
											refreshLifeTimeVideo();
										}
										break;
									}
								}
							}
						}

						LinearLayout container = (LinearLayout) mSelectedVideoIV
								.getParent().getParent().getParent()
								.getParent();
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
