package com.devapp.memoir;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

public class MultiDatePickerDialogFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {
	private TextView startDateTextView;
	private TextView endDateTextView;
	private Calendar startDate;
	private Calendar endDate;

	static final int DATE_DIALOG_ID = 0;

	private TextView activeDateTextView;
	private Calendar activeDate;

	/*public MultiDatePickerDialogFragment(TextView startDateTv,
			TextView endDateTv) {
		startDateTextView = startDateTv;
		endDateTextView = endDateTv;

		startDate = endDate = Calendar.getInstance();

		
		startDateTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDateDialog(startDateTextView, startDate);
			}
		});

		endDateTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDateDialog(endDateTextView, endDate);
			}
		});
	}*/

	private void updateDisplay(TextView dateDisplay, Calendar date) {
		dateDisplay.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(date.get(Calendar.MONTH) + 1).append("-")
				.append(date.get(Calendar.DAY_OF_MONTH)).append("-")
				.append(date.get(Calendar.YEAR)).append(" "));

	}

	public void showDateDialog(TextView dateDisplay, Calendar date) {
		activeDateTextView = dateDisplay;
		activeDate = date;
		//showDialog(DATE_DIALOG_ID);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		activeDate.set(Calendar.YEAR, year);
		activeDate.set(Calendar.MONTH, monthOfYear);
		activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		updateDisplay(activeDateTextView, activeDate);
		unregisterDateDisplay();
	}

	private void unregisterDateDisplay() {
		activeDateTextView = null;
		activeDate = null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new DatePickerDialog(getActivity(), this,
				activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH),
				activeDate.get(Calendar.DAY_OF_MONTH));
	}
}
