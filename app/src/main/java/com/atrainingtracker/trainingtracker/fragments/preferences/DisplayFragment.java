

package com.atrainingtracker.trainingtracker.fragments.preferences;

import android.os.Bundle;
import android.util.Log;

import com.atrainingtracker.R;
import com.atrainingtracker.trainingtracker.TrainingApplication;


public class DisplayFragment extends androidx.preference.PreferenceFragmentCompat {
    private static final boolean DEBUG = TrainingApplication.DEBUG;
    private static final String TAG = DisplayFragment.class.getName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (DEBUG) Log.i(TAG, "onCreatePreferences(savedInstanceState, rootKey=" + rootKey + ")");

        setPreferencesFromResource(R.xml.prefs, rootKey);
    }

    // that's all???
}
