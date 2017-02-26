package com.nuspatial.geoqar;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import android.location.Location;
import android.location.LocationManager;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
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

    int ID = 0;

    static final Integer LOCATION = 0x01;
    static final Integer CAMERA = 0x02;
    static final Integer SEND_SMS = 0x03;
    static final Integer RECEIVE_SMS = 0x04;

    static double DEG2RAD = 0.01745329251;

    public List<PointOfInterest> points;

    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();

            Integer id = b.getInt("id");
            String status = b.getString("status");
            Float lat = b.getFloat("lat") * (float)DEG2RAD;
            Float lon = b.getFloat("lon") * (float)DEG2RAD;

            Log.i("Receiver", "received poi");
            PointOfInterest poi = new PointOfInterest(id, new GeoPoint(lat, lon, -1), status);
            points.add(poi);
        }
    };


    public void onClick(PointOfInterest point) {
        Log.i("MainActivity", "onClick");

        DetailFragment newFragment = new DetailFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        newFragment.setPOI(point);
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
        container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

        // This is req'd for Nougat, thanks T-mobile
        // https://www.sitepoint.com/requesting-runtime-permissions-in-android-m-and-n/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION);
            askForPermission(Manifest.permission.CAMERA, CAMERA);
            askForPermission(Manifest.permission.SEND_SMS, SEND_SMS);
            askForPermission(Manifest.permission.RECEIVE_SMS, RECEIVE_SMS);
        }

        mixer = new SensorMixer();
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.setKeepScreenOn(true);

        Location cur = null;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mixer.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            mixer.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 0, mixer);
            cur = mixer.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }

        final double curLat;
        final double curLon;
        final double curAlt;

        if (cur != null) {
            curLat = cur.getLatitude() * DEG2RAD;
            curLon = cur.getLongitude() * DEG2RAD;
            curAlt = cur.getAltitude();
        } else {
            curLat = 34.74122 * DEG2RAD;
            curLon = -86.58194 * DEG2RAD;
            curAlt = 210;
        }

        points = new ArrayList<PointOfInterest>() {{
            /*
            add(new PointOfInterest(0, new GeoPoint(curLat + 1e-5,curLon, curAlt), "north"));
            add(new PointOfInterest(1, new GeoPoint(curLat - 1e-5,curLon, curAlt), "south"));
            add(new PointOfInterest(2, new GeoPoint(curLat,curLon+1e-5, curAlt), "east"));
            add(new PointOfInterest(3, new GeoPoint(curLat,curLon-1e-5, curAlt), "west"));
            add(new PointOfInterest(4, new GeoPoint(curLat,curLon, curAlt+10), "up"));
            add(new PointOfInterest(5, new GeoPoint(curLat,curLon, curAlt-10), "down"));*/

            //add(new PointOfInterest(new GeoPoint(
            //        34.749184 * DEG2RAD, -86.583051 * DEG2RAD, 105.0), "test"));
        }};

        overlay = new Overlay(this.getApplicationContext());
        overlay.setRenderPoints(points);
        overlay.setCallback(this);
        container.addView(overlay);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mixer.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            mixer.sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

            Log.i("MainActivity", "Requesting location");
            mixer.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 0, mixer);
            mixer.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 0, mixer);
            if(cur != null) {
                overlay.onLocation(new double[]{cur.getLatitude() * DEG2RAD, cur.getLongitude() * DEG2RAD, cur.getAltitude()});
            }

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

        registerReceiver(broadcastReceiver, new IntentFilter("POI"));


        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose Condition")
                        .setItems(R.array.conditions_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) sendSMS("Green");
                                if (which == 1) sendSMS("Yellow");
                                if (which == 2) sendSMS("Red");
                                if (which == 3) sendSMS("Black");
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void sendSMS(String status) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            float lat = (float) (this.overlay.mCurLoc.mLat / DEG2RAD);
            float lon = (float) (this.overlay.mCurLoc.mLon / DEG2RAD);
            Location cur = mixer.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            String payload = Integer.toString(ID) + ";" + Double.toString(lat) + ";" + Double.toString(lon) + ";" + status;
            ID = ID + 1;
            Log.i("MainActivity", payload);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("12566661447", null, payload, null, null);
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
