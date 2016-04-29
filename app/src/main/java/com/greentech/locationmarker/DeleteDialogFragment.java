package com.greentech.locationmarker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
    Made by Cyril Mathew
    For Use with Green Tech App
*/

public class DeleteDialogFragment extends DialogFragment {
    EditActivity returnActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString("Title");
        returnActivity = (EditActivity) getActivity();
        final CharSequence description = getArguments().getString("Description");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(description)
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        returnActivity.deleteEntry();
                        Log.i("Info", "Deleted entry");
                    }
                })
                .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}