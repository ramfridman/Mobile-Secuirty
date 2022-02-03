package com.afeka.wifiscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.afeka.wifiscanner.Adapters.WifiGridAdapter;
import com.afeka.wifiscanner.Logic.WifiNetwork;
import com.afeka.wifiscanner.permissions.LocationPermissionController;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements WifiGridAdapter.GridClickListener {

    private static final int REQUEST_ENABLE_LOCATION = 8956;
    private static final String SHARED_PREF_FILE = "MyPerfFile";

    private WifiManager wifiManager;
    private WifiGridAdapter wifiGridAdapter;
    private Set<WifiNetwork> wifiNetworks;
    private ScanWifiNetworkReceiver wifiNetworkReceiver;
    private LocationManager locationManager;
    private LocationPermissionController permissionController;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton mSearchFab;
    private FloatingActionButton mStopFab;
    private GridView gridView;
    private TextView headLine;
    private GifImageView loadingImage;

    public class ScanWifiNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final List<ScanResult> scannedNetworks = wifiManager.getScanResults();
            Log.d("INFO", "received scan result. " + intent.toString() + " size: " + scannedNetworks.size());
            loadingImage.setVisibility(View.GONE);
            wifiGridAdapter.setWifiNetworks(wifiManager.getScanResults(), wifiNetworks);

            Location location = null;
            if (permissionController.checkLocationPermissions(getApplicationContext())) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            saveCurrentNetworks(location);
        }
    }

    private void saveCurrentNetworks(Location location) {
        SharedPreferences pref = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if(location != null) {
            String longitude = Double.toString(location.getLongitude());
            String latitude = Double.toString(location.getLatitude());
            editor.putString("Longitude", longitude);
            editor.putString("Latitude", latitude);
            Gson gson = new Gson();
            int counter = 0;
            for(WifiNetwork network: wifiNetworks) {
                double index = counter + location.getLongitude() + location.getLatitude();
                String wifiNetwork = gson.toJson(network);
                Log.d("TAG", wifiNetwork);
                editor.putString(Double.toString(index), wifiNetwork);
                counter++;
            }
            editor.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = findViewById(R.id.coordinator);
        mSearchFab = findViewById(R.id.search_fab);
        mStopFab = findViewById(R.id.stop_fab);
        gridView = findViewById(R.id.gridView);
        headLine = findViewById(R.id.header);
        loadingImage = findViewById(R.id.loading_gif);
        loadingImage.setVisibility(View.GONE);

        mStopFab.setVisibility(View.GONE);
        wifiNetworks = new HashSet<>();

        permissionController = new LocationPermissionController();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiNetworkReceiver = new ScanWifiNetworkReceiver();
        headLine.setText("Device Supports WIFI RTT : " + String.valueOf(wifiManager.isDeviceToApRttSupported()));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mSearchFab.setOnClickListener(view -> startWifiScan());
        mStopFab.setOnClickListener(view -> stopWifiScan());
        initUI();
    }
    @Override
    public void onItemClicked(ScanResult wifiNetwork) {
        switchActivities(wifiNetwork);
    }

    private void switchActivities(ScanResult wifiNetwork) {
        Intent intent = new Intent(this, NetworkInfoActivity.class);
        intent.putExtra("EXTRA_WIFI_NETWORK", wifiNetwork);
        startActivity(intent);
    }

    private void stopWifiScan() {
        stopService(new Intent(this, ScanningService.class));
        mStopFab.setVisibility(View.GONE);
        mSearchFab.setVisibility(View.VISIBLE);
    }

    private void initUI() {
       gridView.setVisibility(View.GONE);
       wifiGridAdapter = new WifiGridAdapter(getApplicationContext(), this);
       gridView.setAdapter(wifiGridAdapter);
    }

    private void startWifiScan() {
        if (!permissionController.checkLocationPermissions(getApplicationContext())) {
            permissionController.requestLocationPermission(this, coordinatorLayout);
            return;
        }
        if (!isLocationEnabled(getApplicationContext())) {
            handleLocationServiceDisabled();
            return;
        }
        if (!wifiManager.isWifiEnabled()) {
            Snackbar.make(coordinatorLayout, R.string.enable_wifi, Snackbar.LENGTH_LONG).show();
            return;
        }
        gridView.setVisibility(View.VISIBLE);
        loadingImage.setVisibility(View.VISIBLE);
        mSearchFab.setVisibility(View.GONE);
        mStopFab.setVisibility(View.VISIBLE);
        startService(new Intent(this, ScanningService.class));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (resultCode == RESULT_OK) {
                startWifiScan();
            } else {
                Snackbar.make(coordinatorLayout, R.string.location_service_disabled, Snackbar.LENGTH_SHORT).setAction
                        (android.R.string.ok, view -> startEnableLocationServicesActivity()).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (permissionController.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            startWifiScan();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static boolean isLocationEnabled(@NonNull final Context context) {
        final int locationEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure
                .LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        return locationEnabled != Settings.Secure.LOCATION_MODE_OFF;
    }

    private void handleLocationServiceDisabled() {
        Snackbar.make(coordinatorLayout, R.string.location_service_disabled, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, view -> startEnableLocationServicesActivity())
                .show();
    }

    private void startEnableLocationServicesActivity() {
        Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 87);
            }
        }
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiNetworkReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiNetworkReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWifiScan();
    }
}