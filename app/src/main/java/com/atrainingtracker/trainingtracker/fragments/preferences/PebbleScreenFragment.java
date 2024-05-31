

package com.atrainingtracker.trainingtracker.fragments.preferences;

import androidx.appcompat.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.atrainingtracker.R;
import com.atrainingtracker.trainingtracker.TrainingApplication;
import com.atrainingtracker.trainingtracker.smartwatch.pebble.Watchapp;


public class PebbleScreenFragment extends androidx.preference.PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final boolean DEBUG = TrainingApplication.DEBUG;
    private static final String TAG = PebbleScreenFragment.class.getName();

    private SharedPreferences mSharedPreferences;

    private ListPreference mPebbleWatchappPref;
    private Preference mConfigurePebbleDisplays;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (DEBUG) Log.i(TAG, "onCreatePreferences(savedInstanceState, rootKey=" + rootKey + ")");

        setPreferencesFromResource(R.xml.prefs, rootKey);

        mPebbleWatchappPref = (ListPreference) getPreferenceScreen().findPreference(TrainingApplication.SP_PEBBLE_WATCHAPP);
        mConfigurePebbleDisplays = getPreferenceScreen().findPreference(TrainingApplication.SP_CONFIGURE_PEBBLE_DISPLAY);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.i(TAG, "onResume()");

        mPebbleWatchappPref.setSummary(TrainingApplication.getPebbleWatchapp().getUiId());


        mPebbleWatchappPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (DEBUG)
                    Log.d(TAG, "onPreferenceChange: key=" + preference.getKey() + ", newValue=" + newValue);

                if (Watchapp.TRAINING_TRACKER.name().equals(newValue)
                        && TrainingApplication.showPebbleInstallDialog()) {
                    showInstallPebbleWatchappDialog();
                }

                return true;
            }
        });

        if (TrainingApplication.pebbleSupport() && TrainingApplication.getPebbleWatchapp() == Watchapp.TRAINING_TRACKER) {
            mConfigurePebbleDisplays.setEnabled(true);
        } else {
            mConfigurePebbleDisplays.setEnabled(false);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TrainingApplication.SP_PEBBLE_WATCHAPP.equals(key)) {
            mPebbleWatchappPref.setSummary(TrainingApplication.getPebbleWatchapp().getUiId());
            if (TrainingApplication.getPebbleWatchapp() == Watchapp.TRAINING_TRACKER) {
                mConfigurePebbleDisplays.setEnabled(true);
            } else {
                mConfigurePebbleDisplays.setEnabled(false);
            }
        }

        if (TrainingApplication.SP_PEBBLE_SUPPORT.equals(key)) {
            if (TrainingApplication.pebbleSupport() && TrainingApplication.getPebbleWatchapp() == Watchapp.TRAINING_TRACKER) {
                mConfigurePebbleDisplays.setEnabled(true);
            } else {
                mConfigurePebbleDisplays.setEnabled(false);
            }
        }
    }


    // simple helper method
    protected void showInstallPebbleWatchappDialog() {
        if (DEBUG) Log.d(TAG, "showInstallPebbleWatchappDialog");

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        LayoutInflater adbInflater = LayoutInflater.from(getActivity());
        View eulaLayout = adbInflater.inflate(R.layout.checkbox_dialog, null);
        final CheckBox doNotShowAgain = eulaLayout.findViewById(R.id.cbSkip);
        adb.setView(eulaLayout);
        adb.setTitle(R.string.install_pebble_watchapp_title);
        adb.setMessage(R.string.install_pebble_watchapp_text);

        adb.setPositiveButton(R.string.install_pebble_watchapp_now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TrainingApplication.setShowPebbleInstallDialog(!doNotShowAgain.isChecked());

                try { // try to use the deep link, this should work when the pebble app is installed
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("pebble://appstore/537516d64385b82a3b00009b")));
                } catch (ActivityNotFoundException e) {  // if this fails, we open the website
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://apps.getpebble.com/applications/537516d64385b82a3b00009b")));
                }

                return;
            }
        });

        adb.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TrainingApplication.setShowPebbleInstallDialog(!doNotShowAgain.isChecked());

                return;
            }
        });

        adb.show();

    }
}
