

package com.atrainingtracker.trainingtracker.activities;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.ActivityType;
import com.atrainingtracker.banalservice.BANALService;
import com.atrainingtracker.banalservice.devices.DeviceType;
import com.atrainingtracker.banalservice.Protocol;
import com.atrainingtracker.banalservice.database.DevicesDatabaseManager;
import com.atrainingtracker.banalservice.dialogs.InstallANTShitDialog;
import com.atrainingtracker.banalservice.filters.FilterData;
import com.atrainingtracker.banalservice.fragments.DeviceTypeChoiceFragment;
import com.atrainingtracker.banalservice.fragments.EditDeviceDialogFragment;
import com.atrainingtracker.banalservice.fragments.RemoteDevicesFragment;
import com.atrainingtracker.banalservice.fragments.RemoteDevicesFragmentTabbedContainer;
import com.atrainingtracker.banalservice.fragments.SportTypeListFragment;
import com.atrainingtracker.banalservice.helpers.BatteryStatusHelper;
import com.atrainingtracker.trainingtracker.exporter.ExportManager;
import com.atrainingtracker.trainingtracker.exporter.ExportWorkoutIntentService;
import com.atrainingtracker.trainingtracker.exporter.FileFormat;
import com.atrainingtracker.trainingtracker.TrainingApplication;
import com.atrainingtracker.trainingtracker.database.TrackingViewsDatabaseManager;
import com.atrainingtracker.trainingtracker.dialogs.DeleteOldWorkoutsDialog;
import com.atrainingtracker.trainingtracker.dialogs.EnableBluetoothDialog;
import com.atrainingtracker.trainingtracker.dialogs.GPSDisabledDialog;
import com.atrainingtracker.trainingtracker.dialogs.ReallyDeleteWorkoutDialog;
import com.atrainingtracker.trainingtracker.dialogs.StartOrResumeDialog;
import com.atrainingtracker.trainingtracker.fragments.ExportStatusDialogFragment;
import com.atrainingtracker.trainingtracker.fragments.StartAndTrackingFragmentTabbedContainer;
import com.atrainingtracker.trainingtracker.fragments.WorkoutSummariesWithMapListFragment;
import com.atrainingtracker.trainingtracker.fragments.mapFragments.MyLocationsFragment;
import com.atrainingtracker.trainingtracker.fragments.mapFragments.TrackOnMapTrackingFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.AltitudeCorrectionFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.CloudUploadFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.DisplayFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.EmailUploadFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.FancyWorkoutNameListFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.FileExportFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.LocationSourcesFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.PebbleScreenFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.RootPrefsFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.RunkeeperUploadFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.SearchFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.StartSearchFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.StravaUploadFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.TrainingpeaksUploadFragment;
import com.atrainingtracker.trainingtracker.helpers.DeleteWorkoutTask;
import com.atrainingtracker.trainingtracker.interfaces.ReallyDeleteDialogInterface;
import com.atrainingtracker.trainingtracker.interfaces.RemoteDevicesSettingsInterface;
import com.atrainingtracker.trainingtracker.interfaces.ShowWorkoutDetailsInterface;
import com.atrainingtracker.trainingtracker.interfaces.StartOrResumeInterface;
import com.atrainingtracker.trainingtracker.onlinecommunities.strava.StravaGetAccessTokenActivity;
import com.atrainingtracker.trainingtracker.segments.SegmentsDatabaseManager;
import com.atrainingtracker.trainingtracker.segments.StarredSegmentsListFragment;
import com.atrainingtracker.trainingtracker.segments.StarredSegmentsTabbedContainer;
import com.dropbox.core.android.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.LinkedList;
import java.util.List;

// import android.support.v7.app.AlertDialog;


public class MainActivityWithNavigation
        extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        DeviceTypeChoiceFragment.OnDeviceTypeSelectedListener,
        RemoteDevicesFragment.OnRemoteDeviceSelectedListener,
        RemoteDevicesSettingsInterface,
        ShowWorkoutDetailsInterface,
        BANALService.GetBanalServiceInterface,
        ReallyDeleteDialogInterface,
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        StartAndTrackingFragmentTabbedContainer.UpdateActivityTypeInterface,
        StarredSegmentsListFragment.StartSegmentDetailsActivityInterface,
        StartOrResumeInterface {
    public static final String SELECTED_FRAGMENT_ID = "SELECTED_FRAGMENT_ID";
    public static final String SELECTED_FRAGMENT = "SELECTED_FRAGMENT";
    private static final String TAG = "com.atrainingtracker.trainingtracker.MainActivityWithNavigation";
    private static final boolean DEBUG = TrainingApplication.DEBUG && false;
    private static final int DEFAULT_SELECTED_FRAGMENT_ID = R.id.drawer_start_tracking;
    // private static final int REQUEST_ENABLE_BLUETOOTH            = 1;
    private static final int REQUEST_INSTALL_GOOGLE_PLAY_SERVICE = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    // private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE       = 3;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_AND_WRITE_EXTERNAL_STORAGE = 4;
    private static final long WAITING_TIME_BEFORE_DISCONNECTING = 5 * 60 * 1000; // 5 min
    private static final int CRITICAL_BATTERY_LEVEL = 30;
    protected TrainingApplication mTrainingApplication;
    // remember which fragment should be shown
    protected int mSelectedFragmentId = DEFAULT_SELECTED_FRAGMENT_ID;
    // the views
    protected DrawerLayout mDrawerLayout;
    protected NavigationView mNavigationView;
    protected MenuItem mPreviousMenuItem;
    protected Fragment mFragment;
    protected Handler mHandler;  // necessary to wait some time before we disconnect from the BANALService when the app is paused.
    protected boolean mStartAndNotResume = true;        // start a new workout or continue with the previous one
    protected BANALService.BANALServiceComm mBanalServiceComm = null;
    LinkedList<ConnectionStatusListener> mConnectionStatusListeners = new LinkedList<>();
    /* Broadcast Receiver to adapt the title based on the tracking state */
    BroadcastReceiver mStartTrackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTitle(R.string.Tracking);
            mNavigationView.getMenu().findItem(R.id.drawer_start_tracking).setTitle(R.string.Tracking);
        }
    };

    // protected ActivityType mActivityType = ActivityType.GENERIC;  // no longer necessary since we have the getActivity() method
    // protected long mWorkoutID = -1;
    BroadcastReceiver mPauseTrackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTitle(R.string.Paused);
            mNavigationView.getMenu().findItem(R.id.drawer_start_tracking).setTitle(R.string.Pause);
        }
    };
    BroadcastReceiver mStopTrackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTitle(R.string.app_name);
            mNavigationView.getMenu().findItem(R.id.drawer_start_tracking).setTitle(R.string.Start);

            checkBatteryStatus();
        }
    };
    private IntentFilter mStartTrackingFilter;
    private boolean mAlreadyTriedToRequestDropboxToken = false;
    // class BANALConnection implements ServiceConnection
    private ServiceConnection mBanalConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) Log.i(TAG, "onServiceConnected");

            mBanalServiceComm = (BANALService.BANALServiceComm) service; // IBANALService.Stub.asInterface(service);

            // create all the filters
            for (FilterData filterData : TrackingViewsDatabaseManager.getAllFilterData()) {
                mBanalServiceComm.createFilter(filterData);
            }

            // inform listeners
            for (ConnectionStatusListener connectionStatusListener : mConnectionStatusListeners) {
                connectionStatusListener.connectedToBanalService();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) Log.i(TAG, "onServiceDisconnected");

            mBanalServiceComm = null;

            // inform listeners
            for (ConnectionStatusListener connectionStatusListener : mConnectionStatusListeners) {
                connectionStatusListener.disconnectedFromBanalService();
            }
        }
    };
    protected Runnable mDisconnectFromBANALServiceRunnable = new Runnable() {
        @Override
        public void run() {
            disconnectFromBANALService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        // some initialization
        mTrainingApplication = (TrainingApplication) getApplication();
        mHandler = new Handler();

        mStartTrackingFilter = new IntentFilter(TrainingApplication.REQUEST_START_TRACKING);
        mStartTrackingFilter.addAction(TrainingApplication.REQUEST_RESUME_FROM_PAUSED);

        // now, create the UI
        setContentView(R.layout.main_activity_with_navigation);

        Toolbar toolbar = findViewById(R.id.apps_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar supportAB = getSupportActionBar();
        // supportAB.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        supportAB.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.TrainingTracker, R.string.TrainingTracker);
        actionBarDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setItemIconTintList(null);  // avoid converting the icons to black and white or gray and white
        mNavigationView.setNavigationItemSelectedListener(this);

        if (!BANALService.isProtocolSupported(this, Protocol.BLUETOOTH_LE)) {
            MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.drawer_pairing_BTLE);
            menuItem.setEnabled(false);
            menuItem.setCheckable(false);
        }

        // getPermissions
        if ((!TrainingApplication.havePermission(Manifest.permission.ACCESS_FINE_LOCATION)
                || !TrainingApplication.havePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                && !TrainingApplication.havePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_AND_WRITE_EXTERNAL_STORAGE);
        }
        if (!TrainingApplication.havePermission(Manifest.permission.ACCESS_FINE_LOCATION)
                || !TrainingApplication.havePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (!TrainingApplication.havePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        // if (!TrainingApplication.havePermission(Manifest.permission.READ_PHONE_STATE)) {
        //     ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        // }

        // check ANT+ installation
        if (TrainingApplication.checkANTInstallation() && BANALService.isANTProperlyInstalled(this)) {
            showInstallANTShitDialog();
        }


        if (savedInstanceState != null) {
            mSelectedFragmentId = savedInstanceState.getInt(SELECTED_FRAGMENT_ID, DEFAULT_SELECTED_FRAGMENT_ID);
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, "mFragment");
        } else {
            if (getIntent().hasExtra(SELECTED_FRAGMENT)) {
                switch (SelectedFragment.valueOf(getIntent().getStringExtra(SELECTED_FRAGMENT))) {
                    case START_OR_TRACKING:
                        mSelectedFragmentId = R.id.drawer_start_tracking;
                        break;

                    case WORKOUT_LIST:
                        mSelectedFragmentId = R.id.drawer_workouts;
                        break;
                }
            }
            // now, create and show the main fragment
            onNavigationItemSelected(mNavigationView.getMenu().findItem(mSelectedFragmentId));
        }


        if (TrainingApplication.trackLocation()) {
            // check whether GPS is enabled
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser();
            }
        }

        // TODO: better place for this code?
        // PROBLEM: when play service is not installed, DeviceManager will start the unfiltered GPS.
        //          when then the play service is installed, the DeviceManager will not use the newly available filtered GPS stuff

        // check whether the google play service utils are installed
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this), this, REQUEST_INSTALL_GOOGLE_PLAY_SERVICE);
        if (dialog != null) {  // so there is a problem with the Google Play Service
            // since there is no 'no' and 'do not ask again' button, we show this only several times, see the corresponding function of TrainingApplication
            if (TrainingApplication.showInstallPlayServicesDialog()) {
                dialog.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume");

        if (mBanalServiceComm == null) {
            bindService(new Intent(this, BANALService.class), mBanalConnection, Context.BIND_AUTO_CREATE);
        }

        mHandler.removeCallbacks(mDisconnectFromBANALServiceRunnable);

        checkPreferences();

        getWindow().getDecorView().setKeepScreenOn(TrainingApplication.keepScreenOn());

        if (TrainingApplication.NoUnlocking()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        if (TrainingApplication.forcePortrait()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        // register the receivers
        registerReceiver(mStartTrackingReceiver, mStartTrackingFilter);
        registerReceiver(mPauseTrackingReceiver, new IntentFilter(TrainingApplication.REQUEST_PAUSE_TRACKING));
        registerReceiver(mStopTrackingReceiver, new IntentFilter(TrainingApplication.REQUEST_STOP_TRACKING));

        upgradeDropboxV2();
    }

    // method to verify the preferences
    // when we shall upload to a platform there must be a token.
    // TODO: inform user when the settings are not valid?
    protected void checkPreferences() {
        // BUT not Dropbox since this case is part of the Auth procedure...
        // if (TrainingApplication.uploadToDropbox() && TrainingApplication.getDropboxToken() == null) {
        //     TrainingApplication.setUploadToDropbox(false);
        // }

        if (TrainingApplication.uploadToStrava() && TrainingApplication.getStravaAccessToken() == null) {
            TrainingApplication.setUploadToStrava(false);
        }

        if (TrainingApplication.uploadToStrava() && TrainingApplication.getStravaTokenExpiresAt() == 0) {
            Log.i(TAG, "migrating to new Strava OAuth");
            // TrainingApplication.setStravaTokenExpiresAt(1); // avoid starting the StravaGetAccessToken Activity again and again...
            startActivityForResult(new Intent(this, StravaGetAccessTokenActivity.class), StravaUploadFragment.GET_STRAVA_ACCESS_TOKEN);
        }

        if (TrainingApplication.uploadToRunKeeper() && TrainingApplication.getRunkeeperToken() == null) {
            TrainingApplication.setUploadToRunkeeper(false);
        }

        if (TrainingApplication.uploadToTrainingPeaks() && TrainingApplication.getTrainingPeaksRefreshToken() == null) {
            TrainingApplication.setUploadToTrainingPeaks(false);
        }

    }

    protected void upgradeDropboxV2() {
        if (TrainingApplication.uploadToDropbox() && !TrainingApplication.hasDropboxToken()) {

            String accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                TrainingApplication.storeDropboxToken(accessToken);
            } else {
                if (mAlreadyTriedToRequestDropboxToken) {
                    TrainingApplication.deleteDropboxToken();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.title_request_dropbox_token)
                            .setIcon(R.drawable.dropbox_logo_blue)
                            .setMessage(R.string.message_request_dropbox_token)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mAlreadyTriedToRequestDropboxToken = true;
                                    Auth.startOAuth2Authentication(MainActivityWithNavigation.this, TrainingApplication.getDropboxAppKey());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    TrainingApplication.deleteDropboxToken();
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG)
            Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        switch (requestCode) {
            case REQUEST_INSTALL_GOOGLE_PLAY_SERVICE:
                if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
                    // TODO: now, google play service is available, inform DeviceManager to change the shit
                } else {
                    // TODO: failed to install google play service
                }
                break;


//            case REQUEST_ENABLE_BLUETOOTH:
//                // TODO: copied code from ControlTrackingFragment
//                BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
//
//                // TODO: some more tries, then write own dialog and enable Bluetooth via enableBluetoothRequest()
//                if (bluetoothAdapter.isEnabled() ) {
//                    if (DEBUG) Log.i(TAG, "Bluetooth is now enabled");
//                    startPairing(Protocol.BLUETOOTH_LE);
//                }
//                else {
//                    if (DEBUG) Log.i(TAG, "Bluetooth is NOT enabled");
//                }
//
//                break;

            default:  // maybe someone else (like fragments) might be able to handle this
                if (DEBUG) Log.i(TAG, "requestCode not handled");
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (DEBUG) Log.i(TAG, "onSaveInstanceState");

        //Save the fragment's instance
        if (mFragment != null && mFragment.isAdded()) {
            getSupportFragmentManager().putFragment(savedInstanceState, "mFragment", mFragment);
        }

        savedInstanceState.putInt(SELECTED_FRAGMENT_ID, mSelectedFragmentId);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause");

        try {
            unregisterReceiver(mStartTrackingReceiver);
        } catch (IllegalArgumentException e) {
        }
        try {
            unregisterReceiver(mPauseTrackingReceiver);
        } catch (IllegalArgumentException e) {
        }
        try {
            unregisterReceiver(mStopTrackingReceiver);
        } catch (IllegalArgumentException e) {
        }


        mHandler.postDelayed(mDisconnectFromBANALServiceRunnable, WAITING_TIME_BEFORE_DISCONNECTING);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy");

        disconnectFromBANALService();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (DEBUG) Log.i(TAG, "onNavigationItemSelected");

        if (menuItem == null) {
            return false;
        }

        mDrawerLayout.closeDrawers();

        // uncheck previous menuItem
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        mPreviousMenuItem = menuItem;
        menuItem.setChecked(true);

        // just for debugging
        // if (DEBUG) Toast.makeText(getApplicationContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();

        // save
        mSelectedFragmentId = menuItem.getItemId();

        mFragment = null;
        String tag = null;
        int titleId = R.string.app_name;

        switch (mSelectedFragmentId) {
            case R.id.drawer_start_tracking:
                mFragment = StartAndTrackingFragmentTabbedContainer.newInstance(getActivityType(), StartAndTrackingFragmentTabbedContainer.CONTROL_ITEM);
                tag = StartAndTrackingFragmentTabbedContainer.TAG;
                break;

//            case R.id.drawer_map:
//                mFragment = TrackOnMapTrackingFragment.newInstance();
//                tag = TrackOnMapTrackingFragment.TAG;
//                break;
//
//            case R.id.drawer_segments:
//                titleId = R.string.segments;
//                mFragment = new StarredSegmentsTabbedContainer();
//                tag = StarredSegmentsTabbedContainer.TAG;
//                break;

            case R.id.drawer_workouts:
                titleId = R.string.tab_workouts;
                mFragment = new WorkoutSummariesWithMapListFragment();
                tag = WorkoutSummariesWithMapListFragment.TAG;
                break;

            case R.id.drawer_pairing_ant:
                titleId = R.string.pairing_ANT;
                // fragment = DeviceTypeChoiceFragment.newInstance(Protocol.ANT_PLUS);
                // tag = DeviceTypeChoiceFragment.TAG;

                // Log.i(TAG, "PluginVersionString=" + AntPluginPcc.getInstalledPluginsVersionString(this));
                // Log.i(TAG, "MissingDependencyName=" + AntPluginPcc.getMissingDependencyName());
                // Log.i(TAG, "MissingDependencyPackageName=" + AntPluginPcc.getMissingDependencyPackageName());
                // Log.i(TAG, "PATH_ANTPLUS_PLUGIN_PKG=" + AntPluginPcc.PATH_ANTPLUS_PLUGINS_PKG);

                mFragment = RemoteDevicesFragmentTabbedContainer.newInstance(Protocol.ANT_PLUS);
                tag = DeviceTypeChoiceFragment.TAG;
                break;

            case R.id.drawer_pairing_BTLE:
                titleId = R.string.pairing_bluetooth;
                // fragment = DeviceTypeChoiceFragment.newInstance(Protocol.BLUETOOTH_LE);
                // tag = DeviceTypeChoiceFragment.TAG;
                mFragment = RemoteDevicesFragmentTabbedContainer.newInstance(Protocol.BLUETOOTH_LE);
                tag = DeviceTypeChoiceFragment.TAG;
                break;

            case R.id.drawer_my_sensors:
                titleId = R.string.myRemoteDevices;
                mFragment = RemoteDevicesFragmentTabbedContainer.newInstance(Protocol.ALL, DeviceType.ALL);
                tag = DeviceTypeChoiceFragment.TAG;
                break;

//            case R.id.drawer_my_locations:
//                titleId = R.string.my_locations;
//                mFragment = new MyLocationsFragment();
//                tag = MyLocationsFragment.TAG;
//                break;

            case R.id.drawer_settings:
                titleId = R.string.settings;
                mFragment = new RootPrefsFragment();
                tag = RootPrefsFragment.TAG;
                break;


            case R.id.drawer_privacy_policy:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.atrainingtracker.com/privacy.html"));
                startActivity(browserIntent);
                return true;


            default:
                Log.d(TAG, "setting a new content fragment not yet implemented");
                Toast.makeText(this, "setting a new content fragment not yet implemented", Toast.LENGTH_SHORT);
        }

        if (mFragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, mFragment, tag);
            // if (addToBackStack) { fragmentTransaction.addToBackStack(null); }
            fragmentTransaction.commit();
        }
        setTitle(titleId);

        return true;
    }

    @Override
    public void onBackPressed() {
        // TODO: optimize: when showing "deeper fragments", we only want to go back one step and not completely to the start_tracking fragment
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 0
                && mSelectedFragmentId != R.id.drawer_start_tracking) {
            onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.drawer_start_tracking));
        } else {
            super.onBackPressed();
        }
    }

    /* Called when an options item is clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) Log.i(TAG, "onOptionsItemSelected");

        // Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.itemDeleteOldWorkouts:  // TODO: move to somewhere else?  automatically??
                if (DEBUG) Log.i(TAG, "option itemDeleteOldWorkouts pressed");
                deleteOldWorkouts();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected ActivityType getActivityType() {
        if (mBanalServiceComm == null) {
            return ActivityType.getDefaultActivityType();
        } else {
            return mBanalServiceComm.getActivityType();
        }
    }

    @Override
    public void updateActivityType(int selectedItem) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, StartAndTrackingFragmentTabbedContainer.newInstance(getActivityType(), selectedItem));
        // if (addToBackStack) { fragmentTransaction.addToBackStack(null); }
        fragmentTransaction.commit();
    }

    @Override
    public void enableBluetoothRequest() {
        if (DEBUG) Log.i(TAG, "enableBluetoothRequest");

        showEnableBluetoothDialog();

//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);


    }

    @Override
    public void startPairing(Protocol protocol) {
        if (DEBUG) Log.d(TAG, "startPairingActivity: " + protocol);
        switch (protocol) {
            case ANT_PLUS:
                onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.drawer_pairing_ant));
                // changeContentFragment(R.id.drawer_pairing_ant);
                return;

            case BLUETOOTH_LE:
                onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.drawer_pairing_BTLE));
                // changeContentFragment(R.id.drawer_pairing_BTLE);
                return;
        }

        Toast.makeText(getApplicationContext(), "TODO: must implement the startPairing for" + protocol.name(), Toast.LENGTH_SHORT).show();
    }

    // @Override
    // public void startWorkoutDetailsActivity(long workoutId, WorkoutDetailsActivity.SelectedFragment selectedFragment)
    // {
    //     if (DEBUG) Log.i(TAG, "startWorkoutDetailsActivity(" + workoutId + ")");

    //     Bundle bundle = new Bundle();
    //     bundle.putLong(WorkoutSummaries.WORKOUT_ID, workoutId);
    //     bundle.putString(WorkoutDetailsActivity.SELECTED_FRAGMENT, selectedFragment.name());
    //     Intent workoutDetailsIntent = new Intent(this, WorkoutDetailsActivity.class);
    //     workoutDetailsIntent.putExtras(bundle);
    //     startActivity(workoutDetailsIntent);
    // }

    protected void checkBatteryStatus() {
        final List<DevicesDatabaseManager.NameAndBatteryPercentage> criticalBatteryDevices = DevicesDatabaseManager.getCriticalBatteryDevices(CRITICAL_BATTERY_LEVEL);
        if (criticalBatteryDevices.size() > 0) {

            final List<String> stringList = new LinkedList<>();
            for (DevicesDatabaseManager.NameAndBatteryPercentage device : criticalBatteryDevices) {
                stringList.add(getString(R.string.critical_battery_message_format,
                        device.name, getString(BatteryStatusHelper.getBatteryStatusNameId(device.batteryPercentage))));
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(criticalBatteryDevices.size() == 1 ? R.string.check_battery_status_title_1 : R.string.check_battery_status_title_many);
            builder.setItems(stringList.toArray(new String[stringList.size()]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditDeviceDialogFragment editDeviceDialogFragment = EditDeviceDialogFragment.newInstance(criticalBatteryDevices.get(which).deviceId);
                    editDeviceDialogFragment.show(getSupportFragmentManager(), EditDeviceDialogFragment.TAG);
                }
            });
            builder.create().show();
        }
    }

    @Override
    public void startSegmentDetailsActivity(int segmentId, SegmentDetailsActivity.SelectedFragment selectedFragment) {
        if (DEBUG) Log.i(TAG, "startSegmentDetailsActivity: segmentId=" + segmentId);

        Bundle bundle = new Bundle();
        bundle.putLong(SegmentsDatabaseManager.Segments.SEGMENT_ID, segmentId);
        bundle.putString(WorkoutDetailsActivity.SELECTED_FRAGMENT, selectedFragment.name());
        Intent segmentDetailsIntent = new Intent(this, SegmentDetailsActivity.class);
        segmentDetailsIntent.putExtras(bundle);
        startActivity(segmentDetailsIntent);
    }

    @Override
    public void exportWorkout(long id, FileFormat fileFormat) {
        if (DEBUG) Log.i(TAG, "exportWorkout");

        ExportManager exportManager = new ExportManager(this, TAG);
        exportManager.exportWorkoutTo(id, fileFormat);
        exportManager.onFinished(TAG);

        startService(new Intent(this, ExportWorkoutIntentService.class));
    }

    @Override
    public void showExportStatusDialog(long workoutId) {
        if (DEBUG) Log.i(TAG, "startExportDetailsActivity");

        ExportStatusDialogFragment exportStatusFragment = ExportStatusDialogFragment.newInstance(workoutId);
        exportStatusFragment.show(getSupportFragmentManager(), ExportStatusDialogFragment.TAG);
    }

    @Override
    public void confirmDeleteWorkout(long workoutId) {
        ReallyDeleteWorkoutDialog newFragment = ReallyDeleteWorkoutDialog.newInstance(workoutId);
        newFragment.show(getSupportFragmentManager(), ReallyDeleteWorkoutDialog.TAG);
    }

    @Override
    public void reallyDeleteWorkout(long workoutId) {
        (new DeleteWorkoutTask(this)).execute(workoutId);
    }

    public void deleteOldWorkouts() {
        if (DEBUG) Log.i(TAG, "deleteOldWorkouts");

        DeleteOldWorkoutsDialog deleteOldWorkoutsDialog = new DeleteOldWorkoutsDialog();
        deleteOldWorkoutsDialog.show(getSupportFragmentManager(), DeleteOldWorkoutsDialog.TAG);
    }

    @Override
    public void onDeviceTypeSelected(DeviceType deviceType, Protocol protocol) {
        if (DEBUG)
            Log.i(TAG, "onDeviceTypeSelected(" + deviceType.name() + "), mProtocol=" + protocol);

        RemoteDevicesFragmentTabbedContainer fragment = RemoteDevicesFragmentTabbedContainer.newInstance(protocol, deviceType);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, RemoteDevicesFragmentTabbedContainer.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onRemoteDeviceSelected(long deviceId) {
        EditDeviceDialogFragment editDeviceDialogFragment = EditDeviceDialogFragment.newInstance(deviceId);
        editDeviceDialogFragment.show(getSupportFragmentManager(), EditDeviceDialogFragment.TAG);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // the connection to the BANALService
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // stolen from http://stackoverflow.com/questions/32487206/inner-preferencescreen-not-opens-with-preferencefragmentcompat
    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        if (DEBUG) Log.i(TAG, "onPreferenceStartScreen: " + preferenceScreen.getKey());
        String key = preferenceScreen.getKey();
        PreferenceFragmentCompat fragment = null;
        if (key.equals("root")) {
            fragment = new RootPrefsFragment();
        } else if (key.equals("display")) {
            fragment = new DisplayFragment();
        }
        // else if (key.equals("smoothing")) {
        //     fragment = new SmoothingFragment();
        // }
        else if (key.equals("search_settings")) {
            fragment = new SearchFragment();
        } else if (key.equals(TrainingApplication.PREF_KEY_START_SEARCH)) {
            fragment = new StartSearchFragment();
        } else if (key.equals("fileExport")) {
            fragment = new FileExportFragment();
        } else if (key.equals("cloudUpload")) {
            fragment = new CloudUploadFragment();
        } else if (key.equals(TrainingApplication.PREFERENCE_SCREEN_EMAIL_UPLOAD)) {
            fragment = new EmailUploadFragment();
        } else if (key.equals(TrainingApplication.PREFERENCE_SCREEN_STRAVA)) {
            fragment = new StravaUploadFragment();
        } else if (key.equals(TrainingApplication.PREFERENCE_SCREEN_RUNKEEPER)) {
            fragment = new RunkeeperUploadFragment();
        } else if (key.equals(TrainingApplication.PREFERENCE_SCREEN_TRAINING_PEAKS)) {
            fragment = new TrainingpeaksUploadFragment();
        } else if (key.equals("pebbleScreen")) {
            fragment = new PebbleScreenFragment();
        } else if (key.equals("prefsLocationSources")) {
            fragment = new LocationSourcesFragment();
        } else if (key.equals("altitudeCorrection")) {
            fragment = new AltitudeCorrectionFragment();
        } else if (key.equals("sportTypes")) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, new SportTypeListFragment(), preferenceScreen.getKey());
            ft.addToBackStack(preferenceScreen.getKey());
            ft.commit();
            return true;
        } else if (key.equals("fancyWorkoutNames")) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, new FancyWorkoutNameListFragment(), preferenceScreen.getKey());
            ft.addToBackStack(preferenceScreen.getKey());
            ft.commit();
            return true;
        } else {
            Log.d(TAG, "WTF: unknown key");
        }


        if (fragment != null) {
            Bundle args = new Bundle();
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
            fragment.setArguments(args);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, fragment, preferenceScreen.getKey());
            ft.addToBackStack(preferenceScreen.getKey());
            ft.commit();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void registerConnectionStatusListener(ConnectionStatusListener connectionStatusListener) {
        mConnectionStatusListeners.add(connectionStatusListener);
    }

    @Override
    public BANALService.BANALServiceComm getBanalServiceComm() {
        return mBanalServiceComm;
    }

    private void disconnectFromBANALService() {
        if (DEBUG) Log.i(TAG, "disconnectFromBANALService");

        if (mBanalServiceComm != null) {
            unbindService(mBanalConnection);                                                        // TODO: on some devices, an exception is thrown here
            mBanalServiceComm = null;
        }
    }

    private void showGPSDisabledAlertToUser() {
        GPSDisabledDialog gpsDisabledDialog = new GPSDisabledDialog();
        gpsDisabledDialog.show(getSupportFragmentManager(), GPSDisabledDialog.TAG);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // showing several dialogs
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showEnableBluetoothDialog() {
        EnableBluetoothDialog enableBluetoothDialog = new EnableBluetoothDialog();
        enableBluetoothDialog.show(getSupportFragmentManager(), EnableBluetoothDialog.TAG);
    }

    private void showInstallANTShitDialog() {
        InstallANTShitDialog installANTShitDialog = new InstallANTShitDialog();
        installANTShitDialog.show(getSupportFragmentManager(), InstallANTShitDialog.TAG);
    }

    /***********************************************************************************************/

    @Override
    public void showStartOrResumeDialog() {
        StartOrResumeDialog startOrResumeDialog = new StartOrResumeDialog();
        startOrResumeDialog.show(getSupportFragmentManager(), StartOrResumeDialog.TAG);
    }


    /***********************************************************************************************/
    /* Implementation of the StartOrResumeInterface                                                */
    @Override
    public void chooseStart() {
        TrainingApplication.setResumeFromCrash(false);

        TextView tv = findViewById(R.id.tvStart);
        if (tv != null) {
            tv.setText(R.string.start_new_workout);
        }
    }

    @Override
    public void chooseResume() {
        TrainingApplication.setResumeFromCrash(true);

        TextView tv = findViewById(R.id.tvStart);
        if (tv != null) {
            tv.setText(R.string.resume_workout);
        }
    }

    public enum SelectedFragment {START_OR_TRACKING, WORKOUT_LIST}

}
