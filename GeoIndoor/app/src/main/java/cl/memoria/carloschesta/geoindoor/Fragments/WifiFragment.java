package cl.memoria.carloschesta.geoindoor.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cl.memoria.carloschesta.geoindoor.Adapter.WifiAdapter;
import cl.memoria.carloschesta.geoindoor.Connection.WifiConnection;
import cl.memoria.carloschesta.geoindoor.Model.WiFi;
import cl.memoria.carloschesta.geoindoor.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class WifiFragment extends Fragment {

    private ListView listViewWifi;
    private static ArrayList<WiFi> deviceList;
    private static WifiAdapter adapter;
    private WifiManager wifiManager;
    private FloatingActionButton floatingActingButton;
    private WifiConnection wifiConnection;

    public WifiFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_wifi, container, false);

        floatingActingButton = (FloatingActionButton) v.findViewById(R.id.floatingActingButtonWifi);
        floatingActingButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_wifi_black_24dp));

        floatingActingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.bluetooth_config, null);
                dialogBuilder.setView(dialogView);

                final EditText etD = (EditText) dialogView.findViewById(R.id.etBluetoothD);
                final EditText etI = (EditText) dialogView.findViewById(R.id.etBluetoothI);
                final EditText etJ = (EditText) dialogView.findViewById(R.id.etBluetoothJ);

                final SharedPreferences prefs = getActivity().getSharedPreferences(
                        "cl.memoria.carloschesta.geoindoor.PREFERENCE_WIFI_CONFIG", Context.MODE_PRIVATE);

                String d = prefs.getString("d", "");
                String i = prefs.getString("i", "");
                String j = prefs.getString("j", "");

                etD.setText(d);
                etI.setText(i);
                etJ.setText(j);

                dialogBuilder.setTitle("Set position");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putString("d", etD.getText().toString());
                        editor.putString("i", etI.getText().toString());
                        editor.putString("j", etJ.getText().toString());

                        editor.commit();
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();

            }
        });

        listViewWifi = (ListView) v.findViewById(R.id.wifiListView);
        deviceList = new ArrayList<WiFi>();
        adapter = new WifiAdapter(this.getContext(), deviceList);
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
            if(deviceList != null && adapter != null) {
                deviceList.clear();
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

    public static void adapterNotifyDataSetChanged(){
        adapter.notifyDataSetChanged();
    }

    public static void addWifiToList(WiFi wifi){
        deviceList.add(wifi);
    }

    public static void clearList(){
        deviceList.clear();
        adapter.notifyDataSetChanged();
    }

}