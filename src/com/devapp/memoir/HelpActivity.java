package com.devapp.memoir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class HelpActivity extends Activity {
	
	public ExpandableListView mList = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("first_time", true);
        editor.commit();
        
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_help);

		mList = (ExpandableListView) findViewById(R.id.ActivityHelpELV);    
		ExpandableListAdapter adapter = new MyExpandableListAdapter(this);
		mList.setAdapter(adapter);
		mList.expandGroup(0);
	}

	@Override
	public void onBackPressed ()
	{
		Intent i = new Intent (HelpActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity (i);
		finish();
	}
	public class MyExpandableListAdapter extends BaseExpandableListAdapter {
		private int[] groups = { R.string.help_mt1 , R.string.help_mt2, R.string.help_mt3, R.string.help_mt4, R.string.help_mt5, R.string.help_mt6 };
		private int[][] children = { { R.string.help_t1}, { R.string.help_t2}, { R.string.help_t3}, { R.string.help_t4}, { R.string.help_t5}, { R.string.help_t6}};

		private Context cxt;
		private Resources res;
		private LayoutInflater mInflater = null;
		

		public MyExpandableListAdapter(Context cxt) {
			this.cxt = cxt;
			this.res = this.cxt.getResources();
			this.mInflater = LayoutInflater.from(cxt);
		}

		@Override
		public Object getChild(int groupPos, int childPos) {
			return this.res.getString(children[groupPos][childPos]);
		}

		@Override
		public long getChildId(int groupPos, int childPos) {
			return childPos;
		}

		@Override
		public View getChildView(int groupPos, int childPos,
				boolean isLastChild, View convertView, ViewGroup parent) {

			TextView tv = (TextView)convertView;
			if(convertView == null)
				tv = (TextView) this.mInflater.inflate(R.layout.activity_help_child, null);
			tv.setText(getChild(groupPos, childPos).toString());
			return tv;
		}

		@Override
		public int getChildrenCount(int groupPos) {
			return children[groupPos].length;
		}

		@Override
		public String getGroup(int groupPos) {
			return this.res.getString(groups[groupPos]);
		}

		@Override
		public int getGroupCount() {
			return groups.length;
		}

		@Override
		public long getGroupId(int groupPos) {
			return groupPos;
		}

		@Override
		public View getGroupView(int groupPos, boolean isExpanded,
				View convertView, ViewGroup parent) {
			
			TextView tv = (TextView)convertView;
			if(convertView == null)
				tv = (TextView) this.mInflater.inflate(R.layout.activity_help_header, null);
			tv.setText(getGroup(groupPos).toString());
			return tv;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPos, int childPos) {
			if(groupPos > 0)
				return true;
			else 
				return false;
		}

	}
}

/*
 * ((TextView)findViewById(R.id.HelpActivityMTV2)).setOnClickListener(new
 * myClickListener((TextView) findViewById(R.id.HelpActivityTV2)));
 * ((TextView
 * )findViewById(R.id.HelpActivityMTV3)).setOnClickListener(new
 * myClickListener((TextView) findViewById(R.id.HelpActivityTV3)));
 * ((TextView
 * )findViewById(R.id.HelpActivityMTV4)).setOnClickListener(new
 * myClickListener((TextView) findViewById(R.id.HelpActivityTV4)));
 * ((TextView
 * )findViewById(R.id.HelpActivityMTV5)).setOnClickListener(new
 * myClickListener((TextView) findViewById(R.id.HelpActivityTV5)));
 * ((TextView
 * )findViewById(R.id.HelpActivityMTV6)).setOnClickListener(new
 * myClickListener((TextView) findViewById(R.id.HelpActivityTV6)));
 */

/*	public class myClickListener implements View.OnClickListener {

TextView tv = null;

myClickListener(TextView tv) {
	this.tv = tv;
}

@Override
public void onClick(View arg0) {
	if (this.tv.getVisibility() == View.INVISIBLE) {
		this.tv.setVisibility(View.VISIBLE);
	} else {
		this.tv.setVisibility(View.INVISIBLE);
	}
}
}*/

