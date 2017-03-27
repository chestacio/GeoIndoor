package cl.memoria.carloschesta.geoindoor.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import cl.memoria.carloschesta.geoindoor.Adapter.DeviceSelectAdapter;
import cl.memoria.carloschesta.geoindoor.Libraries.MapBoxOfflineTileProvider;
import cl.memoria.carloschesta.geoindoor.Libraries.SVGtoBitmap;
import cl.memoria.carloschesta.geoindoor.Model.Device;
import cl.memoria.carloschesta.geoindoor.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback{

    private final int MAX_ZOOM = 6;
    private final int MIN_ZOOM = 1;
    private final int INIT_ZOOM = 4;
    private final int MAX_DEVICES = 3;
    private final int DECIMALS = 2;
    private final LatLng NORTHEAST = new LatLng(32,141);
    private final LatLng SOUTHWEST = new LatLng(5, 3);
    private final LatLng INITIAL_POS_CAMERA = new LatLng(18,74);
    private final LatLng INITIAL_POS_MARKER = new LatLng(18,74);
    private final String PARKING_FILE_NAME = "parking_origin_0-145_HD.mbtiles";
    private final String LIGHTBLUE_MAC_BEACON = "C3:3C:D0:40:ED:64";
    private final String PURPLE_MAC_BEACON = "F1:50:7A:27:67:4F";
    private final String GREEN_MAC_BEACON = "DB:2A:7D:35:34:F7";
    private final String GREEN_BEACON_NAME = "Green Beacon";
    private final String PURPLE_BEACON_NAME = "Purple Beacon";
    private final String LIGHTBLUE_BEACON_NAME = "Light Blue Beacon";
    private final String AP1_NAME = "Access Point 1";
    private final String AP2_NAME = "Access Point 2";
    private final String AP3_NAME = "Access Point 3";

    private ArrayList<Device> arrayDevicesCreated;
    private ArrayList<Device> arrayBeaconDevicesAvailable;
    private ArrayList<Device> arrayAPDevicesAvailable;
    private DeviceSelectAdapter adapter;
    private ColorStateList floatingActionButtonOriginalColor;
    private GoogleMap gMap;
    private MapView mMapView;
    private Marker userPositionMarker;
    private File f;
    private TextView tvX;
    private TextView tvY;
    private FloatingActionButton faAddMarkerButton;
    private FloatingActionButton faSettingsButton;
    private FloatingActionButton faGetLocationButton;
    private Float distanceD;
    private Float distanceI;
    private Float distanceJ;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);

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

                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.distance_settings, null);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setView(dialogView);

                final EditText etDistanceD = (EditText) dialogView.findViewById(R.id.etD);
                final EditText etDistanceI = (EditText) dialogView.findViewById(R.id.etI);
                final EditText etDistanceJ = (EditText) dialogView.findViewById(R.id.etJ);

                // Import local preferences (distances between Devices)
                final SharedPreferences prefs = getActivity().getSharedPreferences(
                        "cl.memoria.carloschesta.geoindoor.PREFERENCE_MAIN_CONFIG", Context.MODE_PRIVATE);

                distanceD = prefs.getFloat("distanceD", 0);
                distanceI = prefs.getFloat("distanceI", 0);
                distanceJ = prefs.getFloat("distanceJ", 0);

                // Set real distances values
                // Assuming that purple beacon is in (0,0), green beacon is in (0,d) and light blue beacon is in (i, j)
                if (arrayDevicesCreated.size() == MAX_DEVICES) {
                    distanceD = getDistanceD(arrayDevicesCreated);
                    distanceI = getDistanceI(arrayDevicesCreated);
                    distanceJ = getDistanceJ(arrayDevicesCreated);
                }

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
                    public void onClick(DialogInterface dialog, int whichButton) {   }
                });

                AlertDialog b = dialogBuilder.create();
                b.show();
            }

        });

        faGetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (distanceD == null || distanceI == null || distanceJ == null || distanceD == 0 || distanceI == 0 || distanceJ == 0) {
                    Toast.makeText(getContext(), "Distances between devices need to be configured first", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!areDevicesSameType(arrayDevicesCreated)){
                    Toast.makeText(getContext(), "Added devices are not the same type", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (arrayDevicesCreated.size() != MAX_DEVICES){
                    Toast.makeText(getContext(), "Set at least three devices to get the current location", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Start geolocation
                if (userPositionMarker == null){
                    MarkerOptions userMarkerOptions = new MarkerOptions();
                    userMarkerOptions.draggable(true);
                    userMarkerOptions.position(INITIAL_POS_MARKER);
                    userMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_standing_frontal_man_silhouette)));

                    userPositionMarker = gMap.addMarker(userMarkerOptions);
                    userPositionMarker.setTag("userMarker");

                    floatingActionButtonOriginalColor = faGetLocationButton.getBackgroundTintList();
                    faGetLocationButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0, 170, 0)));
                }

                // Stop geolocation
                else{
                    userPositionMarker.remove();
                    userPositionMarker = null;

                    faGetLocationButton.setBackgroundTintList(floatingActionButtonOriginalColor);
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


        arrayDevicesCreated = new ArrayList<Device>();
        arrayBeaconDevicesAvailable = new ArrayList<Device>();
        arrayAPDevicesAvailable = new ArrayList<Device>();

        // Load initial Beacon devices
        InitDevices();

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

                if (marker.getTag().equals("device")){
                    View v = getActivity().getLayoutInflater().inflate(R.layout.device_info, null);

                    LatLng latLng = marker.getPosition();

                    TextView tvDeviceX = (TextView) v.findViewById(R.id.tvDeviceX);
                    TextView tvDeviceY = (TextView) v.findViewById(R.id.tvDeviceY);
                    TextView tvDeviceType = (TextView) v.findViewById(R.id.tvDeviceType);
                    TextView tvDeviceMAC = (TextView) v.findViewById(R.id.tvDeviceMAC);
                    TextView tvDeviceName = (TextView) v.findViewById(R.id.tvDeviceName);

                    Device device = findDeviceByMarker(marker, arrayDevicesCreated);

                    tvDeviceX.setText(String.valueOf(truncateNumber(latLng.longitude, DECIMALS)));
                    tvDeviceY.setText(String.valueOf((truncateNumber(latLng.latitude, DECIMALS))));
                    tvDeviceType.setText(device.isAP() ? "Access Point" : "Beacon");
                    tvDeviceMAC.setText(device.getMAC());
                    tvDeviceName.setText(device.getName());

                    return v;
                }

                return null;
            }
        });



        // Update position when the marker is dragged
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

        // The device (marker) will be removed when it description is clicked
        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Device device = findDeviceByMarker(marker, arrayDevicesCreated);

                if (device.isAP())
                    arrayAPDevicesAvailable.add(device);
                else
                    arrayBeaconDevicesAvailable.add(device);

                arrayDevicesCreated.remove(device);
                adapter.notifyDataSetChanged();

                marker.remove();
            }
        });

        faAddMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (arrayDevicesCreated.size() != MAX_DEVICES) {

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.add_device, null);

                    // Building the dialog menu to add devices
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setView(dialogView);

                    final Switch switchDeviceType = (Switch) dialogView.findViewById(R.id.switchDeviceType);
                    final Spinner spinnerDeviceList = (Spinner) dialogView.findViewById(R.id.spinnerDeviceList);

                    adapter = new DeviceSelectAdapter(getContext(), arrayBeaconDevicesAvailable);

                    spinnerDeviceList.setAdapter(adapter);

                    // If the device type switch is clicked, the spinner change it contents
                    switchDeviceType.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            DeviceSelectAdapter adapter;

                            if (switchDeviceType.isChecked())
                                adapter = new DeviceSelectAdapter(getContext(), arrayAPDevicesAvailable);
                            else
                                adapter = new DeviceSelectAdapter(getContext(), arrayBeaconDevicesAvailable);

                            spinnerDeviceList.setAdapter(adapter);

                        }
                    });

                    dialogBuilder.setTitle("Add device");
                    dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            // Marker options
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.draggable(true);
                            markerOptions.position(INITIAL_POS_MARKER);

                            Device device = (Device) spinnerDeviceList.getSelectedItem();

                            // Setting proper icon to marker
                            if (device.isAP()) {
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_wifi)));
                                arrayAPDevicesAvailable.remove(device);
                            }
                            else {
                                if (device.getName().equals(LIGHTBLUE_BEACON_NAME))
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_ble_beacon_icon_lightblue)));
                                if (device.getName().equals(PURPLE_BEACON_NAME))
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_ble_beacon_icon_purple)));
                                if (device.getName().equals(GREEN_BEACON_NAME))
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_ble_beacon_icon_green)));
                                arrayBeaconDevicesAvailable.remove(device);
                            }

                            // Creating the marker with the options
                            Marker marker = gMap.addMarker(markerOptions);
                            marker.setTag("device");

                            device.setMarker(marker);

                            // Adding a device to the created devices list
                            arrayDevicesCreated.add(device);
                            adapter.notifyDataSetChanged();

                            // Move camera to the marker recently created
                            CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(INITIAL_POS_MARKER, INIT_ZOOM);
                            gMap.animateCamera(upd, 500, null);

                        }
                    });
                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });

                    AlertDialog b = dialogBuilder.create();
                    b.show();
                }

                else
                    Toast.makeText(getContext(), "Maximum number of devices reached", Toast.LENGTH_SHORT).show();

            }

        });

    }

    private void InitDevices() {
        Device LightBlueBeacon = new Device();
        Device PurpleBeacon = new Device();
        Device GreenBeacon = new Device();

        Device AP1 = new Device();
        Device AP2 = new Device();
        Device AP3 = new Device();

        LightBlueBeacon.setName(LIGHTBLUE_BEACON_NAME);
        LightBlueBeacon.setMAC(LIGHTBLUE_MAC_BEACON);
        LightBlueBeacon.setAP(false);

        PurpleBeacon.setName(PURPLE_BEACON_NAME);
        PurpleBeacon.setMAC(PURPLE_MAC_BEACON);
        PurpleBeacon.setAP(false);

        GreenBeacon.setName(GREEN_BEACON_NAME);
        GreenBeacon.setMAC(GREEN_MAC_BEACON);
        GreenBeacon.setAP(false);

        AP1.setName(AP1_NAME);
        AP1.setAP(true);

        AP2.setName(AP2_NAME);
        AP2.setAP(true);

        AP3.setName(AP3_NAME);
        AP3.setAP(true);

        arrayBeaconDevicesAvailable.add(LightBlueBeacon);
        arrayBeaconDevicesAvailable.add(PurpleBeacon);
        arrayBeaconDevicesAvailable.add(GreenBeacon);

        arrayAPDevicesAvailable.add(AP1);
        arrayAPDevicesAvailable.add(AP2);
        arrayAPDevicesAvailable.add(AP3);
    }

    private double truncateNumber(double value, int decimals) {
        return Math.floor(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    @Nullable
    private Device findDeviceByMarker(Marker marker, ArrayList<Device> arrayList) {
        for (Device device : arrayList) {
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

    // Assuming that purple beacon is in (0,0), green beacon is in (0,d) and light blue beacon is in (i, j)
    private float getDistanceD(ArrayList<Device> devices) {
        return (float) getDistanceBetweenTwoDevices(devices.get(getDeviceIndex(devices, PURPLE_BEACON_NAME)), devices.get(getDeviceIndex(devices, GREEN_BEACON_NAME)));
    }

    // Assuming that purple beacon is in (0,0), green beacon is in (0,d) and light blue beacon is in (i, j)
    private float getDistanceJ(ArrayList<Device> devices) {
        Device greenBeacon = devices.get(getDeviceIndex(devices, GREEN_BEACON_NAME));
        Device purpleBeacon = devices.get(getDeviceIndex(devices, PURPLE_BEACON_NAME));
        Device lightblueBeacon = devices.get(getDeviceIndex(devices, LIGHTBLUE_BEACON_NAME));

        double a = getDistanceBetweenTwoDevices(greenBeacon, purpleBeacon);
        double b = getDistanceBetweenTwoDevices(lightblueBeacon, purpleBeacon);
        double c = getDistanceBetweenTwoDevices(greenBeacon, lightblueBeacon);
        double s = (a + b + c) / 2.0;

        return (float) (Math.pow(s * (s - a) * (s - b) * (s - c), 0.5) * (2 / a));
    }

    // Assuming that purple beacon is in (0,0), green beacon is in (0,d) and light blue beacon is in (i, j)
    private float getDistanceI(ArrayList<Device> devices) {
        Device purpleBeacon = devices.get(getDeviceIndex(devices, PURPLE_BEACON_NAME));
        Device lightblueBeacon = devices.get(getDeviceIndex(devices, LIGHTBLUE_BEACON_NAME));

        double hypotenuse = getDistanceBetweenTwoDevices(purpleBeacon, lightblueBeacon);

        return (float) Math.pow(Math.pow(hypotenuse, 2) - Math.pow(getDistanceJ(devices), 2), 0.5);
    }

    private double getDistanceBetweenTwoDevices(Device device1, Device device2) {
        double x1 = device1.getMarker().getPosition().longitude;
        double y1 = device1.getMarker().getPosition().latitude;
        double x2 = device2.getMarker().getPosition().longitude;
        double y2 = device2.getMarker().getPosition().latitude;

        return Math.pow(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2), 0.5);
    }

    private int getDeviceIndex(ArrayList<Device> devices, String name) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getName().equals(name))
                return i;
        }

        return -1;
    }
}
