package com.afeka.wifiscanner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScanningService extends Service {

    final int TIME_BETWEEN_SCANS = 10 * 1000;

    WifiManager wifiManager;
    MainActivity.ScanWifiNetworkReceiver wifiNetworkReceiver;
    Timer timer = new Timer();

    public ScanningService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(wifiNetworkReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                wifiManager.startScan();
            }
        }, 0, TIME_BETWEEN_SCANS);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.d("INFO", "Stop scanning for networks");
    }

}
