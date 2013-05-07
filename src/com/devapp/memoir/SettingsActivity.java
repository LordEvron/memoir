package com.devapp.memoir;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	public ArrayList<SettingsItem> mArrayList = null;
	public SettingsArrayAdapter mSettingsAdapter = null;
	public ListView mSettingsView = null;
	public SharedPreferences mPrefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_settings);

		mPrefs = this.getSharedPreferences("com.devapp.memoir",
				Context.MODE_PRIVATE);

		mArrayList = new ArrayList<SettingsItem>();

		Log.d("zxc", "reading those values again");
		mArrayList.add(new SettingsItem("Set Start Date", mPrefs.getString(
				"com.devapp.memoir.startselected", "No Start Date Set")));
		mArrayList.add(new SettingsItem("Set End Date", mPrefs.getString(
				"com.devapp.memoir.endselected", "No End Date Set")));

		mArrayList.add(new SettingsItem("Show Rows With Multiple Videos Only",
				"You can disable rows which have only single video",
				mPrefs.getBoolean("com.devapp.memoir.showonlymultiple", false),
				new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						mPrefs.edit()
								.putBoolean(
										"com.devapp.memoir.showonlymultiple",
										arg1).commit();
					}

				}));

		mSettingsView = (ListView) findViewById(R.id.SettingLV);
		mSettingsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				Log.d("zxc", "position" + position);
				if (position == 0) {

					if (mPrefs.getString("com.devapp.memoir.startall", null)
							.equals(mPrefs.getString(
									"com.devapp.memoir.endselected", null))) {
						Toast.makeText(
								SettingsActivity.this,
								"Only possible start date is the one already selected",
								Toast.LENGTH_SHORT).show();
					} else {
						DatePickerFragment newFragment = new DatePickerFragment();
						newFragment.setDefaultDate(mPrefs.getString(
								"com.devapp.memoir.startselected", null),
								mPrefs.getString("com.devapp.memoir.startall",
										null), mPrefs.getString(
										"com.devapp.memoir.endselected", null),
								new setDateInterface() {
									@Override
									public void setDate(String s) {

										if (s.compareTo(mPrefs.getString(
												"com.devapp.memoir.startall",
												null)) < 0) {
											s = mPrefs
													.getString(
															"com.devapp.memoir.startall",
															null);
										} else if (s.compareTo(mPrefs
												.getString(
														"com.devapp.memoir.endselected",
														null)) > 0) {
											s = mPrefs
													.getString(
															"com.devapp.memoir.endselected",
															null);
										}
										mPrefs.edit()
												.putString(
														"com.devapp.memoir.startselected",
														s)
												.putBoolean(
														"com.devapp.memoir.datachanged",
														true).commit();
										mArrayList.get(0).text2 = s;
										mSettingsView.invalidateViews();

										Toast.makeText(
												SettingsActivity.this,
												"Setting Start Date To" + s,
												Toast.LENGTH_SHORT).show();
									}
								});
						newFragment.show(
								SettingsActivity.this.getFragmentManager(),
								"datePicker");
					}
				} else if (position == 1) {
					if (mPrefs.getString("com.devapp.memoir.startselected",
							null).equals(
							mPrefs.getString("com.devapp.memoir.endall", null))) {
						Toast.makeText(
								SettingsActivity.this,
								"Only possible end date is the one already selected",
								Toast.LENGTH_SHORT).show();
					} else {
						DatePickerFragment newFragment = new DatePickerFragment();
						newFragment.setDefaultDate(mPrefs.getString(
								"com.devapp.memoir.endselected", null), mPrefs
								.getString("com.devapp.memoir.startselected",
										null), mPrefs.getString(
								"com.devapp.memoir.endall", null),
								new setDateInterface() {
									@Override
									public void setDate(String s) {
										if (s.compareTo(mPrefs.getString(
												"com.devapp.memoir.startselected",
												null)) < 0) {
											s = mPrefs
													.getString(
															"com.devapp.memoir.startselected",
															null);
										} else if (s.compareTo(mPrefs
												.getString(
														"com.devapp.memoir.endall",
														null)) > 0) {
											s = mPrefs
													.getString(
															"com.devapp.memoir.endall",
															null);
										}

										mPrefs.edit()
												.putString(
														"com.devapp.memoir.endselected",
														s)
												.putBoolean(
														"com.devapp.memoir.datachanged",
														true).commit();
										mArrayList.get(1).text2 = s;
										mSettingsView.invalidateViews();
										
										Toast.makeText(
												SettingsActivity.this,
												"Setting End Date To" + s,
												Toast.LENGTH_SHORT).show();
									}
								});
						newFragment.show(
								SettingsActivity.this.getFragmentManager(),
								"datePicker");
					}
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mSettingsAdapter = new SettingsArrayAdapter(this, mArrayList);
		mSettingsView.setAdapter(mSettingsAdapter);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public class SettingsItem {
		String text1;
		String text2;
		boolean showCheckbox;
		boolean checkboxValue;
		OnCheckedChangeListener listener;

		public SettingsItem(String t1, String t2) {
			text1 = t1;
			text2 = t2;
			showCheckbox = false;
		}

		public SettingsItem(String t1, String t2, boolean cbValue,
				OnCheckedChangeListener l) {
			text1 = t1;
			text2 = t2;
			showCheckbox = true;
			checkboxValue = cbValue;
			listener = l;
		}

	}

	public class SettingsArrayAdapter extends
			ArrayAdapter<ArrayList<SettingsItem>> {

		private ArrayList<SettingsItem> mList;
		private LayoutInflater mInflater;

		public SettingsArrayAdapter(Context context,
				ArrayList<SettingsItem> List) {
			super(context, R.layout.activity_settings_item);

			this.mList = List;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d("asd", "in getView for position " + position);

			if (convertView == null) {
				Log.d("asd", "convertView turned out to be null ");
				convertView = mInflater.inflate(
						R.layout.activity_settings_item, null);
			}

			SettingsItem item = this.mList.get(position);
			((TextView) convertView.findViewById(R.id.SettingsItemTV1))
					.setText(item.text1);
			((TextView) convertView.findViewById(R.id.SettingsItemTV2))
					.setText(item.text2);

			CheckBox cb = (CheckBox) convertView
					.findViewById(R.id.SettingsItemCB);
			if (item.showCheckbox) {
				cb.setChecked(item.checkboxValue);
				cb.setOnCheckedChangeListener(item.listener);
				cb.setVisibility(View.VISIBLE);
			} else {
				cb.setVisibility(View.INVISIBLE);
			}

			return convertView;
		}

		@Override
		public int getCount() {
			if (mList == null)
				return 0;
			return mList.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		String defaultDate = null;
		String minDate = null;
		String maxDate = null;
		setDateInterface setDateI = null;

		public void setDefaultDate(String dD, String minDate, String maxDate,
				setDateInterface setDateI) {
			this.defaultDate = dD;
			this.minDate = minDate;
			this.maxDate = maxDate;
			this.setDateI = setDateI;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			int year = 0, month = 0, day = 0;

			if (this.defaultDate != null) {
				day = Integer.parseInt(defaultDate.substring(0, 2));
				month = Integer.parseInt(defaultDate.substring(3, 5)) - 1;
				year = Integer.parseInt(defaultDate.substring(6));
				Log.d("zxc", "Date is >" + year + month + day);
			} else {
				// Use the current date as the default date in the picker
				final Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
				month = c.get(Calendar.MONTH);
				day = c.get(Calendar.DAY_OF_MONTH);

			}

			Calendar cal1 = Calendar.getInstance();
			cal1.set(Integer.parseInt(minDate.substring(6)),
					Integer.parseInt(minDate.substring(3, 5)) - 1,
					Integer.parseInt(minDate.substring(0, 2)));

			Log.d("asd", "Min Date is > " + cal1.getTimeInMillis()
					+ " where min date is >" + minDate);

			Calendar cal2 = Calendar.getInstance();
			cal2.set(Integer.parseInt(maxDate.substring(6)),
					Integer.parseInt(maxDate.substring(3, 5)) - 1,
					Integer.parseInt(maxDate.substring(0, 2)));
			Log.d("asd", "Max Date is > " + cal2.getTimeInMillis()
					+ "  Wjere max date is > " + maxDate);

			DatePickerDialog dpDialog = new DatePickerDialog(getActivity(),
					this, year, month, day);
			DatePicker dp = dpDialog.getDatePicker();
			dp.setMinDate(cal1.getTimeInMillis() - 60 * 60 * 24 * 1000);
			dp.setMaxDate(cal2.getTimeInMillis() + 60 * 60 * 24 * 1000);

			return dpDialog;
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			String d = null, m = null;
			month++;
			d = (day < 10) ? ("0" + day) : ("" + day);
			m = (month < 10) ? ("0" + month) : ("" + month);
			this.setDateI.setDate(d + "/" + m + "/" + year);
		}
	}

	public interface setDateInterface {
		public void setDate(String s);
	}
}
