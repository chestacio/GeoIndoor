package cl.memoria.carloschesta.geoindoor.Connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.Date;

import cl.memoria.carloschesta.geoindoor.Fragments.BluetoothFragment;
import cl.memoria.carloschesta.geoindoor.Fragments.MainFragment;
import cl.memoria.carloschesta.geoindoor.Model.BluetoothLe;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

import static cl.memoria.carloschesta.geoindoor.Utils.Utils.getListAverage;
import static cl.memoria.carloschesta.geoindoor.Utils.Utils.isFullArray;
import static cl.memoria.carloschesta.geoindoor.Utils.Utils.listToString;

/**
 * Created by Carlos on 14-08-2016.
 */
public class BLeConnection {

    private Activity activity;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000000;
    private final int BLE = 1;
    private double beaconDistancesList1[] = {0.0, 0.0, 0.0, 0.0, 0.0};
    private double beaconDistancesList2[] = {0.0, 0.0, 0.0, 0.0, 0.0};
    private double beaconDistancesList3[] = {0.0, 0.0, 0.0, 0.0, 0.0};
    private int counter1;
    private int counter2;
    private int counter3;


    public BLeConnection(Activity activity, BluetoothAdapter bluetoothAdapter) {
        this.activity = activity;
        mHandler = new Handler();
        mBluetoothAdapter = bluetoothAdapter;
        counter1 = 0;
        counter2 = 0;
        counter3 = 0;
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

                                if (iBeacon.getAddress().endsWith("F1:50:7A:27:67:4F")){
                                    deviceDetected.setColor("Purple");
                                    beaconDistancesList1[counter1 % 5] = iBeacon.getAccuracy();
                                    counter1++;
                                    Log.i("BEACON", "Purple [0]:\t" + listToString(beaconDistancesList1));
                                }

                                else if (iBeacon.getAddress().endsWith("DB:2A:7D:35:34:F7")){
                                    deviceDetected.setColor("Green");
                                    beaconDistancesList2[counter2 % 5] = iBeacon.getAccuracy();
                                    counter2++;
                                    Log.i("BEACON", "Green  [1]:\t" + listToString(beaconDistancesList2));
                                }

                                else if (iBeacon.getAddress().endsWith("C3:3C:D0:40:ED:64")){
                                    deviceDetected.setColor("Light Blue");
                                    beaconDistancesList3[counter3 % 5] = iBeacon.getAccuracy();
                                    counter3++;
                                    Log.i("BEACON", "L Blue [2]:\t" + listToString(beaconDistancesList3));
                                }

                                BluetoothFragment.addBluetoothDevice(deviceDetected);

                                if (MainFragment.getCalculatedPositionMarker() != null) {

                                    if (isFullArray(beaconDistancesList1) && isFullArray(beaconDistancesList2) && isFullArray(beaconDistancesList3)){

                                        LatLng calcPos = getCurrentLocation(MainFragment.getDistanceD(), MainFragment.getDistanceI(), MainFragment.getDistanceJ());
                                        MainFragment.setLocationAndAddDataToCSV((new Date()).getTime(), BLE, calcPos);

                                        Log.i("BEACON", "-------------------------------");
                                        Log.i("BEACON", "Purple [0]:\t" + listToString(beaconDistancesList1));
                                        Log.i("BEACON", "Green  [1]:\t" + listToString(beaconDistancesList2));
                                        Log.i("BEACON", "L Blue [2]:\t" + listToString(beaconDistancesList3));
                                        Log.i("BEACON", "-------------------------------");

                                        beaconDistancesList1 = new double[]{0, 0, 0, 0, 0};
                                        beaconDistancesList2 = new double[]{0, 0, 0, 0, 0};
                                        beaconDistancesList3 = new double[]{0, 0, 0, 0, 0};

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
        }

    }

    private LatLng getCurrentLocation(double d, double i, double j){
        double r1 = getListAverage(beaconDistancesList1);
        double r2 = getListAverage(beaconDistancesList2);
        double r3 = getListAverage(beaconDistancesList3);

        double x = (Math.pow(r1, 2) - Math.pow(r2, 2) + Math.pow(d, 2))/(2*d);
        double y = (Math.pow(r1, 2) - Math.pow(r3, 2) + Math.pow(i, 2) + Math.pow(j, 2))/(2*j) - (i/j) * x;

        Log.d("XY", String.valueOf(new DecimalFormat("#.##").format(x)) + ", " + String.valueOf(new DecimalFormat("#.##").format(y)));

        return new LatLng(y, x);
    }
}
