

package com.atrainingtracker.trainingtracker.fragments;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.atrainingtracker.banalservice.BANALService;
import com.atrainingtracker.trainingtracker.TrainingApplication;

public abstract class BaseTrackingFragment extends Fragment {

    public static final String TAG = ControlTrackingFragment.class.getName();
    private static final boolean DEBUG = TrainingApplication.DEBUG & false;


    protected BANALService.GetBanalServiceInterface mGetBanalServiceIf;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (DEBUG) Log.d(TAG, "onAttach");

        try {
            mGetBanalServiceIf = (BANALService.GetBanalServiceInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement GetBanalServiceInterface");
        }
    }

    protected void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        // also hide the action bar
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    protected void showSystemUI() {
        if (getActivity() == null) { return; }

        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    protected void forceDay() {
        if (getActivity() == null) { return; }
        ((AppCompatActivity) getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    protected void forceNight() {
        if (getActivity() == null) { return; }
        ((AppCompatActivity) getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    protected void followSystem() {
        if (getActivity() == null) { return; }
        ((AppCompatActivity) getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.getDefaultNightMode());
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode());
    }
}


