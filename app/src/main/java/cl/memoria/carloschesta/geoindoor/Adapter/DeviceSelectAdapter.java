package cl.memoria.carloschesta.geoindoor.Adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;

import cl.memoria.carloschesta.geoindoor.Model.Device;
import cl.memoria.carloschesta.geoindoor.R;

/**
 * Created by carlos on 20-03-17.
 */

public class DeviceSelectAdapter extends ArrayAdapter<Device> {

    private Context context;
    private ArrayList<Device> devices;

    public DeviceSelectAdapter(Context context, ArrayList<Device> devices) {
        super(context, -1, devices);
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TextView label = new TextView(context);
        label.setText(devices.get(position).getName());
        label.setGravity(Gravity.CENTER);

        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.device_row, parent, false);

        TextView tvSelectDeviceName = (TextView) row.findViewById(R.id.tvSelectDeviceName);
        TextView tvSelectDeviceMAC = (TextView) row.findViewById(R.id.tvSelectDeviceMAC);

        tvSelectDeviceName.setText(devices.get(position).getName());
        tvSelectDeviceMAC.setText("(" + devices.get(position).getMAC() + ")");

        return row;
    }
}
