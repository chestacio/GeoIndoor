package cl.memoria.carloschesta.geoindoor.Fragments;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.memoria.carloschesta.geoindoor.Adapter.DeviceSelectAdapter;
import cl.memoria.carloschesta.geoindoor.Connection.BLeConnection;
import cl.memoria.carloschesta.geoindoor.Connection.WifiConnection;
import cl.memoria.carloschesta.geoindoor.Libraries.MapBoxOfflineTileProvider;
import cl.memoria.carloschesta.geoindoor.Libraries.SVGtoBitmap;
import cl.memoria.carloschesta.geoindoor.Model.Device;
import cl.memoria.carloschesta.geoindoor.R;

import static cl.memoria.carloschesta.geoindoor.Utils.Utils.getDistance;
import static cl.memoria.carloschesta.geoindoor.Utils.Utils.truncateNumber;

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
    private final LatLng INITIAL_POS_CAMERA = new LatLng(14.425, 77.009);
    private final LatLng INITIAL_POS_MARKER = new LatLng(14.425, 77.009);
    private final LatLng INITIAL_POS_DEVICE1 = new LatLng(6.10, 68.749);
    private final LatLng INITIAL_POS_DEVICE2 = new LatLng(6.152, 84.336);
    private final LatLng INITIAL_POS_DEVICE3 = new LatLng(22.000, 76.302);
    private final String PARKING_FILE_NAME = "parking_origin_0-145_HD.mbtiles";
    private final String BEACON1_MAC = "F1:50:7A:27:67:4F";
    private final String BEACON2_MAC = "DB:2A:7D:35:34:F7";
    private final String BEACON3_MAC = "C3:3C:D0:40:ED:64";
    private final String BEACON2_NAME = "Green Beacon (P2)";
    private final String BEACON3_NAME = "Light Blue Beacon (P3)";
    private final String AP2_NAME = "Access Point Buffalo with band-aid (P2)";
    private final String AP3_NAME = "Access Point Buffalo (P3)";
    private final String AP1_MAC = "F8:1A:67:F6:61:9C";
    private final String AP2_MAC = "00:16:01:D1:85:3C";
    private final String AP3_MAC = "00:16:01:D2:6F:CE";

    private static final String BEACON1_NAME = "Purple Beacon (P1)";
    private static final String AP1_NAME = "Access Point TP-Link (P1)";

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static List<String[]> csvData;
    private static Marker calculatedPositionMarker;
    private static Marker realUserPositionMarker;
    private static Float distanceD;
    private static Float distanceI;
    private static Float distanceJ;
    private static ArrayList<Device> arrayDevicesCreated;

    private ArrayList<Device> arrayBeaconDevicesAvailable;
    private ArrayList<Device> arrayAPDevicesAvailable;
    private BLeConnection BLeConnection;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean devicesRecentlyMoved = false;
    private DeviceSelectAdapter adapter;
    private ColorStateList floatingActionButtonOriginalColor;
    private GoogleMap gMap;
    private MapView mMapView;
    private File file;
    private TextView tvX;
    private TextView tvY;
    private FloatingActionButton faAddMarkerButton;
    private FloatingActionButton faSettingsButton;
    private FloatingActionButton faGetLocationButton;
    private FloatingActionButton faAddRealPersonButton;
    private WifiConnection wifiConnection;
    private WifiManager wifiManager;
    private SharedPreferences prefs;


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
        faAddRealPersonButton = (FloatingActionButton) v.findViewById(R.id.faAddRealPersonButton);

        faAddMarkerButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_add_location_black_24dp));
        faSettingsButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_settings_black_24dp));
        faGetLocationButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_my_location_black_24dp));
        faAddRealPersonButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_standing_frontal_man_silhouette));

        // Import local preferences (distances between Devices)
        prefs = getActivity().getSharedPreferences("cl.memoria.carloschesta.geoindoor.PREFERENCE_MAIN_CONFIG", Context.MODE_PRIVATE);

        // Import and export distances settings
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

                distanceD = prefs.getFloat("distanceD", 0);
                distanceI = prefs.getFloat("distanceI", 0);
                distanceJ = prefs.getFloat("distanceJ", 0);

                // Set real distances values
                if (arrayDevicesCreated.size() == MAX_DEVICES && devicesRecentlyMoved) {
                    distanceD = getDistanceD(arrayDevicesCreated);
                    distanceI = getDistanceI(arrayDevicesCreated);
                    distanceJ = getDistanceJ(arrayDevicesCreated);

                    devicesRecentlyMoved = false;
                }

                etDistanceD.setText(String.valueOf(truncateNumber(distanceD, 2)));
                etDistanceI.setText(String.valueOf(truncateNumber(distanceI, 2)));
                etDistanceJ.setText(String.valueOf(truncateNumber(distanceJ, 2)));

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

        // Real magic here
        faGetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check for consistency
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
                if (calculatedPositionMarker == null) {
                    MarkerOptions calculatedPositionMarkerOptions = new MarkerOptions();
                    calculatedPositionMarkerOptions.draggable(true);
                    calculatedPositionMarkerOptions.position(INITIAL_POS_MARKER);
                    calculatedPositionMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_position_icon)));

                    calculatedPositionMarker = gMap.addMarker(calculatedPositionMarkerOptions);
                    calculatedPositionMarker.setTag("calculatedPositionMarkerOptions");

                    // Set new color to Location Button
                    floatingActionButtonOriginalColor = faGetLocationButton.getBackgroundTintList();
                    faGetLocationButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0, 170, 0)));

                    // Set visible Add Real Person Button
                    faAddRealPersonButton.setVisibility(View.VISIBLE);

                    // Array which there will be all the data inside it
                    csvData = new ArrayList<String[]>();

                    // Run geolocation
                    if (arrayDevicesCreated.get(0).isAP())
                        wifiConnection.run();
                    else
                        BLeConnection.startScanLe(true);
                }


                // Stop geolocation
                else{
                    calculatedPositionMarker.remove();
                    calculatedPositionMarker = null;

                    // Reset color to Location Button
                    faGetLocationButton.setBackgroundTintList(floatingActionButtonOriginalColor);

                    // Hide Add Real Person Button
                    faAddRealPersonButton.setVisibility(View.GONE);

                    // Remove real user marker
                    if (realUserPositionMarker != null) {
                        realUserPositionMarker.remove();
                        realUserPositionMarker = null;
                    }

                    // Save CSV data
                    // Request writing external storage permission if needed
                    int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        // We don't have permission so prompt the user
                        ActivityCompat.requestPermissions(
                                getActivity(),
                                PERMISSIONS_STORAGE,
                                REQUEST_EXTERNAL_STORAGE
                        );
                    }

                    // Return a string with the following format
                    String csvNameFile = (new SimpleDateFormat("yyyy-M-dd hh-mm-ss")).format(new Date()) + ".csv";

                    // Saved at /storage/emulated/0/Documents
                    String csv = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + csvNameFile;
                    CSVWriter csvWriter = null;

                    // Write data if contains it
                    if (!csvData.isEmpty()) {
                        try {
                            csvWriter = new CSVWriter(new FileWriter(csv), ';');
                            csvWriter.writeAll(csvData);
                            csvWriter.close();

                            Toast.makeText(getContext(), "CSV file created successfully", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "CSV file not created", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }

                    // Reset the csv array list
                    csvData.clear();

                    // Cancel geolocation algorithms
                    if (arrayDevicesCreated.get(0).isAP())
                        wifiConnection.cancel();
                    else
                        BLeConnection.startScanLe(false);

                }

            }
        });

        // Add/remove the real location marker for measure distance errors
        faAddRealPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (realUserPositionMarker == null) {
                    Float realUserPositionLat = prefs.getFloat("realUserPositionLat", (float) INITIAL_POS_MARKER.latitude);
                    Float realUserPositionLng = prefs.getFloat("realUserPositionLng", (float) INITIAL_POS_MARKER.longitude);

                    MarkerOptions realUserMarkerOptions = new MarkerOptions();
                    realUserMarkerOptions.draggable(true);
                    realUserMarkerOptions.position(new LatLng(realUserPositionLat, realUserPositionLng));
                    realUserMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_standing_frontal_man_silhouette)));

                    realUserPositionMarker = gMap.addMarker(realUserMarkerOptions);
                    realUserPositionMarker.setTag("realUserMarker");
                }
                else {
                    realUserPositionMarker.remove();
                    realUserPositionMarker = null;
                }

            }
        });

        // Initialize Google Map
        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mMapView.getMapAsync(this);
        mMapView.onSaveInstanceState(savedInstanceState);

        // Access compass in order to move it a bit further away from the top
        ViewGroup parent = (ViewGroup) mMapView.findViewById(Integer.parseInt("1")).getParent();
        View compassButton = parent.getChildAt(4);

        RelativeLayout.LayoutParams CompRlp = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
        CompRlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        CompRlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        CompRlp.setMargins(0, 180, 180, 0);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get a File from Assets reference to the MBTiles file.
        file = new File(getActivity().getCacheDir()+"/" + PARKING_FILE_NAME);
        if (!file.exists()) try {

            InputStream is = getActivity().getAssets().open(PARKING_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) { throw new RuntimeException(e); }


        // Initialize arrays and connections
        arrayDevicesCreated = new ArrayList<Device>();
        arrayBeaconDevicesAvailable = new ArrayList<Device>();
        arrayAPDevicesAvailable = new ArrayList<Device>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BLeConnection = new BLeConnection(getActivity(), mBluetoothAdapter);

        wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiConnection = new WifiConnection(getActivity(), wifiManager);

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
        MapBoxOfflineTileProvider provider = new MapBoxOfflineTileProvider(file);

        // Set the tile provider on the TileOverlayOptions.
        opts.tileProvider(provider);

        // Add the tile overlay to the map.
        TileOverlay overlay = gMap.addTileOverlay(opts);
    }

    private void setUpMap() {
        gMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setMapToolbarEnabled(false);
        gMap.getUiSettings().setCompassEnabled(true);


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

                LatLng latLng = marker.getPosition();

                if (marker.getTag().equals("device")){
                    View v = getActivity().getLayoutInflater().inflate(R.layout.device_info, null);

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

                if (marker.getTag().equals("realUserMarker")) {
                    View v = getActivity().getLayoutInflater().inflate(R.layout.user_position_info, null);

                    TextView tvUserX = (TextView) v.findViewById(R.id.tvUserX);
                    TextView tvUserY = (TextView) v.findViewById(R.id.tvUserY);

                    tvUserX.setText(String.valueOf(truncateNumber(latLng.longitude, DECIMALS)));
                    tvUserY.setText(String.valueOf((truncateNumber(latLng.latitude, DECIMALS))));

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

                LatLng latLng = marker.getPosition();

                tvX.setText(String.valueOf(latLng.longitude));
                tvY.setText(String.valueOf(latLng.latitude));

                if (marker.getTag().equals("device")){
                    View v = getActivity().getLayoutInflater().inflate(R.layout.device_info, null);

                    TextView tvDeviceX = (TextView) v.findViewById(R.id.tvDeviceX);
                    TextView tvDeviceY = (TextView) v.findViewById(R.id.tvDeviceY);

                    tvDeviceX.setText(String.valueOf(truncateNumber(latLng.longitude, DECIMALS)));
                    tvDeviceY.setText(String.valueOf((truncateNumber(latLng.latitude, DECIMALS))));
                }

                if (marker.getTag().equals("realUserMarker")) {
                    View v = getActivity().getLayoutInflater().inflate(R.layout.user_position_info, null);

                    TextView tvUserX = (TextView) v.findViewById(R.id.tvUserX);
                    TextView tvUserY = (TextView) v.findViewById(R.id.tvUserY);

                    tvUserX.setText(String.valueOf(truncateNumber(latLng.longitude, DECIMALS)));
                    tvUserY.setText(String.valueOf((truncateNumber(latLng.latitude, DECIMALS))));
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                SharedPreferences.Editor editor = prefs.edit();

                if (marker.getTag().equals("realUserMarker")) {

                    editor.putFloat("realUserPositionLat",(float) marker.getPosition().latitude);
                    editor.putFloat("realUserPositionLng",(float) marker.getPosition().longitude);

                    double distance = getDistanceBetweenTwoMarkers(marker, calculatedPositionMarker);

                    Toast.makeText(getContext(), "Distance: " + truncateNumber(distance, 2) + " meters", Toast.LENGTH_SHORT).show();
                }

                if (marker.getTag().equals("device")) {

                    if (marker.getTitle().equals("Device1")) {
                        editor.putFloat("device1Lat", (float) marker.getPosition().latitude);
                        editor.putFloat("device1Lng", (float) marker.getPosition().longitude);
                    }

                    if (marker.getTitle().equals("Device2")) {
                        editor.putFloat("device2Lat", (float) marker.getPosition().latitude);
                        editor.putFloat("device2Lng", (float) marker.getPosition().longitude);
                    }

                    if (marker.getTitle().equals("Device3")) {
                        editor.putFloat("device3Lat", (float) marker.getPosition().latitude);
                        editor.putFloat("device3Lng", (float) marker.getPosition().longitude);
                    }
                }

                editor.commit();

                devicesRecentlyMoved = true;
            }
        });

        // The device (marker) will be removed when it description is clicked
        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                if (marker.getTag().equals("device")) {
                    Device device = findDeviceByMarker(marker, arrayDevicesCreated);

                    if (device.isAP())
                        arrayAPDevicesAvailable.add(device);
                    else
                        arrayBeaconDevicesAvailable.add(device);

                    arrayDevicesCreated.remove(device);
                    adapter.notifyDataSetChanged();
                }

                if (marker.getTag().equals("realUserMarker"))
                    realUserPositionMarker = null;

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

                            // Get previous device position
                            Float device1Lat = prefs.getFloat("device1Lat", (float) INITIAL_POS_DEVICE1.latitude);
                            Float device1Lng = prefs.getFloat("device1Lng", (float) INITIAL_POS_DEVICE1.longitude);
                            Float device2Lat = prefs.getFloat("device2Lat", (float) INITIAL_POS_DEVICE2.latitude);
                            Float device2Lng = prefs.getFloat("device2Lng", (float) INITIAL_POS_DEVICE2.longitude);
                            Float device3Lat = prefs.getFloat("device3Lat", (float) INITIAL_POS_DEVICE3.latitude);
                            Float device3Lng = prefs.getFloat("device3Lng", (float) INITIAL_POS_DEVICE3.longitude);

                            Device device = (Device) spinnerDeviceList.getSelectedItem();

                            // Setting proper icon to marker
                            if (device.isAP()) {
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_wifi)));

                                if (device.getName().equals(AP1_NAME))
                                    markerOptions.position(new LatLng((double) device1Lat, (double) device1Lng));

                                if (device.getName().equals(AP2_NAME))
                                    markerOptions.position(new LatLng((double) device2Lat, (double) device2Lng));

                                if (device.getName().equals(AP3_NAME))
                                    markerOptions.position(new LatLng((double) device3Lat, (double) device3Lng));

                                arrayAPDevicesAvailable.remove(device);
                            }
                            else {

                                if (device.getName().equals(BEACON1_NAME)) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_ble_beacon_icon_purple)));
                                    markerOptions.position(new LatLng((double) device1Lat, (double) device1Lng));
                                }

                                if (device.getName().equals(BEACON2_NAME)) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_ble_beacon_icon_green)));
                                    markerOptions.position(new LatLng((double) device2Lat, (double) device2Lng));
                                }

                                if (device.getName().equals(BEACON3_NAME)) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(SVGtoBitmap.getBitmap(getContext(), R.drawable.ic_ble_beacon_icon_lightblue)));
                                    markerOptions.position(new LatLng((double) device3Lat, (double) device3Lng));
                                }
                                arrayBeaconDevicesAvailable.remove(device);
                            }

                            // Creating the marker with the options
                            Marker marker = gMap.addMarker(markerOptions);
                            marker.setTag("device");

                            device.setMarker(marker);

                            if (device.getName().equals(AP1_NAME) || device.getName().equals(BEACON1_NAME))
                                marker.setTitle("Device1");
                            if (device.getName().equals(AP2_NAME) || device.getName().equals(BEACON2_NAME))
                                marker.setTitle("Device2");
                            if (device.getName().equals(AP3_NAME) || device.getName().equals(BEACON3_NAME))
                                marker.setTitle("Device3");

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

        LightBlueBeacon.setName(BEACON3_NAME);
        LightBlueBeacon.setMAC(BEACON3_MAC);
        LightBlueBeacon.setAP(false);

        PurpleBeacon.setName(BEACON1_NAME);
        PurpleBeacon.setMAC(BEACON1_MAC);
        PurpleBeacon.setAP(false);

        GreenBeacon.setName(BEACON2_NAME);
        GreenBeacon.setMAC(BEACON2_MAC);
        GreenBeacon.setAP(false);

        AP1.setName(AP1_NAME);
        AP1.setMAC(AP1_MAC);
        AP1.setAP(true);

        AP2.setName(AP2_NAME);
        AP2.setMAC(AP2_MAC);
        AP2.setAP(true);

        AP3.setName(AP3_NAME);
        AP3.setMAC(AP3_MAC);
        AP3.setAP(true);

        arrayBeaconDevicesAvailable.add(PurpleBeacon);
        arrayBeaconDevicesAvailable.add(GreenBeacon);
        arrayBeaconDevicesAvailable.add(LightBlueBeacon);

        arrayAPDevicesAvailable.add(AP1);
        arrayAPDevicesAvailable.add(AP2);
        arrayAPDevicesAvailable.add(AP3);
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

    private double getDistanceBetweenTwoMarkers(Marker marker1, Marker marker2) {
        return getDistance(marker1.getPosition(), marker2.getPosition());
    }

    private int getDeviceIndex(ArrayList<Device> devices, String name) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getName().equals(name))
                return i;
        }

        return -1;
    }

    private static Device findDeviceByName(ArrayList<Device> devices, String name) {

        for (Device device : devices) {
            if (device.getName().equals(name))
                return device;

        }

        return null;
    }

    public static LatLng shiftPosition(LatLng position) {

        LatLng posShift = null;

        double x = position.longitude;
        double y = position.latitude;

        // First device is not at (0,0) absolute so there is a shift in marker position
        if (arrayDevicesCreated.get(0).isAP())
            posShift = findDeviceByName(arrayDevicesCreated, AP1_NAME).getMarker().getPosition();
        else
            posShift = findDeviceByName(arrayDevicesCreated, BEACON1_NAME).getMarker().getPosition();

        x = x + posShift.longitude;
        y = y + posShift.latitude;

        LatLng calcPos =  new LatLng(y, x);

        return calcPos;
    }

    public static void setMarkerPosition(LatLng position) {
        calculatedPositionMarker.setPosition(position);
    }

    public static void setLocationAndAddDataToCSV(long msFromEpoch, int deviceType, LatLng calcPos) {

        LatLng shiftPos = shiftPosition(calcPos);

        // Move the marker on the map
        setMarkerPosition(shiftPos);

        String distErrorString = "";
        String realPosString = "";

        if (realUserPositionMarker != null) {
            double realLat = realUserPositionMarker.getPosition().latitude;
            double realLng = realUserPositionMarker.getPosition().longitude;
            double distError = getDistance(shiftPos, realUserPositionMarker.getPosition());

            distErrorString = String.valueOf(distError);

            realPosString = String.format("%f,%f", realLng, realLat);

            String timestamp = (new SimpleDateFormat("yyyy-M-dd hh:mm:ss")).format(new Date(msFromEpoch));
            String calcPosString = String.format("%f,%f", shiftPos.longitude, shiftPos.latitude);
            String device1Position = String.valueOf(arrayDevicesCreated.get(0).getMarker().getPosition());
            String device2Position = String.valueOf(arrayDevicesCreated.get(1).getMarker().getPosition());
            String device3Position = String.valueOf(arrayDevicesCreated.get(2).getMarker().getPosition());
            String deviceName = String.valueOf(arrayDevicesCreated.get(0).getName());

            csvData.add(new String[] {timestamp, deviceName, device1Position, device2Position, device3Position, calcPosString, realPosString, distErrorString});
        }


    }

    public static Marker getCalculatedPositionMarker() {
        return calculatedPositionMarker;
    }

    public static Float getDistanceD() {
        return distanceD;
    }

    public static Float getDistanceI() {
        return distanceI;
    }

    public static Float getDistanceJ() {
        return distanceJ;
    }

    // Assuming that device 1 is in (0,0), device 2 is in (0,d) and device 3 is in (i, j)
    private float getDistanceD(ArrayList<Device> devices) {
        int indexDevice1;
        int indexDevice2;

        if (devices.get(0).isAP()) {
            indexDevice1 = getDeviceIndex(devices, AP1_NAME);
            indexDevice2 = getDeviceIndex(devices, AP2_NAME);
        }
        else {
            indexDevice1 = getDeviceIndex(devices, BEACON1_NAME);
            indexDevice2 = getDeviceIndex(devices, BEACON2_NAME);
        }

        Device device1 = devices.get(indexDevice1);
        Device device2 = devices.get(indexDevice2);

        Marker marker1 = device1.getMarker();
        Marker marker2 = device2.getMarker();

        double distances = getDistanceBetweenTwoMarkers(marker1, marker2);

        return (float) distances;
    }

    // Assuming that device 1 is in (0,0), device 2 is in (0,d) and device 3 is in (i, j)
    private float getDistanceJ(ArrayList<Device> devices) {

        Device device1;
        Device device2;
        Device device3;

        if (devices.get(0).isAP()) {
            device1 = devices.get(getDeviceIndex(devices, AP1_NAME)); // TP-Link
            device2 = devices.get(getDeviceIndex(devices, AP2_NAME)); // Buffalo with Band-Aid
            device3 = devices.get(getDeviceIndex(devices, AP3_NAME)); // Buffalo
        }
        else {
            device1 = devices.get(getDeviceIndex(devices, BEACON1_NAME)); // Purple
            device2 = devices.get(getDeviceIndex(devices, BEACON2_NAME)); // Green
            device3 = devices.get(getDeviceIndex(devices, BEACON3_NAME)); // Light Blue
        }

        double a = getDistanceBetweenTwoMarkers(device2.getMarker(), device1.getMarker());
        double b = getDistanceBetweenTwoMarkers(device3.getMarker(), device1.getMarker());
        double c = getDistanceBetweenTwoMarkers(device2.getMarker(), device3.getMarker());
        double s = (a + b + c) / 2.0;

        return (float) (Math.pow(s * (s - a) * (s - b) * (s - c), 0.5) * (2 / a));
    }

    // Assuming that device 1 is in (0,0), device 2 is in (0,d) and device 3 is in (i, j)
    private float getDistanceI(ArrayList<Device> devices) {
        Device device1;
        Device device3;

        if (devices.get(0).isAP()) {
            device1 = devices.get(getDeviceIndex(devices, AP1_NAME)); // TP-Link
            device3 = devices.get(getDeviceIndex(devices, AP3_NAME)); // Buffalo
        }
        else {
            device1 = devices.get(getDeviceIndex(devices, BEACON1_NAME)); // Purple
            device3 = devices.get(getDeviceIndex(devices, BEACON3_NAME)); // Light Blue
        }

        double hypotenuse = getDistanceBetweenTwoMarkers(device1.getMarker(), device3.getMarker());

        return (float) Math.pow(Math.pow(hypotenuse, 2) - Math.pow(getDistanceJ(devices), 2), 0.5);
    }

}
