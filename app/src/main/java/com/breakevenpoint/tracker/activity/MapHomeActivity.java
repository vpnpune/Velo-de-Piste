package com.breakevenpoint.tracker.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.breakevenpoint.model.AthleteLocation;
import com.breakevenpoint.tracker.BuildConfig;
import com.breakevenpoint.tracker.R;
import com.breakevenpoint.tracker.service.LocationUpdateService;
import com.breakevenpoint.tracker.util.Utils;
import com.bumptech.glide.util.Util;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHomeActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    GoogleMap mMap;
    MarkerOptions markerOptions;
    LatLng latLng;
    Button btn_track_ride;
    Button btn_stop_track;
    //btnStopTrack

    //location provider
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = MapHomeActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private float zoomLevel = 15;
    private int animateSec = 5000;

    // A reference to the service used to get location updates.
    private LocationUpdateService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;
    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    private View mLayout;

    public static  String RIDER_NAME;
    public static String RIDER_NUMBER="";


    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdateService.LocalBinder binder = (LocationUpdateService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        Log.e(TAG, "On Create");
        setContentView(R.layout.activity_map_home);
        mLayout = findViewById(R.id.activity_maphome);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            String riderName = (String) b.getString("riderName");
            String riderNumber = (String) b.getString("riderNumber");

            RIDER_NAME =riderName;
            RIDER_NUMBER=riderNumber;
            Toast.makeText(this.getApplicationContext(), riderName, Toast.LENGTH_LONG).show();
        }

        if (!checkPermissions())
                    requestPermissions();

/*
        if (Utils.requestingLocationUpdates(this)) {
            Log.e(TAG, "            Checking Permission");
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
*/


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_track_ride = (Button) findViewById(R.id.btnTrackRace);
        btn_track_ride.setOnClickListener(this);
        btn_stop_track = (Button) findViewById(R.id.btnStopTrack);
        btn_stop_track.setOnClickListener(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        LinearLayout buttonContainer =findViewById(R.id.activity_maphome);
        ViewGroup.LayoutParams params1 = buttonContainer.getLayoutParams();
        params1.height = height*15/100;
        buttonContainer.setLayoutParams(params1);


        LinearLayout mapContainer =findViewById(R.id.map_container);
        ViewGroup.LayoutParams params = mapContainer.getLayoutParams();
        params.height = height*85/100;
        mapContainer.setLayoutParams(params);
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "On Start");

        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);


        // old code
/*if (!checkPermissions()) {
            requestPermissions();
        }*/

        // Restore the state of the buttons when the activity (re)launches.
        //setButtonsState(Utils.requestingLocationUpdates(this));
        if(Utils.requestingLocationUpdates(this)){
            if(latLng!=null)
            setMapMarker(latLng.latitude,latLng.longitude);

            Snackbar.make(mLayout, "Tracking is ON, Please Wait While It Sync with GPS", Snackbar.LENGTH_LONG)
                    .setAction("OK", null).show();


        }


        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        Log.i(TAG,"Before BindService");
        bindService(new Intent(this, LocationUpdateService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        Log.i(TAG,"After BindService");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "OnMapreadyCalled");
        mMap = googleMap;

        // TODO: Before enabling the My Location layer, you must request
        // location permission from the user. This sample does not include
        // a request for location permission.
  /*      if (!checkPermissions())
            requestPermissions();
*/

        if (!checkPermissions()) {
            requestPermissions();
        }
        Log.e(TAG, "Permission VALUE" + checkPermissions());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }


        /*if (Utils.requestingLocationUpdates(this)) {
            Log.e(TAG, "            Checking Permission");

        }
*/

        Log.e(TAG, "PERMISSION Already Granted");
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //map.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
*/

        //provideLocation();

        /*if(latLng!=null){

        }else{
            //Add a marker in Sydney, Australia, and move the camera.
            LatLng sydney = new LatLng(-34, 151);
            map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));
        }*/

        //Add a marker in Sydney, Australia, and move the camera.
        // LatLng sydney = new LatLng(-34, 151);
        // map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdateService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTrackRace:

                Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show();
                // initiate Service
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();

                }
                break;


            case R.id.btnStopTrack:
               /* Snackbar.make(v, getString(R.string.snackbar_forgot_password), Snackbar.LENGTH_LONG)
                        .setAction("^_^", null).show();*/
                Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show();
                mService.removeLocationUpdates();

                break;

        }
    }


    // provide location method
    @SuppressWarnings("MissingPermission")
    private void provideLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            double lat = task.getResult().getLatitude();
                            double lg = task.getResult().getLongitude();
                            showSnackbar("Lat : " + lat + " Long : " + lg);
                            latLng = new LatLng(lat, lg);
                            //Add a marker in Sydney, Australia, and move the camera.

                            mMap.addMarker(new MarkerOptions().position(latLng).title("Location Accquired"));
                            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel), animateSec, null);

                            // displAY
                            /*TextView textLatitude = (TextView) findViewById(R.id.textViewLat);
                            textLatitude.setText("Latitude " + String.valueOf(lat));
                            TextView textLongitude = (TextView) findViewById(R.id.textViewLong);
                            textLongitude.setText("Longitude " + String.valueOf(lg));*/
                            // create
                          /*  UserLocation data = new UserLocation();
                            data.setBibNo("RQ-069");
                            data.setLastUpdated(new Date());
                            data.setLat(lat);
                            data.setLongitude(lg);
                            data.setRiderName("Neeraj Vishwakarma");
                            data.setUserId(userId);*/
                            //send
                            //sendData(data,"location/submitLoc");
                            //sendDataGet(data, "location/submitLocGET?");


                        } else {
                            Log.w(getClass().getName(), "getLastLocation:exception", task.getException());

                            //showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });

    }


    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Snackbar.make(
                    findViewById(R.id.activity_maphome),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MapHomeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
            Log.e(TAG, "Displaying permission rationale to provide additional context.");
        } else {
            Log.e(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MapHomeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void showSnackbar(final String text) {
        Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_LONG).show();

    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
                MapHomeActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.e(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                // mService.requestLocationUpdates();
            } else {
                // Permission denied.

                Log.e(TAG, "Permission Denied showing snackbar.");
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_maphome),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdateService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdateService.EXTRA_LOCATION);
            Log.e(TAG,"from Activity " +location.toString());
            if(location!=null){
                latLng = setMapMarker(location.getLatitude(),location.getLongitude());
                AthleteLocation data = new AthleteLocation(null, new Date(),"loginId",MapHomeActivity.RIDER_NUMBER,"demoLocationId");
                data.setLat(location.getLatitude());
                data.setLongitude(location.getLongitude());
                data.setRiderName(MapHomeActivity.RIDER_NAME);
                data.setLastUpdated(new Date());
                Log.e(TAG,data.toString());
                sendDataGet(data,"location/submitLocGET");

            }

            if (location != null) {
                Toast.makeText(MapHomeActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
           /* Toast.makeText(MapHomeActivity.this, "True",
                    Toast.LENGTH_SHORT).show();*/
            btn_track_ride.setEnabled(false);
            btn_stop_track.setEnabled(true);

            // mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            /*Toast.makeText(MapHomeActivity.this, "False",
                    Toast.LENGTH_SHORT).show();*/
            btn_track_ride.setEnabled(true);
            btn_stop_track.setEnabled(false);
            //mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }

    private LatLng setMapMarker(double lt, double lg){
        latLng = new LatLng(lt, lg);
        //Add a marker in Sydney, Australia, and move the camera.

        mMap.addMarker(new MarkerOptions().position(latLng).title(RIDER_NAME + "("+RIDER_NUMBER+")"));
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel), animateSec, null);
        return latLng;
    }
    //"http://localhost:8090/root/location/submitLoc"
    /*private void sendData(AthleteLocation data, String path){
        GsonBuilder gsonBuilder = new GsonBuilder();
        // "Mar 5, 2018 4:28:12 PM
        //gsonBuilder.setDateFormat("MMM MM-dd hh:mm:ss a");
        gsonBuilder.setDateFormat("dd MMM yyyy HH:mm:ss");
        Gson gson = gsonBuilder.create();
        String input =  gson.toJson(data);
        //String baseUrl = getResources().getString(R.string.app_server_path);
        String baseUrl = getApplicationContext().getString(R.string.app_server_path);
        Log.e(TAG,input);
        String serverURL = baseUrl + "/"+path;
        Toast.makeText(this,serverURL,Toast.LENGTH_SHORT);
        //Log.i(getClass().getName(), serverURL);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Create http cliient object to send request to server
        try {
           *//* HttpURLConnection url = (HttpURLConnection) new URL(serverURL).openConnection();
            url.connect();*//*

            URL url = new URL(serverURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("locationJSON", input);
            conn.setRequestProperty("Content-Type", "text/plain");

            Log.i(getClass().getName(),String.valueOf(conn.getResponseCode()));
            Log.i(getClass().getName(),conn.getResponseMessage());
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                Log.i(getClass().getName(),String.valueOf(output));
            }
            br.close();
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(getClass().getName(), e.getMessage());

        }
    }*/
    private void sendDataGet(AthleteLocation mockObj, String path) {
        String baseUrl = getResources().getString(R.string.app_server_path);
        StringBuilder stringBuilder = new StringBuilder(baseUrl + "/" + path+"?");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("lat", mockObj.getLat());
        parameters.put("lg", mockObj.getLongitude());
        parameters.put("lastUpdated", mockObj.getLastUpdated().getTime());
        parameters.put("bibNo", mockObj.getBibNo());
        parameters.put("userId", mockObj.getBibNo());
        parameters.put("riderName", mockObj.getRiderName());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            stringBuilder.append(getParamsString(parameters));

            URL url = new URL(stringBuilder.toString());
            Log.i(getClass().getName(), stringBuilder.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            Log.i(getClass().getName(), String.valueOf(conn.getResponseCode()));
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Toast.makeText(this.getApplicationContext(), "Server is down, Trying 1, 2, 3", Toast.LENGTH_LONG).show();

                //throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
            br.close();

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(getClass().getName(), e.getMessage());
        }


    }
    public static String getParamsString(Map<String, Object> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }


}
