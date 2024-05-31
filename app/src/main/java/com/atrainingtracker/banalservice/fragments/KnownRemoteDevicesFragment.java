

package com.atrainingtracker.banalservice.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.BANALService;
import com.atrainingtracker.banalservice.devices.DeviceType;
import com.atrainingtracker.banalservice.Protocol;
import com.atrainingtracker.banalservice.database.DevicesDatabaseManager.DevicesDbHelper;


public class KnownRemoteDevicesFragment extends RemoteDevicesFragment {
    public static final String TAG = "KnownRemoteDevicesFragment";
    private static final boolean DEBUG = BANALService.DEBUG && true;

    public static KnownRemoteDevicesFragment newInstance(Protocol protocol, DeviceType deviceType) {
        if (DEBUG) Log.i(TAG, "newInstance");

        KnownRemoteDevicesFragment knownRemoteDevicesFragment = new KnownRemoteDevicesFragment();

        Bundle args = new Bundle();
        args.putString(BANALService.PROTOCOL, protocol.name());
        args.putString(BANALService.DEVICE_TYPE, deviceType.name());
        knownRemoteDevicesFragment.setArguments(args);

        return knownRemoteDevicesFragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.device_list;
    }

    @Override
    protected Cursor getCursor() {
        if (DEBUG) Log.i(TAG, "getCursor()");

        Cursor cursor = null;

        if (mDeviceType == null) {
            // get an empty cursor
            cursor = mRemoteDevicesDb.query(DevicesDbHelper.DEVICES,
                    DeviceListCursorAdapter.COLUMNS,
                    DevicesDbHelper.C_ID + "=?",
                    new String[]{Long.toString(-1)},  // there must be no device with this device type byte
                    null,
                    null,
                    null);
        } else if (mDeviceType == DeviceType.ALL) {
            if (mProtocol == Protocol.ALL) {
                cursor = mRemoteDevicesDb.query(DevicesDbHelper.DEVICES,
                        DeviceListCursorAdapter.COLUMNS,
                        null,
                        null,
                        null,
                        null,
                        null);
            } else {
                cursor = mRemoteDevicesDb.query(DevicesDbHelper.DEVICES,
                        DeviceListCursorAdapter.COLUMNS,
                        DevicesDbHelper.PROTOCOL + "=?",
                        new String[]{mProtocol.name()},
                        null,
                        null,
                        null);
            }
        } else {
            cursor = mRemoteDevicesDb.query(DevicesDbHelper.DEVICES,
                    DeviceListCursorAdapter.COLUMNS,
                    DevicesDbHelper.DEVICE_TYPE + "=? AND " + DevicesDbHelper.PROTOCOL + "=?",
                    new String[]{mDeviceType.name(), mProtocol.name()},
                    null,
                    null,
                    null);
        }

        return cursor;
    }

    // onAttach
    // onCreate
    // onCreateView
    // onActivityCreated
    // onStart
    // onResume

    // onPause

    // onDestroy


}
