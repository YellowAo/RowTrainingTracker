

package com.atrainingtracker.trainingtracker.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.atrainingtracker.R;
import com.atrainingtracker.trainingtracker.TrainingApplication;



public class GPSDisabledDialog extends DialogFragment {
    public static final String TAG = GPSDisabledDialog.class.getName();
    private static final boolean DEBUG = TrainingApplication.DEBUG && false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setMessage(R.string.gps_disabled_on_device)
                .setCancelable(false)
                .setPositiveButton(R.string.gps_goto_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        return alertDialogBuilder.create();
    }
}
