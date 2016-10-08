package com.rozrost.www.smartlocationreminder;


import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yateen Kedare on 4/19/2016.
 */
public class GetData extends DialogFragment {
    private static Place place;
    EditText etName, etRadius,sHour,sMin,eHour,eMin,eTask;
    int radius,startHr,startMin,endHr,endMin;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private static GeofenceFunctions mGeofenceFunctions;

    public static GetData newInstance(Place p, GeofenceFunctions GFF) {

        GetData getData = new GetData();
        place = p;
        mGeofenceFunctions = GFF;
        return getData;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        setCancelable(false);
        View view = inflater.inflate(R.layout.input_fragment, null);
        etName = (EditText)view.findViewById(R.id.etName);
        etRadius = (EditText)view.findViewById(R.id.etDistance);
        etName.setText(place.getName());
        eTask = (EditText)view.findViewById(R.id.etTask);
        sHour = (EditText)view.findViewById(R.id.sHr);
        eHour = (EditText)view.findViewById(R.id.eHr);
        eMin = (EditText)view.findViewById(R.id.eMin);
        sMin = (EditText)view.findViewById(R.id.sMin);

        builder.setView(view);
        builder.setTitle("Add Reminder");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!etName.getText().toString().matches("") &&
                        !etRadius.getText().toString().matches("") &&
                        !sHour.getText().toString().matches("") &&
                        !eHour.getText().toString().matches("") &&
                        !sMin.getText().toString().matches("") &&
                        !eMin.getText().toString().matches("") &&
                        !eTask.getText().toString().matches("")) {

                    startHr = Integer.parseInt(sHour.getText().toString());
                    startMin = Integer.parseInt(sMin.getText().toString());
                    endHr = Integer.parseInt(eHour.getText().toString());
                    endMin = Integer.parseInt(eMin.getText().toString());
                    radius = Integer.parseInt(etRadius.getText().toString());
                        mGeofenceFunctions.createAndAddGeofence(etName.getText().toString(),
                                place.getLatLng().latitude,
                                place.getLatLng().longitude,
                                radius, startHr,startMin,endHr,endMin,
                                eTask.getText().toString() );
                } else {
                    Toast.makeText(getActivity(), "Please Enter all values and try again", Toast.LENGTH_LONG).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_LONG).show();
            }
        });

        return builder.create();
    }

}
