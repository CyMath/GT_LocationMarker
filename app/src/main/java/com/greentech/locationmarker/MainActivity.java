package com.greentech.locationmarker;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.Point;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>{



    final static int REQUEST_ADD_LOCATION = 0x2;
    Point location;

    String locationInput, infoInput, buildingInput;
    double longitude, latitude;

    private GoogleApiClient mLocationClient;
    private Location mCurrentLocation;
    LocationRequest mLocationRequest;
    JSONArray jArray;
    ArrayList<String> listOfMarkedLocations;

    ListView list;
    ListAdapter adapter;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_INTERVAL =
            UPDATE_INTERVAL / 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        File gFile = getFilesDir();
        String path = gFile.getAbsolutePath();

        jArray = new JSONArray();
        listOfMarkedLocations = new ArrayList<String>();

        readFile();

        list = (ListView) findViewById(R.id.lv_main);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listOfMarkedLocations);

        list.setAdapter(adapter);


        mLocationClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        buildLocationSettingsRequest();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), InputActivity.class);
                Bundle giveBundle = new Bundle();

                if (mCurrentLocation != null) {
                    longitude = mCurrentLocation.getLongitude();
                    latitude = mCurrentLocation.getLatitude();

                    giveBundle.putString("lng",  Double.toString(longitude));
                    giveBundle.putString("lat", Double.toString(latitude));
                }

                intent.putExtras(giveBundle);
                startActivityForResult(intent, REQUEST_ADD_LOCATION);

            }
        });

    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mLocationClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i("TAG", "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i("TAG", "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("TAG", "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("TAG", "User chose not to make required location settings changes.");
                        break;
                }
                break;

            case REQUEST_ADD_LOCATION:
                if (resultCode == RESULT_OK) {

                    infoInput = data.getExtras().getString("Info");
                    buildingInput = data.getExtras().getString("Building");

                    location = new Point(longitude, latitude);
                    JSONObject geoEntry = createGEntry(infoInput, buildingInput, location);

                    StringBuilder saveString = new StringBuilder();

                    saveString.append(listOfMarkedLocations.size()+1 + "\n");
                    saveString.append(infoInput + " \n");
                    saveString.append(buildingInput);

                    jArray.put(geoEntry);
                    listOfMarkedLocations.add(saveString.toString());

                    try {
                        writeToFile(jArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private JSONObject createGEntry(String info, String building, Point location)
    {
        Feature feature = new Feature(location);
        JSONObject jObject = new JSONObject();
        JSONObject geoEntry = new JSONObject();

        try
        {
            jObject.put("name", info);
            jObject.put("building", building);
            feature.setProperties(jObject);
            geoEntry = feature.toJSON();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return geoEntry;
    }

    private void writeToFile(JSONArray gArray) throws IOException
    {
        FileOutputStream fos = openFileOutput("geoJSON.json", MODE_PRIVATE);
        fos.write(gArray.toString().getBytes());
        fos.close();

        Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
    }

    private void readFile()
    {
        try {

            FileInputStream fis = openFileInput("geoJSON.json");
            BufferedInputStream bis = new BufferedInputStream(fis);
            StringBuffer buffer = new StringBuffer();

            while (bis.available() != 0) {
                char c = (char) bis.read();
                buffer.append(c);
            }

            bis.close();
            fis.close();

            JSONArray gArray = new JSONArray(buffer.toString());

            for (int i = 0; i < gArray.length(); i++) {
                String name = gArray.getJSONObject(i).getJSONObject("properties").getString("name");
                String building = gArray.getJSONObject(i).getJSONObject("properties").getString("building");
                String tmpPoint = gArray.getJSONObject(i).getJSONObject("geometry").getString("coordinates");

                tmpPoint = tmpPoint.replace("[", "");
                tmpPoint = tmpPoint.replace("]", "");
                String[] point = tmpPoint.split(",");

                Point location = new Point(Double.parseDouble(point[0]), Double.parseDouble(point[1]));

                StringBuilder saveString = new StringBuilder();

                saveString.append(listOfMarkedLocations.size()+1 + "\n");
                saveString.append(name + " \n");
                saveString.append(building);

                JSONObject gEntry = createGEntry(name, building, location);
                jArray.put(gEntry);
                listOfMarkedLocations.add(saveString.toString());
            }
        }
        catch (IOException e)
        {
            Toast.makeText(this, "No JSON File", Toast.LENGTH_SHORT).show();
        }
        catch (JSONException e)
        {
            e.getStackTrace();
        }

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
        if (id == R.id.action_upload) {
            Intent intent = new Intent(this, GDriveActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mLocationClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mLocationClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopLocationUpdates();
        mLocationClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location Update", "CHANGED");
        mCurrentLocation = location;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "Connection Failed. Please try again.", Toast.LENGTH_SHORT).show();

    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            mRequestingLocationUpdates = true;
                        }
                    });

        }
        catch (SecurityException e)
        {
            checkLocationSettings();
        }

    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this)
                .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }


}
