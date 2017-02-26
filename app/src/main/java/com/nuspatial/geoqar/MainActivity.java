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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PointOfInterestClickInterface, GoogleApiClient.ConnectionCallbacks{
    private FrameLayout container;
    private SensorMixer mixer;
    private SurfaceView surfaceView;
    private Overlay overlay;
    private CameraOp disp;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;


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

    int RQS_GooglePlayServices=0;

    @Override
    protected void onStart() {
        super.onStart();

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        } else {
            googleAPI.getErrorDialog(this,resultCode, RQS_GooglePlayServices);
        }
    }

    public void onClick(PointOfInterest point) {
        Log.i("MainActivity", "onClick");

        DetailFragment newFragment = new DetailFragment();
        newFragment.setPOI(point);
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


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        final double curLat;
        final double curLon;
        final double curAlt;

        curLat = 34.747020 * DEG2RAD;
        curLon = -86.581826 * DEG2RAD;
        curAlt = 210;

        points = new ArrayList<PointOfInterest>();
        overlay = new Overlay(this.getApplicationContext());
        overlay.setRenderPoints(points);
        overlay.setCallback(this);
        container.addView(overlay);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mixer.sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
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

    @Override
    public void onConnectionSuspended(int v) {

    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i("MainWindows", "Connected to GoogleApiClient");
        Toast.makeText(this, "ApiClient", Toast.LENGTH_SHORT).show();


        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();


            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    mixer);
            Location cur = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            float curLat = (float) (cur.getLatitude() * DEG2RAD);
            float curLon = (float) (cur.getLongitude() * DEG2RAD);
            float curAlt = (float) cur.getAltitude();

            /*
            float curLat = (float)(34.747020 * DEG2RAD);
            float curLon = (float)(-86.581826 * DEG2RAD);
            float curAlt = 210;
            */
            overlay.setCurrentLocation(new GeoPoint(curLat, curLon, curAlt));


            overlay.mRenderPoints.add(new PointOfInterest(0, new GeoPoint(curLat + 1e-6, curLon, -1), "north"));
            overlay.mRenderPoints.add(new PointOfInterest(0, new GeoPoint(curLat - 1e-6, curLon, -1), "south"));
            overlay.mRenderPoints.add(new PointOfInterest(0, new GeoPoint(curLat, curLon+1e-6, -1), "west"));
            overlay.mRenderPoints.add(new PointOfInterest(0, new GeoPoint(curLat, curLon-1e-6, -1), "east"));
        }
    }

    private void sendSMS(String status) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            float lat = (float) (this.overlay.mCurLoc.mLat / DEG2RAD);
            float lon = (float) (this.overlay.mCurLoc.mLon / DEG2RAD);

            String payload = Integer.toString(ID) + ";" + Double.toString(lat) + ";" + Double.toString(lon) + ";" + status;
            ID = ID + 1;
            Log.i("MainActivity", payload);

            SmsManager sms = SmsManager.getDefault();
            Log.i("MainActivity", this.overlay.mCurLoc.toString());

            if(false) {
                PointOfInterest p = new PointOfInterest(ID, new GeoPoint(this.overlay.mCurLoc.mLat, this.overlay.mCurLoc.mLon, -1), status);
                this.overlay.mRenderPoints.add(p);
            } else {
                sms.sendTextMessage("12566661447", null, payload, null, null);
                Toast.makeText(this, "Sent SMS", Toast.LENGTH_SHORT).show();
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
        if (permissions.length < 1) {
            return;
        }

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
