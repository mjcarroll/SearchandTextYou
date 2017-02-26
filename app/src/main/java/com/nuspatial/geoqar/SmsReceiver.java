package com.nuspatial.geoqar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by michael on 2/26/17.
 */

public class SmsReceiver   extends BroadcastReceiver
{
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
                    String[] fields = smsBody.split(";");

                    int id = Integer.parseInt(fields[0]);
                    float lat = Float.parseFloat(fields[1]);
                    float lon = Float.parseFloat(fields[2]);
                    String status = fields[3];

                    PointOfInterest poi = new PointOfInterest(id, new GeoPoint(lat, lon, -1), status);

                    Bundle extras = intent.getExtras();
                    Intent ii = new Intent("POI");
                    // Data you need to pass to activity
                    ii.putExtra("id", id);
                    ii.putExtra("lat", lat);
                    ii.putExtra("lon", lon);
                    ii.putExtra("status", status);
                    context.sendBroadcast(ii);
                    //points.add(poi);
                }
            }
        }
    }
}