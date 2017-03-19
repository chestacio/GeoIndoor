package cl.memoria.carloschesta.geoindoor.Fragments;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cl.memoria.carloschesta.geoindoor.Adapter.BluetoothLeAdapter;
import cl.memoria.carloschesta.geoindoor.Connection.BLEScan;
import cl.memoria.carloschesta.geoindoor.Model.BluetoothLe;
import cl.memoria.carloschesta.geoindoor.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1, PERMS_REQUEST_CODE = 123;
    private BluetoothAdapter mBluetoothAdapter;
    private static ArrayList<BluetoothLe> devicesList;
    private static ArrayList<String> MACList;
    private static BluetoothLeAdapter adapter;
    private ListView listViewBluetooth;
    private BLEScan connection;
    private FloatingActionButton floatingActingButton;


    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        floatingActingButton = (FloatingActionButton) v.findViewById(R.id.floatingActingButtonBluetooth);
        floatingActingButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_bluetooth_black_24dp));

        floatingActingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.distance_settings, null);
                dialogBuilder.setView(dialogView);

                final EditText etD = (EditText) dialogView.findViewById(R.id.etD);
                final EditText etI = (EditText) dialogView.findViewById(R.id.etI);
                final EditText etJ = (EditText) dialogView.findViewById(R.id.etJ);

                final SharedPreferences prefs = getActivity().getSharedPreferences(
                        "cl.memoria.carloschesta.geoindoor.PREFERENCE_BLUETOOTH_CONFIG", Context.MODE_PRIVATE);

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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listViewBluetooth = (ListView) v.findViewById(R.id.bluetoothListView);

        devicesList = new ArrayList<BluetoothLe>();
        MACList = new ArrayList<String>();
        adapter = new BluetoothLeAdapter(this.getContext(), devicesList);
        listViewBluetooth.setAdapter(adapter);


        connection = new BLEScan(this.getActivity(), mBluetoothAdapter);

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){

            if (hasPermissions()){
                checkBluetoothLECompatibility();
                connection.startScanLe(true);
            }
            else
                requestPerms();

        }
        else{
            Log.i("TEST", "Invisible");
            if (connection != null)
                connection.startScanLe(false);
            if (devicesList != null && MACList != null) {
                devicesList.clear();
                MACList.clear();
                adapter.notifyDataSetChanged();
            }
        }
    }

    public static void addBluetoothDevice(BluetoothLe device){
        if(!MACList.contains(device.getMAC())){
            MACList.add(device.getMAC());
            devicesList.add(device);
        }

        for (BluetoothLe dev: devicesList) {
            if (device.getMAC().equals(dev.getMAC())) {
                dev.setRSSID(device.getRSSID());
                dev.setDistance(device.getDistance());
            }
        }

        adapter.notifyDataSetChanged();

 
    }

    private boolean hasPermissions() {
        int res = 0;
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        for (String perms : permissions) {
            res = getActivity().checkCallingOrSelfPermission(perms);

            if (!(res == PackageManager.PERMISSION_GRANTED))
                return false;
        }

        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {

            case PERMS_REQUEST_CODE:
                for (int res : grantResults)
                    // if user granted all permissions
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                break;

            default:
                // if user not granted permissions
                allowed = false;
                break;

        }

        if (allowed) {
            // user granted all permissions so we can connect Bluetooth
            checkBluetoothLECompatibility();
            connection.startScanLe(true);
        }
        else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(getContext(), "Permiso de Acceso coarse denegado", Toast.LENGTH_SHORT).show();
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(getContext(), "Permiso de Acceso fino denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkBluetoothLECompatibility(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());

        // ve si el cel soporta blutu 4.0
        if (!this.getActivity().getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")){
            alert.setTitle("¡Alerta!");
            alert.setMessage("Este dispositivo no soporta Bluetooth 4.0 (Bluetooth Low Energy)");
            alert.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    System.exit(0);
                }
            });
            alert.create();
            alert.show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            Toast.makeText(BluetoothFragment.this.getContext(), "Activando Bluetooth...", Toast.LENGTH_SHORT).show();
        }
    }
}
