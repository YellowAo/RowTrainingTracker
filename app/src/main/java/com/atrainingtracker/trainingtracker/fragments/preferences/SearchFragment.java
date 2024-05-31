

package com.atrainingtracker.trainingtracker.fragments.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.atrainingtracker.R;
import com.atrainingtracker.trainingtracker.TrainingApplication;

import java.util.LinkedList;
import java.util.List;


public class SearchFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final boolean DEBUG = TrainingApplication.DEBUG;
    private static final String TAG = SearchFragment.class.getName();

    private SharedPreferences mSharedPreferences;

    private EditTextPreference mNumberOfSearchTriesPref;
    private Preference mStartSearchPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (DEBUG) Log.i(TAG, "onCreatePreferences(savedInstanceState, rootKey=" + rootKey + ")");

        setPreferencesFromResource(R.xml.prefs, rootKey);

        mNumberOfSearchTriesPref = (EditTextPreference) getPreferenceScreen().findPreference(TrainingApplication.SP_NUMBER_OF_SEARCH_TRIES);
        mStartSearchPref = getPreferenceScreen().findPreference(TrainingApplication.PREF_KEY_START_SEARCH);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.i(TAG, "onResume()");

        setSearchNumberSummary();
        setStartSearchSummary();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void setSearchNumberSummary() {
        mNumberOfSearchTriesPref.setSummary(TrainingApplication.getNumberOfSearchTries() + "");
    }

    private void setStartSearchSummary() {

        List<String> list = new LinkedList<>();
        if (TrainingApplication.startSearchWhenAppStarts()) {
            list.add(getString(R.string.startSearchWhenAppStarts_short));
        }
        if (TrainingApplication.startSearchWhenTrackingStarts()) {
            list.add(getString(R.string.startSearchWhenTrackingStarts_short));
        }
        if (TrainingApplication.startSearchWhenResumeFromPaused()) {
            list.add(getString(R.string.startSearchWhenResumeFromPaused_short));
        }
        if (TrainingApplication.startSearchWhenNewLap()) {
            list.add(getString(R.string.startSearchWhenNewLap_short));
        }
        if (TrainingApplication.startSearchWhenUserChangesSport()) {
            list.add(getString(R.string.startSearchWhenUserChangesSport_short));
        }

        mStartSearchPref.setSummary(listToString(list));
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (DEBUG) Log.i(TAG, "onSharedPreferenceChanged: key=" + key);

        if (TrainingApplication.SP_NUMBER_OF_SEARCH_TRIES.equals(key)) {
            setSearchNumberSummary();
        }

    }

    private String listToString(List<String> listOfString) {
        int size = listOfString.size();
        if (size == 0) {
            return getString(R.string.startSearchOnlyManually);
        } else if (size == 1) {
            return listOfString.get(0);
        } else if (size == 2) {
            return getString(R.string.concatenate_2_format_or, listOfString.get(0), listOfString.get(1));
        } else {
            String lastOne = listOfString.get(size - 1);
            listOfString.remove(size - 1);

            StringBuilder result = new StringBuilder();
            for (String string : listOfString) {
                result.append(string);
                result.append(", ");
            }
            String firstPart = result.substring(0, result.length() - 2);

            return getString(R.string.concatenate_last_format_or, firstPart, lastOne);
        }
    }
}
