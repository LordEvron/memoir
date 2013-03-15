package com.devapp.memoir;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

public class MyLifeFragment extends Fragment{

	ListView mDateList = null;
	MyLifeDateListArrayAdapter mDateAdapter = null;
	List<List<Video>> mVideos = null;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_my_life, container, false);

        return rootView;
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	mDateList = (ListView) getActivity().findViewById(R.id.dateLV);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		Log.d("asd", "OnStart of my Fragement");
		mVideos = ((MemoirApplication) getActivity().getApplication()).getDBA().getVideos(0, -1, false);
		mDateAdapter = new MyLifeDateListArrayAdapter(getActivity(), mVideos);
		mDateList.setAdapter(mDateAdapter);
		
	}
	
	public class MyLifeDateListArrayAdapter extends ArrayAdapter<List<Video>> {

		private Context mContext;
		private List<List<Video>> mList;
		private LayoutInflater mInflater;
		private LinearLayout mLinearLayout;
		//private MyLifeVideoListArrayAdapter mVideoAdapter;
		
		public MyLifeDateListArrayAdapter(Context context, List<List<Video>> List) {
			super(context, R.layout.fragment_my_life_list_item);

			this.mContext = context;
	        this.mList = List;
	        this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d("asd", "in getView");
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.fragment_my_life_list_item, null);
			}
			
			List<Video> VideoList = this.mList.get(position);
			mLinearLayout = (LinearLayout) convertView.findViewById(R.id.videoListLL);
			
			for(Video v : VideoList) {
				/*VideoView view = (VideoView) mInflater.inflate(R.layout.fragment_my_life_video_item, null);
				mLinearLayout.addView(view);
				view.setVideoPath(v.path);*/

				TextView view = (TextView) mInflater.inflate(R.layout.fragment_my_life_video_item, null);
				mLinearLayout.addView(view);
				view.setText(v.path);
			}
			return convertView;
		}
		
		@Override
		public int getCount() {
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
	
	/*public class MyLifeVideoListArrayAdapter extends ArrayAdapter<Video> {

		private Context mContext;
		private List<Video> mList;
		private LayoutInflater mInflater;
		
		public MyLifeVideoListArrayAdapter(Context context, List<Video> List) {
			super(context, R.layout.fragment_my_life_video_item);

			this.mContext = context;
	        this.mList = List;
	        this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.fragment_my_life_video_item, parent);
			}
			
			Video video = this.mList.get(position);
			VideoView view = (VideoView) convertView;
			view.setVideoPath(video.path);
			
			return convertView;
		}
		
	}*/


}
