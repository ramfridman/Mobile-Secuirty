package com.afeka.wifiscanner;

import androidx.appcompat.app.AppCompatActivity;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class NetworkInfoActivity extends AppCompatActivity {

    TextView name, strength, mac, security;
    private ScanResult wifiNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_info);

        name = findViewById(R.id.name);
        strength = findViewById(R.id.strength);
        mac = findViewById(R.id.mac);
        security = findViewById(R.id.security);

        Bundle extras = getIntent().getExtras();
        wifiNetwork = (ScanResult) extras.get("EXTRA_WIFI_NETWORK");

        name.setText(wifiNetwork.SSID);
        strength.setText("Strength: " + wifiNetwork.level);
        mac.setText("MAC Address: " + wifiNetwork.BSSID);
        security.setText("Security: " + wifiNetwork.capabilities);

    }
}