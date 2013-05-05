package com.devapp.memoir;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

		setContentView(R.layout.activity_help);

		mList = (ExpandableListView) findViewById(R.id.ActivityHelpELV);    
		ExpandableListAdapter adapter = new MyExpandableListAdapter(this);
		mList.setAdapter(adapter);
	}

	public class MyExpandableListAdapter extends BaseExpandableListAdapter {
		private int[] groups = { R.string.help_mt1 , R.string.help_mt2, R.string.help_mt3, R.string.help_mt4, R.string.help_mt5, R.string.help_mt6 };
		private int[][] children = { { R.string.help_t1}, { R.string.help_t2}, { R.string.help_t3}, { R.string.help_t4}, { R.string.help_t5}, { R.string.help_t6}};

		private Context cxt;
		private Resources res;

		public MyExpandableListAdapter(Context cxt) {
			this.cxt = cxt;
			this.res = this.cxt.getResources();
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
			TextView tv = getGenericView();
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

		public TextView getGenericView() {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, 64);

			TextView tv = new TextView(this.cxt);
			tv.setLayoutParams(lp);

			// Center the text vertically
			tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			// Set the text starting position
			tv.setPadding(36, 0, 0, 0);
			return tv;
		}

		@Override
		public View getGroupView(int groupPos, boolean isExpanded,
				View convertView, ViewGroup parent) {
			
			TextView tv = getGenericView();
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

