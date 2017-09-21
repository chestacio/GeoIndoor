package cl.memoria.carloschesta.geoindoor.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cl.memoria.carloschesta.geoindoor.Model.BluetoothLe;
import cl.memoria.carloschesta.geoindoor.R;

/**
 * Created by Carlos on 14-08-2016.
 */
public class BluetoothLeAdapter extends ArrayAdapter<BluetoothLe> {

    private Context context;
    private ArrayList<BluetoothLe> devices;

    public BluetoothLeAdapter(Context context, ArrayList<BluetoothLe> devices) {
        super(context, -1, devices);
        this.context = context;
        this.devices = devices;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.bluetooth_row, parent, false);

        TextView tvBluetoothRSSID = (TextView) row.findViewById(R.id.tvBluetoothRSSID);
        TextView tvBluetoothMAC = (TextView) row.findViewById(R.id.tvBluetoothMAC);
        TextView tvBluetoothDistance = (TextView) row.findViewById(R.id.tvBluetoothDistance);
        TextView tvBluetoothColor = (TextView) row.findViewById(R.id.tvBluetoothColor);
        TextView tvBluetoothName = (TextView) row.findViewById(R.id.tvBluetoothName);

        tvBluetoothMAC.setText(devices.get(position).getMAC());
        tvBluetoothRSSID.setText(devices.get(position).getRSSID());
        tvBluetoothDistance.setText(devices.get(position).getDistance());
        tvBluetoothColor.setText(devices.get(position).getColor());
        tvBluetoothName.setText(devices.get(position).getName());

        return row;
    }
}
