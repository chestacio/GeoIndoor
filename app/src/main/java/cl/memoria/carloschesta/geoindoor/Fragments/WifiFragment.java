package cl.memoria.carloschesta.geoindoor.Fragments;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;

import cl.memoria.carloschesta.geoindoor.Adapter.WifiAdapter;
import cl.memoria.carloschesta.geoindoor.Connection.WifiConnection;
import cl.memoria.carloschesta.geoindoor.Model.WiFi;
import cl.memoria.carloschesta.geoindoor.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class WifiFragment extends Fragment {

    private static ArrayList<WiFi> devicesList;
    private static WifiAdapter adapter;
    private static ArrayList<String> MACList;

    private ListView listViewWifi;
    private WifiManager wifiManager;
    private WifiConnection wifiConnection;

    public WifiFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_wifi, container, false);

        listViewWifi = (ListView) v.findViewById(R.id.wifiListView);
        devicesList = new ArrayList<WiFi>();
        MACList = new ArrayList<String>();
        adapter = new WifiAdapter(this.getContext(), devicesList);
        listViewWifi.setAdapter(adapter);

        wifiManager = (WifiManager) this.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiConnection = new WifiConnection(getActivity(), wifiManager);

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.getActivity() != null){
            enableWifi();

        }
        if(isVisibleToUser){
            if(wifiManager != null)
                wifiConnection.run();
            else
                Toast.makeText(getActivity(), "wifiManager es nulo", Toast.LENGTH_SHORT).show();
        }
        else{
            if (wifiConnection != null)
                wifiConnection.cancel();

            if (devicesList != null && MACList != null) {
                devicesList.clear();
                MACList.clear();
                adapter.notifyDataSetChanged();
            }
        }

    }

    private void enableWifi(){
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(WifiFragment.this.getContext(), "Activando WiFi...", Toast.LENGTH_SHORT).show();

        }
    }

    public static void addWifiDevice(WiFi device){
        if (devicesList == null || adapter == null || MACList == null)
            return;

        if(!MACList.contains(device.getMAC())){
            MACList.add(device.getMAC());
            devicesList.add(device);
        }

        for (WiFi dev: devicesList) {
            if (device.getMAC().equals(dev.getMAC())) {
                dev.setRSSID(device.getRSSID());
                dev.setDistance(device.getDistance());
                dev.setName(device.getName());
                break;
            }
        }

        adapter.sort(new Comparator<WiFi>() {
            @Override
            public int compare(WiFi wiFi, WiFi t1) {
                return wiFi.getMAC().compareTo(t1.getMAC());
            }
        });

        adapter.notifyDataSetChanged();
        //Log.i("CANTIDAD DEVICELIST", String.valueOf(devicesList.size()));
    }

    public static void clearList(){
        devicesList.clear();
        adapter.notifyDataSetChanged();
    }

}
