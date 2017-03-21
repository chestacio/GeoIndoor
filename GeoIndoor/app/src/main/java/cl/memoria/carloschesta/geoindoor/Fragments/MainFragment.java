package cl.memoria.carloschesta.geoindoor.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import cl.memoria.carloschesta.geoindoor.Libraries.MapBoxOfflineTileProvider;
import cl.memoria.carloschesta.geoindoor.Model.Device;
import cl.memoria.carloschesta.geoindoor.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback{

    private final int MAX_ZOOM = 6;
    private final int MIN_ZOOM = 1;
    private final int INIT_ZOOM = 4;
    private final int MAX_MARKERS = 3;
    private final int DECIMALS = 2;
    private final LatLng NORTHEAST = new LatLng(32,141);
    private final LatLng SOUTHWEST = new LatLng(5, 3);
    private final LatLng INITIAL_POS_CAMERA = new LatLng(18,74);
    private final LatLng INITIAL_POS_MARKER = new LatLng(18,74);
    private final String PARKING_FILE_NAME = "parking_origin_0-145_HD.mbtiles";

    private ArrayList<Device> arrayDevice;
    private GoogleMap gMap;
    private MapView mMapView;
    private File f;
    private TextView tvX;
    private TextView tvY;
    private FloatingActionButton faAddMarkerButton;
    private FloatingActionButton faSettingsButton;
    private FloatingActionButton faGetLocationButton;
    private float distanceD;
    private float distanceI;
    private float distanceJ;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        final SharedPreferences prefs = getActivity().getSharedPreferences(
                "cl.memoria.carloschesta.geoindoor.PREFERENCE_MAIN_CONFIG", Context.MODE_PRIVATE);

        distanceD = prefs.getFloat("distanceD", 0);
        distanceI = prefs.getFloat("distanceI", 0);
        distanceJ = prefs.getFloat("distanceJ", 0);

        arrayDevice = new ArrayList<Device>();

        tvX = (TextView) v.findViewById(R.id.tvX);
        tvY = (TextView) v.findViewById(R.id.tvY);

        faAddMarkerButton = (FloatingActionButton) v.findViewById(R.id.faAddMarkerButton);
        faSettingsButton = (FloatingActionButton) v.findViewById(R.id.faSettingButton);
        faGetLocationButton = (FloatingActionButton) v.findViewById(R.id.faGetLocationButton);

        faAddMarkerButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_add_location_black_24dp));
        faSettingsButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_settings_black_24dp));
        faGetLocationButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_my_location_black_24dp));

        faSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.distance_settings, null);
                dialogBuilder.setView(dialogView);

                final EditText etDistanceD = (EditText) dialogView.findViewById(R.id.etD);
                final EditText etDistanceI = (EditText) dialogView.findViewById(R.id.etI);
                final EditText etDistanceJ = (EditText) dialogView.findViewById(R.id.etJ);

                etDistanceD.setText(String.valueOf(distanceD));
                etDistanceI.setText(String.valueOf(distanceI));
                etDistanceJ.setText(String.valueOf(distanceJ));

                dialogBuilder.setTitle("Set position");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putFloat("distanceD", Float.parseFloat(etDistanceD.getText().toString()));
                        editor.putFloat("distanceI", Float.parseFloat(etDistanceI.getText().toString()));
                        editor.putFloat("distanceJ", Float.parseFloat(etDistanceJ.getText().toString()));

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

        faGetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (distanceD == 0 || distanceI == 0 || distanceJ == 0) {
                    Toast.makeText(getContext(), "Distances between devices need to be configured first", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!areDevicesSameType(arrayDevice)){
                    Toast.makeText(getContext(), "Added devices are not the same type", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mMapView.getMapAsync(this);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get a File from Assets reference to the MBTiles file.
        f = new File(getActivity().getCacheDir()+"/" + PARKING_FILE_NAME);
        if (!f.exists()) try {

            InputStream is = getActivity().getAssets().open(PARKING_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) { throw new RuntimeException(e); }

        return v;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Retrieve GoogleMap instance from MapFragment or elsewhere
        gMap = googleMap;
        setUpMap();

        // Create new TileOverlayOptions instance.
        TileOverlayOptions opts = new TileOverlayOptions();

        // Create an instance of MapBoxOfflineTileProvider.
        MapBoxOfflineTileProvider provider = new MapBoxOfflineTileProvider(f);

        // Set the tile provider on the TileOverlayOptions.
        opts.tileProvider(provider);

        // Add the tile overlay to the map.
        TileOverlay overlay = gMap.addTileOverlay(opts);
    }

    private void setUpMap() {
        gMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        gMap.getUiSettings().setZoomControlsEnabled(false);
        gMap.getUiSettings().setMapToolbarEnabled(false);

        // Initial camera position
        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(INITIAL_POS_CAMERA, INIT_ZOOM);
        gMap.moveCamera(upd);

        // Zoom preferences
        gMap.setMaxZoomPreference(MAX_ZOOM);
        gMap.setMinZoomPreference(MIN_ZOOM);

        // Limit map bounds
        LatLngBounds bounds = new LatLngBounds(SOUTHWEST, NORTHEAST);
        gMap.setLatLngBoundsForCameraTarget(bounds);

        // Show a coordinates label when the map is clicked
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                tvX.setText(String.valueOf(latLng.longitude));
                tvY.setText(String.valueOf(latLng.latitude));
            }
        });

        gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(final Marker marker) {

                View v = getActivity().getLayoutInflater().inflate(R.layout.device_info, null);

                LatLng latLng = marker.getPosition();

                TextView tvDeviceX = (TextView) v.findViewById(R.id.tvDeviceX);
                TextView tvDeviceY = (TextView) v.findViewById(R.id.tvDeviceY);
                TextView tvDeviceType = (TextView) v.findViewById(R.id.tvDeviceType);
                TextView tvDeviceMAC = (TextView) v.findViewById(R.id.tvDeviceMAC);

                Device device = findDeviceByMarker(marker);

                tvDeviceX.setText(String.valueOf(truncateNumber(latLng.longitude, DECIMALS)));
                tvDeviceY.setText(String.valueOf((truncateNumber(latLng.latitude, DECIMALS))));
                tvDeviceType.setText(device.isAP() ? "Access Point" : "Beacon");
                tvDeviceMAC.setText(device.getMAC());

                return v;
            }
        });

        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {  }

            @Override
            public void onMarkerDrag(Marker marker) {

                View v = getActivity().getLayoutInflater().inflate(R.layout.device_info, null);

                LatLng latLng = marker.getPosition();

                TextView tvDeviceX = (TextView) v.findViewById(R.id.tvDeviceX);
                TextView tvDeviceY = (TextView) v.findViewById(R.id.tvDeviceY);

                tvDeviceX.setText(String.valueOf(truncateNumber(latLng.longitude, DECIMALS)));
                tvDeviceY.setText(String.valueOf((truncateNumber(latLng.latitude, DECIMALS))));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {  }
        });

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                int i = -1;
                for (Device device : arrayDevice) {
                    if (device.getMarker().equals(marker))
                        i = arrayDevice.indexOf(device);
                }

                if (i != -1) {
                    arrayDevice.remove(i);
                    marker.remove();
                }

            }
        });

        faAddMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.add_device, null);
                dialogBuilder.setView(dialogView);

                final Switch switchDeviceType = (Switch) dialogView.findViewById(R.id.switchDeviceType);
                Spinner spinnerDeviceList = (Spinner) dialogView.findViewById(R.id.spinnerDeviceList);

                dialogBuilder.setTitle("Add device");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (arrayDevice.size() != MAX_MARKERS) {
                            Marker marker = gMap.addMarker(new MarkerOptions()
                                    .position(INITIAL_POS_MARKER)
                                    .draggable(true));

                            Device device = new Device();
                            device.setMarker(marker);
                            device.setAP(switchDeviceType.isChecked());
                            arrayDevice.add(device);

                            CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(INITIAL_POS_MARKER, INIT_ZOOM);
                            gMap.animateCamera(upd, 500, null);
                        }
                        else
                            Toast.makeText(getContext(), "Maximum number of devices reached", Toast.LENGTH_SHORT).show();

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

    }

    private double truncateNumber(double value, int decimals) {
        return Math.floor(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    @Nullable
    private Device findDeviceByMarker(Marker marker) {
        for (Device device : arrayDevice) {
            if (device.getMarker().equals(marker))
                return device;
        }
        return null;
    }

    // Check if all the devices has the same type
    private boolean areDevicesSameType(ArrayList<Device> arrayList) {

        for (int i = 0; i < arrayList.size(); i++){
            Device current = arrayList.get(i);

            if (arrayList.size() > 1 && i > 0){
                Device prev = arrayList.get(i - 1);

                if (prev.isAP() != current.isAP())
                    return false;
            }
        }
        return true;
    }
}
