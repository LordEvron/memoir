package com.devapp.memoir;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

public class MultiDatePicker extends DialogFragment implements OnDateSetListener{
	private Button startDateButton;
	private Button endDateButton;
	private Calendar startDate;
	private Calendar endDate;

	private Button activeDateButton;
	private Calendar activeDate;

	/*
	 * public MultiDatePicker(Button startDateBtn, Button endDateBtn) {
	 * startDateButton = startDateBtn; endDateButton = endDateBtn;
	 * 
	 * startDate = endDate = Calendar.getInstance(); }
	 */

	public MultiDatePicker () {
		startDateButton = (Button) getActivity().findViewById(
				R.id.startDateButton);
		endDateButton = (Button) getActivity().findViewById(R.id.endDateButton);

		startDate = endDate = Calendar.getInstance();
	}

	public void setActiveDate(View dateView) {
		int id = dateView.getId();

		if (id == R.id.startDateButton) {
			activeDateButton = startDateButton;
			activeDate = startDate;
		} else if (id == R.id.endDateButton) {
			activeDateButton = endDateButton;
			activeDate = endDate;
		}
	}

	private void updateDisplay(Button dateDisplay, Calendar date) {
		dateDisplay.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(date.get(Calendar.MONTH) + 1).append("-")
				.append(date.get(Calendar.DAY_OF_MONTH)).append("-")
				.append(date.get(Calendar.YEAR)).append(" "));

	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		activeDate.set(Calendar.YEAR, year);
		activeDate.set(Calendar.MONTH, monthOfYear);
		activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		updateDisplay(activeDateButton, activeDate);
		unregisterDateDisplay();
	}

	private void unregisterDateDisplay() {
		activeDateButton = null;
		activeDate = null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new DatePickerDialog(getActivity(), this,
				activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH),
				activeDate.get(Calendar.DAY_OF_MONTH));
	}
}
