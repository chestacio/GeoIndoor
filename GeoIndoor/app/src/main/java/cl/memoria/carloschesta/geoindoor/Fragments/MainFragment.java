package cl.memoria.carloschesta.geoindoor.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

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
import cl.memoria.carloschesta.geoindoor.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback{

    private final int MAX_ZOOM = 14;
    private final int MIN_ZOOM = 11;
    private final int INIT_ZOOM = 12;
    private final int MAX_MARKERS = 3;
    private final LatLng NORTHEAST = new LatLng(-0.02,-53.355);
    private final LatLng SOUTHWEST = new LatLng(-0.09, -53.75);
    private final LatLng INITIAL_POS_CAMERA = new LatLng(-0.0500001,-53.500001);
    private final LatLng INITIAL_POS_MARKER = new LatLng(-0.0500001,-53.500001);

    private ArrayList<Marker> arrayMarker;
    private GoogleMap gMap;
    private MapView mMapView;
    private File f;
    private TextView tvX;
    private TextView tvY;
    private FloatingActionButton floatingActingAddMarkerButton;
    private FloatingActionButton floatingActingSettingsButton;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        arrayMarker = new ArrayList<Marker>();

        tvX = (TextView) v.findViewById(R.id.tvX);
        tvY = (TextView) v.findViewById(R.id.tvY);

        floatingActingAddMarkerButton = (FloatingActionButton) v.findViewById(R.id.floatingActingAddMarkerButton);
        floatingActingAddMarkerButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_add_location_black_24dp));
        floatingActingSettingsButton = (FloatingActionButton) v.findViewById(R.id.floatingActingSettingButton);
        floatingActingSettingsButton.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_settings_black_24dp));

        floatingActingSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.distance_settings, null);
                dialogBuilder.setView(dialogView);

                final EditText etD = (EditText) dialogView.findViewById(R.id.etD);
                final EditText etI = (EditText) dialogView.findViewById(R.id.etI);
                final EditText etJ = (EditText) dialogView.findViewById(R.id.etJ);

                final SharedPreferences prefs = getActivity().getSharedPreferences(
                        "cl.memoria.carloschesta.geoindoor.PREFERENCE_MAIN_CONFIG", Context.MODE_PRIVATE);

                float d = prefs.getFloat("d", 0);
                float i = prefs.getFloat("i", 0);
                float j = prefs.getFloat("j", 0);

                etD.setText(String.valueOf(d));
                etI.setText(String.valueOf(i));
                etJ.setText(String.valueOf(j));

                dialogBuilder.setTitle("Set position");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putFloat("d", Float.parseFloat(etD.getText().toString()));
                        editor.putFloat("i", Float.parseFloat(etI.getText().toString()));
                        editor.putFloat("j", Float.parseFloat(etJ.getText().toString()));

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
        f = new File(getActivity().getCacheDir()+"/parking.mbtiles");
        if (!f.exists()) try {

            InputStream is = getActivity().getAssets().open("parking.mbtiles");
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

                // Getting view from the layout file info_window_layout
                View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info, null);

                // Getting the position from the marker
                LatLng latLng = marker.getPosition();

                // Getting reference to the TextView to set longitude
                TextView tvX = (TextView) v.findViewById(R.id.tvMarkerX);

                // Getting reference to the TextView to set latitude
                TextView tvY = (TextView) v.findViewById(R.id.tvMarkerY);

                // Setting the longitude
                tvX.setText(String.valueOf(latLng.longitude).substring(0,7));

                // Setting the latitude
                tvY.setText(String.valueOf(latLng.latitude).substring(0,7));

                // Returning the view containing InfoWindow contents
                return v;
            }
        });

        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {  }

            @Override
            public void onMarkerDrag(Marker marker) {

                // Getting view from the layout file info_window_layout
                View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info, null);

                // Getting the position from the marker
                LatLng latLng = marker.getPosition();

                // Getting reference to the TextView to set longitude
                TextView tvX = (TextView) v.findViewById(R.id.tvMarkerX);

                // Getting reference to the TextView to set latitude
                TextView tvY = (TextView) v.findViewById(R.id.tvMarkerY);

                // Setting the longitude
                tvX.setText(String.valueOf(latLng.longitude).substring(0,7));

                // Setting the latitude
                tvY.setText(String.valueOf(latLng.latitude).substring(0,7));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {  }
        });

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                arrayMarker.remove(marker);
                marker.remove();
            }
        });

        floatingActingAddMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arrayMarker.size() != MAX_MARKERS) {
                    Marker marker = gMap.addMarker(new MarkerOptions()
                            .position(INITIAL_POS_MARKER)
                            .draggable(true));
                    arrayMarker.add(marker);
                    }
                }

        });

    }
}
