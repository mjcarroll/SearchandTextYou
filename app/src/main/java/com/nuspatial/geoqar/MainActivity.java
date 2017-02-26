package com.nuspatial.geoqar;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import android.location.Location;
import android.location.LocationManager;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PointOfInterestClickInterface {
    private FrameLayout container;
    private SensorMixer mixer;
    private SurfaceView surfaceView;
    private Overlay overlay;
    private CameraOp disp;

    static final Integer LOCATION = 0x01;
    static final Integer CAMERA = 0x02;
    static final Integer SEND_SMS = 0x03;
    static final Integer RECEIVE_SMS = 0x04;

    static double DEG2RAD = 0.01745329251;

    public List<PointOfInterest> points;

    public void onClick(PointOfInterest point) {
        Log.i("MainActivity", "onClick");
        DetailFragment newFragment = new DetailFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main_activity);

        container = (FrameLayout) findViewById(R.id.container);

        mixer = new SensorMixer();
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.setKeepScreenOn(true);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mixer.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            Log.i("MainActivity", "Requesting location");
            mixer.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 0, mixer);

            Location cur = mixer.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            final double curLat = cur.getLatitude() * DEG2RAD;
            final double curLon = cur.getLongitude() * DEG2RAD;
            final double curAlt = cur.getAltitude();

            points = new ArrayList<PointOfInterest>() {{
                add(new PointOfInterest(new GeoPoint(curLat + 1e-5,curLon, curAlt), "north"));
                add(new PointOfInterest(new GeoPoint(curLat - 1e-5,curLon, curAlt), "south"));
                add(new PointOfInterest(new GeoPoint(curLat,curLon+1e-5, curAlt), "east"));
                add(new PointOfInterest(new GeoPoint(curLat,curLon-1e-5, curAlt), "west"));
                add(new PointOfInterest(new GeoPoint(curLat,curLon, curAlt+10), "up"));
                add(new PointOfInterest(new GeoPoint(curLat,curLon, curAlt-10), "down"));

                //add(new PointOfInterest(new GeoPoint(
                //        34.749184 * DEG2RAD, -86.583051 * DEG2RAD, 105.0), "test"));
            }};
        } else {

            points = new ArrayList<PointOfInterest>() {{
                add(new PointOfInterest(new GeoPoint(
                        (34.747122 + 1e-5) * DEG2RAD, -86.581974 * DEG2RAD, -1), "north"));
                add(new PointOfInterest(new GeoPoint(
                        (34.747122 - 1e-5) * DEG2RAD, -86.581974 * DEG2RAD, -1), "south"));
                add(new PointOfInterest(new GeoPoint(
                        (34.747122) * DEG2RAD, (-86.581974 - 1e-5) * DEG2RAD, -1), "east"));
                add(new PointOfInterest(new GeoPoint(
                        (34.747122) * DEG2RAD, (-86.581974 + 1e-5) * DEG2RAD, -1), "west"));


                //add(new PointOfInterest(new GeoPoint(
                //        34.749184 * DEG2RAD, -86.583051 * DEG2RAD, 105.0), "test"));
            }};
        }

        overlay = new Overlay(this.getApplicationContext());
        overlay.setRenderPoints(points);
        overlay.setCallback(this);
        container.addView(overlay);


        // This is req'd for Nougat, thanks T-mobile
        // https://www.sitepoint.com/requesting-runtime-permissions-in-android-m-and-n/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION);
            askForPermission(Manifest.permission.CAMERA, CAMERA);
            askForPermission(Manifest.permission.SEND_SMS, SEND_SMS);
            askForPermission(Manifest.permission.RECEIVE_SMS, RECEIVE_SMS);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mixer.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            mixer.sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

            Log.i("MainActivity", "Requesting location");
            mixer.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 0, mixer);

            Location cur = mixer.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.i("MainActivity", cur.toString());
            overlay.onLocation(new double[]{cur.getLatitude()*DEG2RAD, cur.getLongitude()*DEG2RAD, cur.getAltitude()});

            mixer.sensorManager.registerListener(mixer,
                    mixer.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    SensorManager.SENSOR_DELAY_FASTEST);


            mixer.registerCallback(overlay);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if(disp == null) {
                disp = new CameraOp(this, surfaceView);
            }
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if(ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == LOCATION) {
                Toast.makeText(this, "Location Permission granted", Toast.LENGTH_SHORT).show();
            } else if(requestCode == CAMERA) {
                Toast.makeText(this, "Camera Permission granted", Toast.LENGTH_SHORT).show();
            } else if(requestCode == SEND_SMS) {
                Toast.makeText(this, "Send SMS Permission granted", Toast.LENGTH_SHORT).show();
            } else if(requestCode == RECEIVE_SMS) {
                Toast.makeText(this, "Receive SMS Permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
