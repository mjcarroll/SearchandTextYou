package com.nuspatial.geoqar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by michael on 2/25/17.
 */

public class SmsReceiver extends BroadcastReceiver {
    public static final String SMS_BUNDLE = "pdus";
    public static final String TAG = "SmsReceiver";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();

                String str = "SMS From: " + address + ": " + smsBody + "\n";
                Log.d(TAG, str);

                if( address.equals("+12566661447"))
                {
                    Log.d(TAG, "abortBroadcast");
                    abortBroadcast();
                }
            }
        }
    }
}
