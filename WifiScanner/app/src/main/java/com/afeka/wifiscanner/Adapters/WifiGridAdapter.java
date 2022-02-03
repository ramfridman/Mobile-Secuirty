package com.afeka.wifiscanner.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.afeka.wifiscanner.Logic.WifiNetwork;
import com.afeka.wifiscanner.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WifiGridAdapter extends BaseAdapter {
    private Context mContext;
    private List<ScanResult> networks;
    private GridClickListener gridClickListener;

    public WifiGridAdapter(@NonNull Context context, GridClickListener gridClickListener ) {
        mContext = context;
        networks = new ArrayList<>();
        this.gridClickListener = gridClickListener;
    }

    @Override
    public int getCount() {
        return networks.size();
    }

    @Override
    public Object getItem(int position) {
        return networks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            grid = inflater.inflate(R.layout.grid_item, null);
            ConstraintLayout grid_item = grid.findViewById(R.id.grid_item);
            ProgressBar signal = grid.findViewById(R.id.signalBar);
            TextView name = grid.findViewById(R.id.network_name);
            TextView macAddress = grid.findViewById(R.id.network_mac);

            final ScanResult result = networks.get(position);

            int level = result.level;

            if (level > -50) {
               signal.getProgressDrawable().setColorFilter(Color.parseColor("#014d11"), PorterDuff.Mode.SCREEN);
            } else if (level < -50 && level > -60) {
                signal.getProgressDrawable().setColorFilter(Color.parseColor("#6cb87c"), PorterDuff.Mode.SCREEN);
            } else if(level < -60 && level > -70) {
                signal.getProgressDrawable().setColorFilter(Color.parseColor("#e3e017"), PorterDuff.Mode.SCREEN);
            } else {
                signal.getProgressDrawable().setColorFilter(Color.parseColor("#ff0000"), PorterDuff.Mode.SCREEN);
            }
            name.setText(result.SSID);
            macAddress.setText(result.BSSID);

            grid_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gridClickListener.onItemClicked(result);
                }
            });

        return grid;
    }


    public void setWifiNetworks(final List<ScanResult> scannedNetworks, final Set<WifiNetwork> wifiNetworks) {
        if (wifiNetworks == null) {
            return;
        }
        final List<String> duplicates = new ArrayList<>(wifiNetworks.size());
        final List<ScanResult> filteredResults = new ArrayList<>(wifiNetworks.size());
        for (ScanResult result : scannedNetworks) {
            if (!duplicates.contains(result.BSSID)) {
                duplicates.add(result.BSSID);
                filteredResults.add(result);
                WifiNetwork temp = new WifiNetwork(result.SSID, result.BSSID, result.level);
                if(!wifiNetworks.contains(temp)) {
                    wifiNetworks.add(new WifiNetwork(result.SSID, result.BSSID, result.level));
                }
            }
        }
        this.networks = filteredResults;
        notifyDataSetChanged();
    }

    public interface GridClickListener{
        void onItemClicked(final ScanResult wifiNetwork);
    }
}
