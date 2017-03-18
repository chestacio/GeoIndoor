package cl.memoria.carloschesta.geoindoor.Fragments;


import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import cl.memoria.carloschesta.geoindoor.Libraries.MapBoxOfflineTileProvider;
import cl.memoria.carloschesta.geoindoor.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback{

    private GoogleMap gMap;
    private MapView mMapView;
    private TileOverlayOptions opts;
    private File parkingFile;
    private MapBoxOfflineTileProvider provider;
    private TileOverlay overlay;
    private AssetManager am;
    private InputStream is;
    private File f;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /**
        am = getActivity().getAssets();
        try {
            is = am.open("parking.mbtiles");
        } catch (IOException e) {
            Log.e("Parking.mbtiles", "Ocurrio un error al leer el archivo");
            e.printStackTrace();
        }

        try {
            file = new File("parking.mbtiles");
            FileUtils.copyInputStreamToFile(is, file);
        } catch (IOException e) {
            Log.e("Convertir archivo", "Ocurrio un error al convertir el archivo");
            e.printStackTrace();
        }
        */

        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


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

        //map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        // Inflate the layout for this fragment
        return v;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        setUpMap();
        opts = new TileOverlayOptions();
        provider = new MapBoxOfflineTileProvider(f);
        opts.tileProvider(provider);
        overlay = gMap.addTileOverlay(opts);
    }

    private void setUpMap() {
        gMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        //gMap.setMyLocationEnabled(true);
        //gMap.setTrafficEnabled(true);
        //gMap.setIndoorEnabled(true);
        //gMap.setBuildingsEnabled(true);
        gMap.getUiSettings().setZoomControlsEnabled(true);
        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(new LatLng(-0.106087,-53.766060), 13);
        gMap.moveCamera(upd);
    }
}
