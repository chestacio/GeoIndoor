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

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cl.memoria.carloschesta.geoindoor.Fragments.MainFragment;
import cl.memoria.carloschesta.geoindoor.Fragments.WifiFragment;
import cl.memoria.carloschesta.geoindoor.Model.WiFi;

import static cl.memoria.carloschesta.geoindoor.Utils.Utils.getListAverage;
import static cl.memoria.carloschesta.geoindoor.Utils.Utils.isFullArray;
import static cl.memoria.carloschesta.geoindoor.Utils.Utils.listToString;

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
    private double APDistancesList1[] = {0.0, 0.0, 0.0, 0.0, 0.0};
    private double APDistancesList2[] = {0.0, 0.0, 0.0, 0.0, 0.0};
    private double APDistancesList3[] = {0.0, 0.0, 0.0, 0.0, 0.0};
    private int counter1;
    private int counter2;
    private int counter3;

    private static int WIFI = 0;
    private final String AP1_MAC = "F8:1A:67:F6:61:9C";
    private final String AP2_MAC = "00:16:01:D1:85:3C";
    private final String AP3_MAC = "00:16:01:D1:85:3C";

    public WifiConnection(Activity activity, WifiManager wifiManager) {
        this.activity = activity;
        this.wifiManager = wifiManager;
        timer = new Timer();

        counter1 = 0;
        counter2 = 0;
        counter3 = 0;

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

                                if (AP.getMAC().equalsIgnoreCase(AP1_MAC)) {
                                    APDistancesList1[counter1 % 5] = Double.parseDouble(AP.getDistance());
                                    counter1++;
                                    Log.i("ACESS_POINT", "AP1:\t" + listToString(APDistancesList1));
                                }

                                if (AP.getMAC().equalsIgnoreCase(AP2_MAC)) {
                                    APDistancesList1[counter2 % 5] = Double.parseDouble(AP.getDistance());
                                    counter2++;
                                    Log.i("ACESS_POINT", "AP2:\t" + listToString(APDistancesList2));
                                }

                                if (AP.getMAC().equalsIgnoreCase(AP3_MAC)) {
                                    APDistancesList1[counter3 % 5] = Double.parseDouble(AP.getDistance());
                                    counter3++;
                                    Log.i("ACESS_POINT", "AP3:\t" + listToString(APDistancesList3));
                                }

                                //if (AP.getMAC().equalsIgnoreCase("1C:5F:2B:FC:FE:B8")){
                                    //WifiFragment.addWifiToList(AP);
                                    //WifiFragment.adapterNotifyDataSetChanged();
                                //}

                                if (MainFragment.getCalculatedPositionMarker() != null) {

                                    if (isFullArray(APDistancesList1) && isFullArray(APDistancesList2) && isFullArray(APDistancesList3)){

                                        LatLng calcPos = getCurrentLocation(MainFragment.getDistanceD(), MainFragment.getDistanceI(), MainFragment.getDistanceJ());
                                        MainFragment.addDataToCSV((new Date()).getTime(), WIFI, calcPos);

                                        Log.i("ACESS_POINT", "-------------------------------");
                                        Log.i("ACESS_POINT", "AP1:\t" + listToString(APDistancesList1));
                                        Log.i("ACESS_POINT", "AP2:\t" + listToString(APDistancesList2));
                                        Log.i("ACESS_POINT", "AP3:\t" + listToString(APDistancesList3));
                                        Log.i("ACESS_POINT", "-------------------------------");

                                        APDistancesList1 = new double[]{0, 0, 0, 0, 0};
                                        APDistancesList2 = new double[]{0, 0, 0, 0, 0};
                                        APDistancesList3 = new double[]{0, 0, 0, 0, 0};

                                    }
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
        double r1 = getListAverage(APDistancesList1);
        double r2 = getListAverage(APDistancesList2);
        double r3 = getListAverage(APDistancesList3);

        double x = (Math.pow(r1, 2) - Math.pow(r2, 2) + Math.pow(d, 2))/(2*d);
        double y = (Math.pow(r1, 2) - Math.pow(r3, 2) + Math.pow(i, 2) + Math.pow(j, 2))/(2*j) - (i/j) * x;

        Log.d("XY", String.valueOf(new DecimalFormat("#.##").format(x)) + ", " + String.valueOf(new DecimalFormat("#.##").format(y)));

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