package com.devapp.memoir;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
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

		mArrayList.add(new SettingsItem("Set start date", MemoirApplication
				.convertDate(
						mPrefs.getLong("com.devapp.memoir.startselected", 0),
						"No Start Date Set")));
		mArrayList.add(new SettingsItem("Set end date", MemoirApplication
				.convertDate(
						mPrefs.getLong("com.devapp.memoir.endselected", 0),
						"No End Date Set")));

		mArrayList.add(new SettingsItem("Adjust view",
				"Hide days with single video", mPrefs.getBoolean(
						"com.devapp.memoir.showonlymultiple", false),
				new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						CheckBox cb = (CheckBox) arg0;
						mPrefs.edit()
								.putBoolean(
										"com.devapp.memoir.showonlymultiple",
										cb.isChecked()).commit();
					}

				}));

		mArrayList.add(new SettingsItem("Auto shoot",
				"Allow Memoir to auto shoot on call", mPrefs.getBoolean(
						"com.devapp.memoir.shootoncall", true),
				new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						CheckBox cb = (CheckBox) arg0;
						mPrefs.edit()
								.putBoolean("com.devapp.memoir.shootoncall",
										cb.isChecked()).commit();
					}

				}));

		mArrayList.add(new SettingsItem("Seconds to record", ""
				+ mPrefs.getInt("com.devapp.memoir.noofseconds", 2)));

		mArrayList.add(new SettingsItem("Reset", "Remove all videos"));

		mSettingsView = (ListView) findViewById(R.id.SettingLV);
		mSettingsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				if (position == 0) {

					if (mPrefs.getLong("com.devapp.memoir.startall", 0) == mPrefs
							.getLong("com.devapp.memoir.endselected", 0)) {
						Toast.makeText(SettingsActivity.this,
								"Only possible start date is the one selected",
								Toast.LENGTH_SHORT).show();
					} else {
						DatePickerFragment newFragment = new DatePickerFragment();
						newFragment.setDefaultDate(mPrefs.getLong(
								"com.devapp.memoir.startselected", 0), mPrefs
								.getLong("com.devapp.memoir.startall", 0),
								mPrefs.getLong("com.devapp.memoir.endselected",
										0), new setDateInterface() {
									@Override
									public void setDate(long d) {

										if (d < mPrefs
												.getLong(
														"com.devapp.memoir.startall",
														0)) {
											d = mPrefs
													.getLong(
															"com.devapp.memoir.startall",
															0);
										} else if (d > mPrefs
												.getLong(
														"com.devapp.memoir.endselected",
														0)) {
											d = mPrefs
													.getLong(
															"com.devapp.memoir.endselected",
															0);
										}
										mPrefs.edit()
												.putLong(
														"com.devapp.memoir.startselected",
														d)
												.putBoolean(
														"com.devapp.memoir.datachanged",
														true).commit();
										mArrayList.get(0).text2 = MemoirApplication
												.convertDate(d, "");
										mSettingsView.invalidateViews();

										Toast.makeText(
												SettingsActivity.this,
												"Setting start date to"
														+ MemoirApplication
																.convertDate(d,
																		""),
												Toast.LENGTH_SHORT).show();
									}
								});
						newFragment.show(
								SettingsActivity.this.getFragmentManager(),
								"datePicker");
					}
				} else if (position == 1) {
					if (mPrefs.getLong("com.devapp.memoir.startselected", 0) == mPrefs
							.getLong("com.devapp.memoir.endall", 0)) {
						Toast.makeText(SettingsActivity.this,
								"Only possible end date is the one selected",
								Toast.LENGTH_SHORT).show();
					} else {
						DatePickerFragment newFragment = new DatePickerFragment();
						newFragment.setDefaultDate(mPrefs.getLong(
								"com.devapp.memoir.endselected", 0), mPrefs
								.getLong("com.devapp.memoir.startselected", 0),
								mPrefs.getLong("com.devapp.memoir.endall", 0),
								new setDateInterface() {
									@Override
									public void setDate(long d) {
										if (d < mPrefs
												.getLong(
														"com.devapp.memoir.startselected",
														0)) {
											d = mPrefs
													.getLong(
															"com.devapp.memoir.startselected",
															0);
										} else if (d > mPrefs.getLong(
												"com.devapp.memoir.endall", 0)) {
											d = mPrefs.getLong(
													"com.devapp.memoir.endall",
													0);
										}

										mPrefs.edit()
												.putLong(
														"com.devapp.memoir.endselected",
														d)
												.putBoolean(
														"com.devapp.memoir.datachanged",
														true).commit();
										mArrayList.get(1).text2 = MemoirApplication
												.convertDate(d, "");
										mSettingsView.invalidateViews();

										Toast.makeText(
												SettingsActivity.this,
												"Setting end date to"
														+ MemoirApplication
																.convertDate(d,
																		""),
												Toast.LENGTH_SHORT).show();
									}
								});
						newFragment.show(
								SettingsActivity.this.getFragmentManager(),
								"datePicker");
					}
				} else if (position == 4) {
					NumberPickerFragment newFragment = new NumberPickerFragment();
					newFragment.setDefault(
							mPrefs.getInt("com.devapp.memoir.noofseconds", 1),
							new OnValueChangeListener() {

								@Override
								public void onValueChange(NumberPicker arg0,
										int arg1, int arg2) {
									mPrefs.edit()
											.putInt("com.devapp.memoir.noofseconds",
													arg1).commit();
									mArrayList.get(4).text2 = "" + arg1;
									mSettingsView.invalidateViews();

									Toast.makeText(SettingsActivity.this,
											"No of seconds set to " + arg1,
											Toast.LENGTH_SHORT).show();
								}
							});
					newFragment.show(
							SettingsActivity.this.getFragmentManager(),
							"numberPicker");
				} else if (position == 5) {
					MemoirApplication app = (MemoirApplication) SettingsActivity.this
							.getApplication();
					app.getDBA().resetAll();
					app.deleteMyLifeFile(getApplicationContext());
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		mSettingsAdapter = new SettingsArrayAdapter(this, mArrayList);
		mSettingsView.setAdapter(mSettingsAdapter);
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
			if (convertView == null) {
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
				cb.setTag(item);
				cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						SettingsItem item = (SettingsItem) arg0.getTag();
						item.checkboxValue = arg1;
						item.listener.onCheckedChanged(arg0, arg1);
					}
				});
				cb.setChecked(item.checkboxValue);
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

		long defaultDate = 0;
		long minDate = 0;
		long maxDate = 0;
		setDateInterface setDateI = null;

		public void setDefaultDate(long dD, long minDate, long maxDate,
				setDateInterface setDateI) {
			this.defaultDate = dD;
			this.minDate = minDate;
			this.maxDate = maxDate;
			this.setDateI = setDateI;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			int year = 0, month = 0, day = 0;

			if (this.defaultDate != 0) {
				day = (int) (defaultDate % 100);
				month = (int) ((defaultDate % 10000) / 100);
				year = (int) (defaultDate / 10000);
			} else {
				final Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
				month = c.get(Calendar.MONTH);
				day = c.get(Calendar.DAY_OF_MONTH);
			}

			DatePickerDialog dpDialog = new DatePickerDialog(getActivity(),
					this, year, month, day);
			DatePicker dp = dpDialog.getDatePicker();

			Calendar cal1 = Calendar.getInstance();
			cal1.set((int) (minDate / 10000),
					(int) ((minDate % 10000) / 100) - 1, (int) (minDate % 100));

			dp.setMinDate(cal1.getTimeInMillis() - 60 * 60 * 24 * 1000);

			Calendar cal2 = Calendar.getInstance();
			cal1.set((int) (maxDate / 10000),
					(int) ((maxDate % 10000) / 100) - 1, (int) (maxDate % 100));

			dp.setMaxDate(cal2.getTimeInMillis() + 60 * 60 * 24 * 1000);

			return dpDialog;
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			month++;
			this.setDateI.setDate(year * 10000 + month * 100 + day);
		}
	}

	public interface setDateInterface {
		public void setDate(long d);
	}

	public static class NumberPickerFragment extends DialogFragment {

		NumberPicker mNumberPicker = null;
		int defaultValue = 2;
		OnValueChangeListener listener = null;

		public void setDefault(int dValue, OnValueChangeListener l) {
			this.defaultValue = dValue;
			listener = l;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			mNumberPicker = new NumberPicker(getActivity());
			mNumberPicker.setMinValue(1);
			mNumberPicker.setMaxValue(5);
			mNumberPicker.setValue(defaultValue);
			builder.setView(mNumberPicker);

			builder.setNeutralButton("Set", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					mNumberPicker.getValue();
					listener.onValueChange(mNumberPicker,
							mNumberPicker.getValue(), 0);
				}
			});
			return builder.create();
		}
	}
}
