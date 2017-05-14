package cl.memoria.carloschesta.geoindoor.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cl.memoria.carloschesta.geoindoor.Model.WiFi;
import cl.memoria.carloschesta.geoindoor.R;

/**
 * Created by Carlos on 14-08-2016.
 */
public class WifiAdapter extends ArrayAdapter<WiFi> {


    private Context context;
    private ArrayList<WiFi> devices;

    public WifiAdapter(Context context, ArrayList<WiFi> devices) {
        super(context, -1, devices);
        this.context = context;
        this.devices = devices;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.wifi_row, parent, false);

        TextView tvWifiMAC = (TextView) row.findViewById(R.id.tvWifiMAC);
        TextView tvWifiSSID = (TextView) row.findViewById(R.id.tvWifiSSID);
        TextView tvWifiRSSID = (TextView) row.findViewById(R.id.tvWifiRSSID);
        TextView tvWifiDistance = (TextView) row.findViewById(R.id.tvWifiDistance);
        TextView tvWifiFreq = (TextView) row.findViewById(R.id.tvWifiFreq);

        tvWifiMAC.setText(devices.get(position).getMAC());
        tvWifiRSSID.setText(devices.get(position).getRSSID());
        tvWifiSSID.setText(devices.get(position).getSSID());
        tvWifiDistance.setText(devices.get(position).getDistance());
        tvWifiFreq.setText(devices.get(position).getFreq());

        return row;
    }
}
