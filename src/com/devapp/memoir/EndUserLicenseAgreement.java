package com.devapp.memoir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class EndUserLicenseAgreement {

	private String EULA_PREFIX = "eula_";
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

		// the eulaKey changes every time you increment the version number in
		// the AndroidManifest.xml
		final String eulaKey = EULA_PREFIX + versionInfo.versionCode;

		// Show the Eula
		String title = mActivity.getString(R.string.app_name) + " v"
				+ versionInfo.versionName;

		// Includes the updates as well so users know what changed.
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