package com.devapp.memoir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class EndUserLicenseAgreement {

	private Activity mActivity;

	public EndUserLicenseAgreement(Activity context) {
		mActivity = context;
	}

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = mActivity.getPackageManager().getPackageInfo(
					mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	public void show(final Dialog.OnClickListener Accept,
			final Dialog.OnClickListener Decline) {
		PackageInfo versionInfo = getPackageInfo();

		String title = mActivity.getString(R.string.app_name) + " v"
				+ versionInfo.versionName;

		String message = mActivity.getString(R.string.eula);

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
				.setTitle(title).setMessage(message)
				.setPositiveButton("Accept", new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						Accept.onClick(dialogInterface, i);
					}
				}).setNegativeButton("Decline", new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Decline.onClick(dialog, which);
					}
				});
		builder.create().show();
	}
}