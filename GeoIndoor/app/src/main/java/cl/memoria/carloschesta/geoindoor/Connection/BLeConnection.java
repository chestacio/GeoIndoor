package cl.memoria.carloschesta.geoindoor.Connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Calendar;
import java.util.Date;

import cl.memoria.carloschesta.geoindoor.Fragments.BluetoothFragment;
import cl.memoria.carloschesta.geoindoor.Fragments.MainFragment;
import cl.memoria.carloschesta.geoindoor.Model.BluetoothLe;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

/**
 * Created by Carlos on 14-08-2016.
 */
public class BLeConnection {

    private Activity activity;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000000;
    private final int BLE = 1;
    private double beaconDistancesList[] = {0.0, 0.0, 0.0};
    private int counter;


    public BLeConnection(Activity activity, BluetoothAdapter bluetoothAdapter) {
        this.activity = activity;
        mHandler = new Handler();
        mBluetoothAdapter = bluetoothAdapter;
        counter = 0;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    activity.runOnUiThread(new Runnable() {

                        final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis());

                        // Start scanning
                        @Override
                        public void run() {
                            BluetoothLe deviceDetected = new BluetoothLe();
                            deviceDetected.setMAC(deviceLe.getAddress());
                            deviceDetected.setRSSID(String.valueOf(deviceLe.getRssi()));

                            Log.i("BEACON", "Purple [0]:\t" + String.valueOf(beaconDistancesList[0]));
                            Log.i("BEACON", "Green  [1]:\t" + String.valueOf(beaconDistancesList[1]));
                            Log.i("BEACON", "L BLue [2]:\t" + String.valueOf(beaconDistancesList[2]));
                            Log.i("BEACON", "-------------------------------");

                            if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON){
                                IBeaconDevice iBeacon = new IBeaconDevice(deviceLe);
                                deviceDetected.setDistance(String.valueOf(iBeacon.getAccuracy()));

                                if (iBeacon.getAddress().endsWith("F1:50:7A:27:67:4F")){
                                    deviceDetected.setColor("Purple");
                                    beaconDistancesList[0] = iBeacon.getAccuracy();
                                    counter++;
                                }

                                else if (iBeacon.getAddress().endsWith("DB:2A:7D:35:34:F7")){
                                    deviceDetected.setColor("Green");
                                    beaconDistancesList[1] = iBeacon.getAccuracy();
                                    counter++;
                                }

                                else if (iBeacon.getAddress().endsWith("C3:3C:D0:40:ED:64")){
                                    deviceDetected.setColor("Light Blue");
                                    beaconDistancesList[2] = iBeacon.getAccuracy();
                                    counter++;
                                }

                                //BluetoothFragment.addBluetoothDevice(deviceDetected);
                                if (MainFragment.getCalculatedPositionMarker() != null) {
                                    if (beaconDistancesList[0] != 0.0 && beaconDistancesList[1] != 0.0 && beaconDistancesList[2] != 0.) {
                                        LatLng calcPos = getCurrentLocation(MainFragment.getDistanceD(), MainFragment.getDistanceI(), MainFragment.getDistanceJ());
                                        MainFragment.setMarkerPosition(calcPos);
                                        MainFragment.addDataToCSV((new Date()).getTime(), BLE, calcPos);
                                    }
                                }

                            }

                        }
                    });
                }
            };

    public void startScanLe(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            },SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            counter = 0;
        }

    }


    private LatLng getCurrentLocation(double d, double i, double j){
        double r1 = beaconDistancesList[0];
        double r2 = beaconDistancesList[1];
        double r3 = beaconDistancesList[2];

        double x = (Math.pow(r1, 2) - Math.pow(r2, 2) + Math.pow(d, 2))/(2*d);
        double y = (Math.pow(r1, 2) - Math.pow(r3, 2) + Math.pow(i, 2) + Math.pow(j, 2))/(2*j) - (i/j) * x;

        return new LatLng(y, x);
    }

}
