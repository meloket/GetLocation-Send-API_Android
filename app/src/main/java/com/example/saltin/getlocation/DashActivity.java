package com.example.saltin.getlocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.location.LocationManager.GPS_PROVIDER;

public class DashActivity extends AppCompatActivity {

    private EditText edi_cu_lat;
    private EditText edi_cu_long;
    private EditText edi_te_lat;
    private EditText edi_te_long;
    private Button bt_cu_submit;
    private Button bt_te_submit;

    private TextView lab_status;

    GPS_Service gps;
    public boolean mBound = false;
    LocationManager locationManager;
    private BroadcastReceiver broadcastReceiver;
    GPS_Service.onGPSLocationChangeListener mGpsListener;


    public static final String LOG_TAG = "MockGpsProviderActivity";
//    private static final String MOCK_GPS_PROVIDER_INDEX = "GpsMockProviderIndex";
//
//    private MockGpsProvider mMockGpsProviderTask = null;
//    private Integer mMockGpsProviderIndex = 0;
    public GPS_Service consumerService; //consumerService for registeration.
        private ServiceConnection mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
    // We've bound to LocalService, cast the IBinder and get LocalService instance
                GPS_Service.GPSBinder binder = (GPS_Service.GPSBinder) service;
                consumerService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };

    private Location getLastBestLocation() {

        @SuppressLint("MissingPermission") Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        @SuppressLint("MissingPermission") Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface arg0, int arg1) {

                        finishAffinity(); // Close all activites
                        System.exit(0);
                    }
                }).create().show();
    }


    //@SuppressLint("MissingPermission")
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);



        edi_cu_lat = (EditText) findViewById(R.id.lat_cu_edit);
        edi_cu_long = (EditText) findViewById(R.id.long_cu_edit);
        edi_te_lat = (EditText) findViewById(R.id.lat_te_edit);
        edi_te_lat.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        edi_te_long = (EditText) findViewById(R.id.long_te_edit);
        edi_te_long.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        bt_cu_submit = (Button) findViewById(R.id.act_cu_button);
        bt_te_submit = (Button) findViewById(R.id.act_te_button);
        //lab_status = (TextView) findViewById(R.id.label_status);

        if (!runtime_permissions())
            enable_buttons();

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        gps = new GPS_Service(this);
        mGpsListener = new GPS_Service.onGPSLocationChangeListener() {
            @Override
            public void onChangedLocation() {
                //Toast.makeText(getApplicationContext(),"LOCATION:CHANGED",Toast.LENGTH_SHORT).show();
                setUI();
            }
        };
        gps.setmListener(mGpsListener);
        gps.updateLocation();
        setUI();
    }

    private void setUI(){
        if(gps.canGetLocation()) {

            String latitude = String.valueOf(gps.getLatitude());
            String longitude = String.valueOf(gps.getLongitude());

            //lab_status.setText("Activated now: ("+latitude+", "+longitude+")");
            edi_cu_lat.setText(latitude);
            edi_cu_long.setText(longitude);
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }
    }




    @SuppressLint({"MissingPermission", "NewApi"})
    private void setTemporaryGPS(double s_latitude, double s_longitude) {

        try {
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy( Criteria.ACCURACY_FINE );

            String mocLocationProvider = LocationManager.GPS_PROVIDER;//lm.getBestProvider( criteria, true );

            if ( mocLocationProvider == null ) {
                Toast.makeText(getApplicationContext(), "No location provider found!", Toast.LENGTH_SHORT).show();
                return;
            }

            lm.addTestProvider(mocLocationProvider, false, false,
                        false, false, true, true, true, 0, 5);

            Location loc = new Location(mocLocationProvider);
            Location mockLocation = new Location(mocLocationProvider); // a string
            mockLocation.setLatitude(s_latitude);  // double
            mockLocation.setLongitude(s_longitude);
            mockLocation.setAltitude(loc.getAltitude());
            mockLocation.setAccuracy(1);
            mockLocation.setTime(System.currentTimeMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }

            lm.setTestProviderEnabled(mocLocationProvider, true);
            lm.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE,null, System.currentTimeMillis());

            lm.setTestProviderLocation( mocLocationProvider, mockLocation);
            loc = locationManager.getLastKnownLocation(mocLocationProvider);
            gps.updateLocation();
            Thread.sleep(500);
            Toast.makeText(getApplicationContext(), "Temporary Geo Location Set", Toast.LENGTH_SHORT).show();
            return;
        } catch (Exception e) {
            Toast.makeText(this, "Not activated the MockProvider!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
    }



    private int check_input(){

        if(edi_te_lat.getText().toString().equals("")){
            return 1;
        }
        if(edi_te_long.getText().toString().equals("")){
            return 2;
        }
        return 0;
    }


    private void enable_buttons() {
        //Toast.makeText(getApplicationContext(),"Please wait...", Toast.LENGTH_LONG).show();

        bt_te_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (check_input() == 1) {
                    Toast.makeText(getApplicationContext(), "Please input latitude.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (check_input() == 2) {
                    Toast.makeText(getApplicationContext(), "Please input longitude.", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTemporaryGPS(Double.valueOf(edi_te_lat.getText().toString()), Double.valueOf(edi_te_long.getText().toString()));
            }
        });

        bt_cu_submit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"MissingPermission", "NewApi"})
            @Override
            public void onClick(View view) {
                try{
                    if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null){
                        Log.i("REMOVE", "test provider");

                        //

                        locationManager.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
                        locationManager.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
                        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false);
                        locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);

                        //Settings.Secure.putInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0);

                        gps.updateLocation();
                        //LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                        //Location realloc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        //boolean isMock = false;
                        //if (android.os.Build.VERSION.SDK_INT >= 18) {
                            //isMock = realloc.isFromMockProvider();
                        //} else {
                            //isMock = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
                        //}
                        //gps.setLocation(realloc);
                        //setUI();
                        Toast.makeText(getApplicationContext(), "Restored Location successfully",Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),"RESTORE ERROR!", Toast.LENGTH_LONG).show();
                }


//                finishAffinity(); // Close all activites
//                System.exit(0);
            }
        });
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_SECURE_SETTINGS}, 100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permissions();
            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
// Bind to LocalService
        Intent intent = new Intent(this, GPS_Service.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }
}
