package com.devapp.memoir;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionMode;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class MyLifeFragment extends Fragment {

	ListView mDateList = null;
	MyLifeDateListArrayAdapter mDateAdapter = null;
	List<List<Video>> mVideos = null;
	View mRootView = null;
	MediaController mMc = null;
	VideoView mVv = null;
	TranscodingServiceBroadcastReceiver mDataBroadcastReceiver = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_my_life, container,
				false);
		Log.d("asd", "Calling onCreateView");
		mRootView = rootView;
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mDateList = (ListView) getActivity().findViewById(R.id.MyLifeDateLV);

		mVv = (VideoView) getActivity().findViewById(R.id.MyLifeVV);
		Context c = this.getActivity().getApplicationContext();
		Log.d("asd",
				"Output file" + MemoirApplication.getConcatenatedOutputFile(c));
		mVv.setVideoPath(MemoirApplication.getConcatenatedOutputFile(c));
		mMc = new MediaController(getActivity());
		mVv.setMediaController(mMc);
		mVv.requestFocus();

		mVv.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d("asd", "in onPreapraed");

				LinearLayout ll = (LinearLayout) getActivity().findViewById(
						R.id.MyLifeLL);
				ll.setLayoutParams(new FrameLayout.LayoutParams(mVv.getWidth(),
						mVv.getHeight() - 155));
				mMc.setAnchorView(ll);

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
		    	getActivity().findViewById(R.id.MyLifePlayIV).setVisibility(View.VISIBLE);
		    }
		});  

		getActivity().findViewById(R.id.MyLifePlayIV).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View imageView) {
				imageView.setVisibility(View.INVISIBLE);
				//mVv.start();
			}
			
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d("asd", "OnStart of my Fragement");
		mVideos = ((MemoirApplication) getActivity().getApplication()).getDBA()
				.getVideos(0, -1, false);
		mDateAdapter = new MyLifeDateListArrayAdapter(getActivity(), mVideos);
		mDateList.setAdapter(mDateAdapter);

		// PendingIntent createPendingResult(int requestCode, Intent data, int
		// flags)
		/*Intent dataIntent = new Intent();
		PendingIntent pendingIntent = this.getActivity().createPendingResult(0,
				dataIntent, PendingIntent.FLAG_ONE_SHOT);
		Intent intent = new Intent(this.getActivity(), TranscodingService.class);
		Bundle b = new Bundle();
		b.putParcelable("pendingIntent", pendingIntent);
		intent.putExtras(b);
		this.getActivity().startService(intent);*/
		
		/*Intent broadcastReceiverIntent = new Intent(this.getActivity(), DataBroadcastReceiver.class);      
	      //create pending intent for broadcasting the DataBroadcastReceiver
	      PendingIntent pi = PendingIntent.getBroadcast(context, 0, broadcastReceiverIntent, 0);      
	      Bundle bundle = new Bundle();            
	      bundle.putParcelable("receiver", pi);
	      //we want to start our service (for handling our time-consuming operation)
	      Intent serviceIntent = new Intent(context, DataRequestService.class);
	      serviceIntent.putExtras(bundle);
	      context.startService(serviceIntent);*/
		
		refreshLifeTimeVideo();
	}
	
	public void refreshLifeTimeVideo() {
		getActivity().findViewById(R.id.MyLifePlayIV).setVisibility(View.INVISIBLE);
		Intent intent = new Intent(this.getActivity(), TranscodingService.class);
		this.getActivity().startService(intent);
		getActivity().findViewById(R.id.MyLifePlayPB).setVisibility(View.VISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
        if (mDataBroadcastReceiver == null) 
        	mDataBroadcastReceiver = new TranscodingServiceBroadcastReceiver();
        
        IntentFilter intentFilter = new IntentFilter("TranscodingComplete");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDataBroadcastReceiver, intentFilter);
	}

	@Override
    public void onPause() {
        super.onPause();
        if (mDataBroadcastReceiver != null) 
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDataBroadcastReceiver);
    }

	
	public class TranscodingServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			getActivity().findViewById(R.id.MyLifePlayPB).setVisibility(View.INVISIBLE);
			getActivity().findViewById(R.id.MyLifePlayIV).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("asd", "I got my on Activity Result Yeeeeee:)");
	}

	public class MyLifeDateListArrayAdapter extends ArrayAdapter<List<Video>> {

		private Context mContext;
		private List<List<Video>> mList;
		private LayoutInflater mInflater;
		private LinearLayout mLinearLayout;
		private Hashtable<Long, RelativeLayout> mHashtable;
		private Object mActionMode;
		private Video mSelectedVideo = null;
		private ImageView mSelectedVideoIV = null;

		// private MyLifeVideoListArrayAdapter mVideoAdapter;

		public MyLifeDateListArrayAdapter(Context context,
				List<List<Video>> List) {
			super(context, R.layout.fragment_my_life_list_item);

			this.mContext = context;
			this.mList = List;
			this.mInflater = LayoutInflater.from(context);
			// this.mHashtable = new Hashtable<Long, RelativeLayout>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d("asd", "in getView for position " + position);
			if (convertView == null) {
				Log.d("asd", "convertView turned out to be null ");
				convertView = mInflater.inflate(
						R.layout.fragment_my_life_list_item, null);
				if (position % 2 == 0) {
					convertView
							.setBackgroundResource(R.drawable.alterselector1);
				} else {
					convertView
							.setBackgroundResource(R.drawable.alterselector2);
				}
			} else {
				Log.d("asd", "ConvertView exists");

				if (convertView.getTag() != null
						&& convertView.getTag().equals("dirty")) {
					mLinearLayout = (LinearLayout) convertView
							.findViewById(R.id.MyLifeListItemInnerLL);
					mLinearLayout.removeAllViews();
				} else {
					return convertView;
				}
				Log.d("asd", "the value is " + convertView.getTag());
			}

			List<Video> VideoList = this.mList.get(position);
			// NOTE: assuming VideoList can never be null here.
			mLinearLayout = (LinearLayout) convertView
					.findViewById(R.id.MyLifeListItemInnerLL);

			Log.d("asd", "Got the linear layout and it contains "
					+ mLinearLayout.getChildCount());
			TextView tv = (TextView) convertView
					.findViewById(R.id.MyLifeListItemTV);
			Date d = new Date(VideoList.get(0).date);
			String date = String.valueOf(VideoList.get(0).date);
			tv.setText(date.substring(6) + "/" + date.substring(4, 6) + "/"
					+ date.substring(0, 4));

			for (Video v : VideoList) {
				FrameLayout FL = (FrameLayout) mInflater.inflate(
						R.layout.fragment_my_life_video_item, null);
				mLinearLayout.addView(FL);
				Log.d("asd", "path is " + v.path);
				ImageView iv = (ImageView) FL.findViewById(R.id.imageView1);
				Bitmap bmp = BitmapFactory.decodeFile(v.thumbnailPath);
				iv.setImageBitmap(bmp);
				iv.setTag(v);
				if (v.selected) {
					iv.setBackgroundColor(getResources().getColor(
							R.color.selectBlue));
				}

				final Uri videopath = Uri.fromFile(new File(v.path));

				iv.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent playIntent = new Intent();
						playIntent.setAction(Intent.ACTION_VIEW);
						playIntent.setDataAndType(videopath, "video/*");
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

				if (v.selected) {
					// FL.findViewById(R.id.checkBox1).setActivated(true);
				}

			}

			/*
			 * if(!VideoList.isEmpty()) { Video v1 = VideoList.get(0); Long l =
			 * new Long(v1.date); if(mHashtable.containsKey(l)) { RelativeLayout
			 * RL = (RelativeLayout) mHashtable.get(l);
			 * mLinearLayout.addView(RL, 0); } else { for(Video v : VideoList) {
			 * RelativeLayout RL = (RelativeLayout)
			 * mInflater.inflate(R.layout.fragment_my_life_video_item, null);
			 * mLinearLayout.addView(RL); VideoView videoV = (VideoView)
			 * RL.findViewById(R.id.videoView1); //videoV.setVideoPath(v.path);
			 * Log.d("asd", "path is " + v.path); } } }
			 */
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
						
						/** NOTE: Function call to refresh the LifeTime Video*/
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
											
											/** NOTE: Function call to refresh the LifeTime Video*/
											refreshLifeTimeVideo();
										} else if (videoList.isEmpty()) {
											mList.remove(videoList);
											
											/** NOTE: Function call to refresh the LifeTime Video*/
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

	/*
	 * public class MyLifeVideoListArrayAdapter extends ArrayAdapter<Video> {
	 * 
	 * private Context mContext; private List<Video> mList; private
	 * LayoutInflater mInflater;
	 * 
	 * public MyLifeVideoListArrayAdapter(Context context, List<Video> List) {
	 * super(context, R.layout.fragment_my_life_video_item);
	 * 
	 * this.mContext = context; this.mList = List; this.mInflater =
	 * LayoutInflater.from(context); }
	 * 
	 * @Override public View getView(int position, View convertView, ViewGroup
	 * parent) {
	 * 
	 * if(convertView == null) { convertView =
	 * mInflater.inflate(R.layout.fragment_my_life_video_item, parent); }
	 * 
	 * Video video = this.mList.get(position); VideoView view = (VideoView)
	 * convertView; view.setVideoPath(video.path);
	 * 
	 * return convertView; }
	 * 
	 * }
	 */

}
