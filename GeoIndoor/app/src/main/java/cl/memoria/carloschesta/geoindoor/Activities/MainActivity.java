package cl.memoria.carloschesta.geoindoor.Activities;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import cl.memoria.carloschesta.geoindoor.Adapter.ViewPagerAdapter;
import cl.memoria.carloschesta.geoindoor.Fragments.BluetoothFragment;
import cl.memoria.carloschesta.geoindoor.Fragments.MainFragment;
import cl.memoria.carloschesta.geoindoor.Fragments.WifiFragment;
import cl.memoria.carloschesta.geoindoor.R;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragments(new MainFragment(), "Main");
        //viewPagerAdapter.addFragments(new WifiFragment(), "WiFi");
        //viewPagerAdapter.addFragments(new BluetoothFragment(), "Bluetooth");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
