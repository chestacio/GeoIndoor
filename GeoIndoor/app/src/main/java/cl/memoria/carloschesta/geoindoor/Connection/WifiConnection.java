package cl.memoria.carloschesta.geoindoor.Connection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.LoginFilter;
import android.util.Log;

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
                                WiFi asd = new WiFi();
                                asd.setMAC(results.get(size).BSSID);
                                asd.setSSID(results.get(size).SSID);
                                asd.setRSSID(String.valueOf(results.get(size).level));
                                asd.setFreq(String.valueOf(results.get(size).frequency));

                                asd.setDistance(String.valueOf(calculateDistance(results.get(size).level, results.get(size).frequency)));

                                WifiFragment.addWifiToList(asd);
                                size--;
                                WifiFragment.adapterNotifyDataSetChanged();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }, 0, 2000);
    }

    private double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
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