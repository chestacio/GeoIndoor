package cl.memoria.carloschesta.geoindoor.Connection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cl.memoria.carloschesta.geoindoor.Fragments.WifiFragment;
import cl.memoria.carloschesta.geoindoor.Model.WiFi;

/**
 * Created by Carlos on 05-09-2016.
 */
public class WifiConnection extends Timer{

    private BroadcastReceiver broadcastReceiver;
    private Activity activity;
    private WifiManager wifiManager;
    private List<ScanResult> results;
    private int size;
    private Timer timer;
    private double APList[] = {0.0, 0.0, 0.0};

    private final String AP1_MAC = "F8:1A:67:F6:61:9C";
    private final String AP2_MAC = "00:16:01:D1:85:3C";
    private final String AP3_MAC = "00:16:01:D1:85:3C";

    public WifiConnection(Activity activity, WifiManager wifiManager) {
        this.activity = activity;
        this.wifiManager = wifiManager;
        timer = new Timer();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                results = WifiConnection.this.wifiManager.getScanResults();
                size = results.size();
            }
        };

        activity.registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void run(){

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WifiFragment.clearList();
                        wifiManager.startScan();
                        results = wifiManager.getScanResults();
                        size = results.size();

                        Log.i("SIZE", String.valueOf(size));

                        try
                        {
                            size = size - 1;
                            while (size >= 0)
                            {
                                WiFi AP = new WiFi();
                                AP.setMAC(results.get(size).BSSID);
                                AP.setSSID(results.get(size).SSID);
                                AP.setRSSID(String.valueOf(results.get(size).level));
                                AP.setFreq(String.valueOf(results.get(size).frequency));

                                AP.setDistance(String.valueOf(calculateDistance(results.get(size).level, results.get(size).frequency)));

                                if (AP.getMAC().equals(AP1_MAC))
                                    APList[0] = Double.parseDouble(AP.getDistance());

                                if (AP.getMAC().equals(AP2_MAC))
                                    APList[1] = Double.parseDouble(AP.getDistance());

                                if (AP.getMAC().equals(AP3_MAC))
                                    APList[2] = Double.parseDouble(AP.getDistance());

                                if (AP.getMAC().equalsIgnoreCase("1C:5F:2B:FC:FE:B8")){
                                    WifiFragment.addWifiToList(AP);
                                    WifiFragment.adapterNotifyDataSetChanged();
                                }

                                size--;
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }, 0, 300);
    }

    private double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    public LatLng getCurrentLocation(double d, double i, double j){
        double r1 = APList[0];
        double r2 = APList[1];
        double r3 = APList[2];

        double x = (Math.pow(r1, 2) - Math.pow(r2, 2) + Math.pow(d, 2))/(2*d);
        double y = (Math.pow(r1, 2) - Math.pow(r3, 2) + Math.pow(i, 2) + Math.pow(j, 2))/(2*j) - (i/j) * x;

        return new LatLng(y, x);
    }

    @Override
    public void cancel() {
        if (broadcastReceiver != null) {
            activity.unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
            super.cancel();
        }
    }
}