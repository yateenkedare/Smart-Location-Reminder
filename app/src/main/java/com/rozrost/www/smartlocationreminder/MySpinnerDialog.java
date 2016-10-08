package com.rozrost.www.smartlocationreminder;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Yateen Kedare on 4/15/2016.
 */
public class MySpinnerDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        ProgressDialog _dialog = new ProgressDialog(getActivity());
        this.setStyle(STYLE_NO_TITLE, getTheme()); // You can use styles or inflate a view
        _dialog.setMessage("Getting Current Location"); // set your messages if not inflated from XML
        _dialog.setCancelable(false);
        setCancelable(false);
        return _dialog;
    }
}