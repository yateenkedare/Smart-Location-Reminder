package com.rozrost.www.smartlocationreminder;

//https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi#constant-summary
//http://developer.android.com/intl/es/training/location/change-location-settings.html
//https://github.com/googlesamples/android-play-location

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.miguelcatalan.materialsearchview.MaterialSearchView;


import java.text.ParseException;
import java.util.ArrayList;

import static com.rozrost.www.smartlocationreminder.GetLocation.REQUEST_CHECK_SETTINGS;
import static com.rozrost.www.smartlocationreminder.GetLocation.TAG;

public class MainActivity extends AppCompatActivity  implements GeofenceFunctions.Communicator{

    boolean ShowDataDialog = false;
    GetData getData;

    private final int PLACE_PICKER_REQUEST = 15;

    //protected static final String TAG = "MainActivity";
    //search box variable
    private MaterialSearchView searchView;

    //Loading Spinner Variable
    MySpinnerDialog myInstance;
    FragmentManager fm;

    GetLocation getLocation = new GetLocation(MainActivity.this);

    PlacePicker.IntentBuilder builder;
    Intent PlacePickerIntent;

    GeofenceFunctions mGeofenceFunctions;
    DatabaseHelper mDatabaseHelper;

    ListView listView;
    ArrayList<SingleRow> list;
    Cursor dbCursor;
    MyListAdapter myListAdapter;

    boolean start = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing Getting Location Load Screan
        fm = getSupportFragmentManager();
        myInstance = new MySpinnerDialog();


        builder = new PlacePicker.IntentBuilder();
        try {
            PlacePickerIntent = builder.build(MainActivity.this);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }


        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        getLocation.mRequestingLocationUpdates = false;
        getLocation.initialise();

        //https://github.com/MiguelCatalan/MaterialSearchView
        //Above mentioned library has been used for search bar and animations
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setHint("Search nearby places");
        searchView.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorDivider));
        searchView.setEllipsize(true);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    getLocation.mLocationStatus = false;
                    //Getting Updated Location
                    getLocation.checkLocationSettings();

                    GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
                    getNearbyPlaces.execute(query);

                } catch (Exception e) {
                    getLocation.mLocationStatus = true;
                    Toast.makeText(MainActivity.this, "Unable to get location. Please update Google play services app.", Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_add_white_24px);
        fab.setOnClickListener(new View.OnClickListener() {
            //Location Reminder Code will be added in the following function
            @Override
            public void onClick(View view) {
                //https://developers.google.com/places/android-api/placepicker#add
                //Google Place picker API
                startActivityForResult(PlacePickerIntent, PLACE_PICKER_REQUEST);

            }
        });


        mDatabaseHelper = new DatabaseHelper(this);

        mGeofenceFunctions = new GeofenceFunctions(getLocation.mGoogleApiClient, this, mDatabaseHelper);

        //initializing the list view for Reminders
        listView = (ListView) findViewById(R.id.list_view);
        listView.setEmptyView(findViewById(R.id.empty));
        refreshListView();

        CheckGoogleClient mcheckGoogleClient = new CheckGoogleClient();
        mcheckGoogleClient.execute();
    }

    @Override
    public void onGetDataClick() {
        refreshListView();
    }

//    @Override
//    public void onClientConnected() {
//        if(!start) {
//            for (SingleRow l : list) {
//                if (l.status)
//                    mGeofenceFunctions.addExistingGeofence(l.PrimaryKey, false);
//            }
//            start = true;
//        }
//    }

    class CheckGoogleClient extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while(!getLocation.mGoogleApiClient.isConnected()){
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!start) {
                for (SingleRow l : list) {
                    if (l.status)
                        mGeofenceFunctions.addExistingGeofence(l.PrimaryKey, false);
                    else
                        mGeofenceFunctions.removeGeofence(l.PrimaryKey, false);
                }
                start = true;
            }
        }
    }


    class GetNearbyPlaces extends AsyncTask<String, Void, String> {
        long start_time,elapsed_time;
        @Override
        protected void onPreExecute() {
            myInstance.show(fm, "some_tag");
            start_time = System.currentTimeMillis();
        }

        @Override
        protected String doInBackground(String... params) {
            while (!getLocation.mLocationStatus) {
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(String query) {

            //Stop Updating Location in order to save Power
            getLocation.stopLocationUpdates();
            //open Google Maps and send search query
            //https://developers.google.com/maps/documentation/android-api/intents#overview
            try {
                Uri gmmIntentUri;
                if(getLocation.mCurrentLocation != null) {
                    gmmIntentUri = Uri.parse("geo:" + getLocation.mCurrentLocation.getLatitude() + "," + getLocation.mCurrentLocation.getLongitude() + "?q=" + query);
                }
                else {
                    gmmIntentUri = Uri.parse("geo:0,0?q=" + query);
                }
                myInstance.dismiss();

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }catch (Exception e) {
                //If Google maps are not installed, generate an Alert Dialog to prompt user to install google maps
                //In some devices such as Xiaomi, Google play services are not installed by default so we cannot route the
                //user directly to google play store(It'll generate an error and the application will stop running). Instead
                //we open the link to Install Google Maps in a browser(Since there is always a default browser present)
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("Please install Google Maps and try again");

                alertDialogBuilder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps&hl=en"));
                            startActivity(myIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, "No application can handle this request."
                                    + " Please install a web browser", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });

                alertDialogBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(getLocation.KEY_REQUESTING_LOCATION_UPDATES)) {
                getLocation.mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        getLocation.KEY_REQUESTING_LOCATION_UPDATES);
            }
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(getLocation.KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                getLocation.mCurrentLocation = savedInstanceState.getParcelable(getLocation.KEY_LOCATION);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLocation.mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (getLocation.mGoogleApiClient.isConnected() && getLocation.mRequestingLocationUpdates) {
            getLocation.startLocationUpdates();
        }

        refreshListView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (getLocation.mGoogleApiClient.isConnected()) {
            getLocation.stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getLocation.mGoogleApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {

                    ShowDataDialog = true;
                    Place place = PlacePicker.getPlace(this,data);
                    getData = GetData.newInstance(place, mGeofenceFunctions);

                }
                break;
            case REQUEST_CHECK_SETTINGS:
                getLocation.activityResult(resultCode, data);
                break;
        }


    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if(ShowDataDialog) {
            ShowDataDialog = false;

            getData.show(getSupportFragmentManager(),"GetDataFragment");
        }
    }

    //Stores activity data in the Bundle.
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(getLocation.KEY_REQUESTING_LOCATION_UPDATES, getLocation.mRequestingLocationUpdates);
        savedInstanceState.putParcelable(getLocation.KEY_LOCATION, getLocation.mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }


    private void refreshListView() {
        list = new ArrayList<SingleRow>();
        dbCursor = mDatabaseHelper.getAllData();
        while(dbCursor.moveToNext()) {
            try {
                list.add(new SingleRow(dbCursor.getString(1),
                        mDatabaseHelper.getStatusFromDatabase(dbCursor.getString(0)),
                        dbCursor.getString(0),""+mDatabaseHelper.getTime(dbCursor.getString(0))+" :: "+mDatabaseHelper.getTaskFromDatabase(dbCursor.getString(0)) ) );
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        myListAdapter = new MyListAdapter(list,this,mGeofenceFunctions);
        listView.setAdapter(myListAdapter);
    }

}
