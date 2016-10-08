package com.rozrost.www.smartlocationreminder;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yateen Kedare on 5/12/2016.
 */
public class GeofenceFunctions  {
    protected ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private  final Context callingActivity;
    private  final GoogleApiClient mGoogleApiClient;
    private  final DatabaseHelper mDatabaseHelper;

    Communicator communicator;

    GeofenceFunctions(GoogleApiClient GAC, Context context, DatabaseHelper DbHelper){
        this.callingActivity = context;
        this.mGoogleApiClient = GAC;
        mDatabaseHelper = DbHelper;
        communicator = (Communicator) context;
    }

    public void createAndAddGeofence(String name, double latitude, double longitude, int radius, int shr, int smin, int ehr, int emin, String task) {
        String primaryKey = null;
        try {
            primaryKey = mDatabaseHelper.addToDatabase(name,latitude,longitude,-1,radius, shr, smin, ehr, emin,task);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i("Primary key :: ", ""+ primaryKey);

        addGeofence(primaryKey,latitude,longitude,radius, true);
    }

    public void addGeofence(final String primaryKey, double latitude, double longitude, int radius, final boolean tMsg) {
        mGeofenceList = new ArrayList<Geofence>();
        mGeofencePendingIntent = null;

        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(primaryKey)

                        // Set the circular region of this geofence.
                .setCircularRegion(
                        latitude,
                        longitude,
                        radius
                )

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry transitions
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                        // Create the geofence.
                .build());

        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(callingActivity, "Google API client not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    mDatabaseHelper.changeOnOffInDatabase(primaryKey,true);
                    communicator.onGetDataClick();
                    if(tMsg) Toast.makeText(callingActivity, "Geofence added successfully", Toast.LENGTH_SHORT).show();
                }
            }); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e("Security Exception", "Invalid location permission. " +
                    "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
        }

    }

    public void removeGeofence(final String primaryKey,final boolean tMsg) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(callingActivity, "Google API client not connected please try again", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> delGeoFence = new ArrayList<String>();
        delGeoFence.add(primaryKey);

        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    delGeoFence
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    mDatabaseHelper.changeOnOffInDatabase(primaryKey, false);
                    communicator.onGetDataClick();
                    if(tMsg) Toast.makeText(callingActivity, "Geofence removed successfully", Toast.LENGTH_SHORT).show();
                }
            }); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e("Security Exception", "Invalid location permission. " +
                    "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(callingActivity, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(callingActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    public void addExistingGeofence(String primaryKey, boolean tMsg) {
        addGeofence(primaryKey,
                mDatabaseHelper.getLatitudeFromDatabase(primaryKey),
                mDatabaseHelper.getLongitudeFromDatabase(primaryKey),
                mDatabaseHelper.getRadiusFromDatabase(primaryKey),
                tMsg);
    }

    interface Communicator {
        public void onGetDataClick();
    }
}
