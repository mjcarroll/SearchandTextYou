package com.nuspatial.geoqar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by michael on 2/25/17.
 */



    /* This is the class that listens to the sensors */
public class SensorMixer implements SensorEventListener, LocationListener {

    public final static String TAG = "SensorMixer";

    private SensorMixerCallback mSensorCb;
    static double DEG2RAD = 0.01745329251;


    public void registerCallback(SensorMixerCallback cb) {
        mSensorCb = cb;
    }


    // SensorEventListener stuff
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(TAG, "onAccuracyChanged");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.i(TAG, "onSensorChanged");

        //https://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrixFromVector(float[], float[])

        float [] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

        if(mSensorCb != null) {
            mSensorCb.onMatrix(rotationMatrix);
        }
    }

    // LocationListener stuff
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged: " + location.toString());

        if(mSensorCb != null) {
            mSensorCb.onLocation(new double[] {location.getLatitude() * DEG2RAD,
                                               location.getLongitude() * DEG2RAD,
                    location.getAltitude()});
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.i(TAG, "onStatusChanged");
    }

    public LocationManager locationManager;
    public SensorManager sensorManager;
}