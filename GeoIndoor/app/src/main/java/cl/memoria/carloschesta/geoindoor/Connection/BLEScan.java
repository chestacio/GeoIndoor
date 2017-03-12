package cl.memoria.carloschesta.geoindoor.Connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import cl.memoria.carloschesta.geoindoor.Fragments.BluetoothFragment;
import cl.memoria.carloschesta.geoindoor.Model.BluetoothLe;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

/**
 * Created by Carlos on 14-08-2016.
 */
public class BLEScan {

    private Activity activity;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000000;

    public BLEScan(Activity activity, BluetoothAdapter bluetoothAdapter) {
        this.activity = activity;
        mHandler = new Handler();
        mBluetoothAdapter = bluetoothAdapter;
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


                            if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON){
                                IBeaconDevice iBeacon = new IBeaconDevice(deviceLe);
                                deviceDetected.setDistance(String.valueOf(iBeacon.getAccuracy()));

                                // BEACON CHESTACU
                                if (iBeacon.getAddress().endsWith("F1:50:7A:27:67:4F"))
                                    deviceDetected.setColor("Purple");

                                else if (iBeacon.getAddress().endsWith("C3:3C:D0:40:ED:64"))
                                    deviceDetected.setColor("Light Blue");

                                // BEACON INDOCU
                                else if (iBeacon.getAddress().endsWith("DB:2A:7D:35:34:F7"))
                                    deviceDetected.setColor("Green");

                                BluetoothFragment.addBluetoothDevice(deviceDetected);
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
        } else
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

    }

}
