package org.billroth.brgpsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import static org.billroth.brgpsapp.R.string.error_no_privs;

public class MainActivity extends AppCompatActivity {

    class NestedLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Log.v(TAG, "OnLocationChanged");
            // Called when a new location is found by the network location provider.
            makeUseOfNewLocation(location);
            // when changed, update fields
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // print status code
            Log.v(TAG,"OnStatusChanged");

            ((TextView) findViewById(R.id.value_status)).setText(provider + ": " + Integer.toString(status));
        }

        public void onProviderEnabled(String provider) {
            // update state
            Log.v(TAG,"OnProviderEnabled");

        }

        public void onProviderDisabled(String provider) {
            // update state
            Log.v(TAG,"OnProviderDisabled");

        }
    }

    private LocationManager locationManager;
    private NestedLocationListener locationListener;
    protected double lon=0, lat=0,altitude=0;
    protected float accuracy=0,speed,speed_accuracy;
    protected long epochtime=0;
    //
    protected String TAG;

    // TODO: Define textviews for items to update.

    private void makeUseOfNewLocation(Location loc) {
        // Set Long late

        if(isBetterLocation(loc)) {
            Log.v(TAG, "Setting new location");
            lon = loc.getLongitude();
            lat = loc.getLatitude();
            altitude = loc.getAltitude();
            accuracy = loc.getAccuracy();
            epochtime = loc.getTime();
            speed = loc.getSpeed();
            //speed_accuracy = loc.getSpeedAccuracyMetersPerSecond();


            ((TextView) findViewById(R.id.value_longitude)).setText(Double.toString(lon));
            ((TextView) findViewById(R.id.value_latitude)).setText(Double.toString(lat));
            ((TextView) findViewById(R.id.value_altitude)).setText(String.format("%.3f",altitude));
            ((TextView) findViewById(R.id.value_accuracy)).setText(Float.toString(accuracy));
            ((TextView) findViewById(R.id.value_epoch_time)).setText(Long.toString(epochtime));
            ((TextView) findViewById(R.id.value_epoch_time)).setText(Float.toString(speed));


        }
    }
    private boolean isBetterLocation(Location loc) {
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        TAG = getResources().getString(R.string.app_name);
        //

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // TODO: Get rid of FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        startLocationServices();

        Log.v(TAG, "onCreate done");
    }

    private void startLocationServices() {
        //
        // check for permissions. If we don't have them, as for them. If we don't get them, pop up a messaging then exit
        //
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)  {
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.coordinator1), R.string.error_no_privs, Snackbar.LENGTH_LONG);
            mySnackbar.show();
            // TODO: Consider calling Request Permissions
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // If we haver permissions, acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new NestedLocationListener();

        // Register the listener with the Location Manager to receive location updates

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000 , 0, locationListener);
        Log.v(TAG, "Location Services Requested");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // This callback is called only when there is a saved instance that is previously saved by using
// onSaveInstanceState(). We restore some state in onCreate(), while we can optionally restore
// other state here, possibly usable after onStart() has completed.
// The savedInstanceState Bundle is same as the one used in onCreate().
    //TODO: Set up save/Restore logic.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // mTextView.setText(savedInstanceState.getString(TEXT_VIEW_KEY));
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
    //
    // More lifecycle methods
    //
    @Override
    protected void onStart() {
        Log.v(TAG,"OnStart");

        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.v(TAG,"OnREsume");

        super.onResume();
    }
    @Override
    protected void onPause() {
        Log.v(TAG,"OnPause");

        super.onPause();
    }
    @Override
    protected void onStop() {
        Log.v(TAG,"OnStop");

        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.v(TAG,"OnDestroy");

        super.onDestroy();
    }
}
