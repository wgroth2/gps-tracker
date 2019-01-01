

/*
 * *
 *  * Created by Bill Roth on 11/1/18 4:01 PM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 12/31/18 4:01 PM
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those
 *  of the authors and should not be interpreted as representing official policies,
 *  either expressed or implied, of the containing project.
 *
 */

package org.billroth.brgpsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ConfigurationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.Locale;

// import static org.billroth.brgpsapp.R.string.error_no_privs;

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
    protected final boolean debug = true;
    protected String TAG;
    protected Location currentBestLocation = null;


    // TODO: Define textviews for items to update.

    private void makeUseOfNewLocation(Location loc) {
        // Set Long late

        if(isBetterLocation(loc,currentBestLocation)) {
            currentBestLocation = loc;
            if (debug) Log.v(TAG, "Setting new location");
            lon = loc.getLongitude();
            lat = loc.getLatitude();
            altitude = loc.getAltitude();
            accuracy = loc.getAccuracy();
            epochtime = loc.getTime();
            speed = loc.getSpeed();
           //speed_accuracy = loc.getSpeedAccuracyMetersPerSecond();

            Locale locale = ConfigurationCompat.getLocales(this.getResources().getConfiguration()).get(0);
            ((TextView) findViewById(R.id.value_longitude)).setText(String.format(locale,"%.6f",lon));
            ((TextView) findViewById(R.id.value_latitude)).setText(String.format(locale,"%.6f",lat));
            ((TextView) findViewById(R.id.value_altitude)).setText(String.format(locale,"%.3f",altitude));
            ((TextView) findViewById(R.id.value_accuracy)).setText(String.format(locale,"%.3f",accuracy));
            ((TextView) findViewById(R.id.value_epoch_time)).setText(String.format(locale,"%d",epochtime));
            ((TextView) findViewById(R.id.value_speed)).setText(String.format(locale,"%.3f",speed));

            try {
                sendData("http://billroth.net/jsn.php", buildJsonObject(loc));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Todo only seeing good locations
            if (debug) Log.v(TAG,"Location not better than one before");
        }
    }
    //
    //
    protected boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (networkInfo != null && (isConnected = networkInfo.isConnected())) {
            // show "Connected" & type of network "WIFI or MOBILE"
            ((TextView) findViewById(R.id.value_network_status)).setText("Connected");
            isConnected =  true;
        } else {
            // Show Not connected
            ((TextView) findViewById(R.id.value_network_status)).setText("Not Connected");
        }
        return isConnected;
    }
    //
    //
    protected void sendData(String url, JSONObject obj) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // if successful do nothing
                        Log.v(TAG,"JSON response received");
                        // TODO: Get proper error reponses here.
                        ((TextView) findViewById(R.id.value_serv_resp)).setText("good");
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error, put error in log. Maybe send toast
                        String msg;
                        Log.v(TAG,msg = "JSON Error received" + error.getLocalizedMessage());
                        ((TextView) findViewById(R.id.value_serv_resp)).setText(msg);
                    }
                });

        // Access the RequestQueue through your singleton class.
        String s = jsonObjectRequest.getBodyContentType();
        queue.add(jsonObjectRequest);
        Log.v(TAG,"Request made");

    }
    //
    private JSONObject buildJsonObject(Location loc) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        Locale locale = ConfigurationCompat.getLocales(getApplicationContext().getResources().getConfiguration()).get(0);
        jsonObject.put("type", "Location");
        jsonObject.put("longitude", loc.getLongitude());
        jsonObject.put("latitude", loc.getLatitude());
        jsonObject.put("altitude", loc.getAltitude());
        jsonObject.put("accuracy", loc.getAccuracy());
        jsonObject.put("speed", loc.getSpeed());
        jsonObject.put("epochtime",(long) loc.getTime() / 1000);
        jsonObject.put("tags", "location-update");
        jsonObject.put("userid", 0);



        return jsonObject;
    }
//
    //
    //
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        if(isSamePlace(location,currentBestLocation)) {
            // same as last time
            Log.v(TAG,"Same as last time");
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
            // changing below: isNewer && !isLessAccurate
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    private boolean isSamePlace(Location one, Location two) {
        if(one.getLongitude() == two.getLongitude() &&
                one.getLatitude() == two.getLatitude() &&
                one.getAltitude() == two.getAltitude())
            return true;
        else
            return false;
    }
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    //
    //
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        TAG = getResources().getString(R.string.app_name);
        //
        Log.v(TAG,"OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // TODO: Get rid of FAB, or connect to SMS.
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

            }

        Log.v(TAG, "onCreate done");
    }
    //
    // TODO: COnsider rewrite: https://stackoverflow.com/questions/43318968/how-to-make-a-simple-tracking-android-app-using-android-studio
    //
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
        // TODO: Set this to the time in the preferences.
        //
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  60 * 1000 , 0, locationListener);
        Log.v(TAG, "Location Services Requested");
    }
    //
    //
    //
    private void stopLocationServices() {
        locationManager.removeUpdates(locationListener);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission with request code 1 granted
                    Toast.makeText(this, "Permission Granted" , Toast.LENGTH_LONG).show();
                    startLocationServices();
                } else {
                    //permission with request code 1 was not granted
                    Toast.makeText(this, "Permission was not Granted" , Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        startLocationServices();
        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.v(TAG,"OnResume");

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
        stopLocationServices();
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.v(TAG,"OnDestroy");
        super.onDestroy();
    }
}
