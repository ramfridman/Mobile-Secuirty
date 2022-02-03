package com.afeka.wifiscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import java.util.List;

public class RttActivity extends AppCompatActivity {
    private WifiRttManager wifiRttManager;
    private CheckRttReceiver rttReceiver;
    private ScanResult wifiNetwork;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        setContentView(R.layout.activity_rtt);
        wifiNetwork = (ScanResult) extras.get("EXTRA_WIFI_NETWORK");
        textView = findViewById(R.id.textView);
        textView.setText(wifiNetwork.SSID);
        wifiRttManager = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
        rttReceiver = new CheckRttReceiver();
        IntentFilter filter = new IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED);
        registerReceiver(rttReceiver, filter);
        ranging();
    }
    private void ranging() {
        final RangingRequest request = new RangingRequest.Builder()
                .addAccessPoint(wifiNetwork)
                .build();
        wifiRttManager.startRanging(request, getApplication().getMainExecutor(), callback);
    }


    final RangingResultCallback callback = new RangingResultCallback() {
                @Override
                public void onRangingFailure(int code) {
                    Log.d("ERROR", "onRangingFailure() code: " + code);
                    queueNextRangingRequest();
                }

                @Override
                public void onRangingResults(@NonNull List<RangingResult> list) {
                    Log.d("INFO", "list of ranges: " + list);
                    if(list.size() == 1) {
                        RangingResult rangingResult = list.get(0);
                        if(wifiNetwork.BSSID.equals(rangingResult.getMacAddress().toString())) {
                            if(rangingResult.getStatus() == RangingResult.STATUS_SUCCESS) {
                                Log.d("INFO", "Ranging Success");
                            } else if(rangingResult.getStatus() == RangingResult.STATUS_RESPONDER_DOES_NOT_SUPPORT_IEEE80211MC) {
                                Log.d("INFO","RangingResult failed (AP doesn't support IEEE80211 MC.");
                            } else {
                                Log.d("INFO","RangingResult failed.");
                            }
                        } else {
                            Log.d("INFO", "MAC Mismatch");
                        }
                    }
                    queueNextRangingRequest();
                }
    };

    private void queueNextRangingRequest() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ranging();
            }
        }, 1000);
    }


    private void writeOutput(@NonNull final List<RangingResult> result) {
        if (result.isEmpty()) {
            Log.d("INFO","EMPTY ranging result received.");
            return;
        }
        for (RangingResult res : result) {
            if(res.getStatus() == RangingResult.STATUS_SUCCESS)
                textView.setText(res.getDistanceMm());
            Log.d("INFO", "Result: " + res.getRangingTimestampMillis() + " RSSI: " + res.getRssi() + " Distance: " + res.getDistanceMm() + " mm");
        }
    }

    public class CheckRttReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if(wifiRttManager.isAvailable()) {

            } else {

            }
        }
    }


}