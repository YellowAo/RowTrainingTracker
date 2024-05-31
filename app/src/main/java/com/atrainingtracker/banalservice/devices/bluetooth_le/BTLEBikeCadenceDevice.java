

package com.atrainingtracker.banalservice.devices.bluetooth_le;

import android.content.Context;
import android.util.Log;

import com.atrainingtracker.banalservice.BANALService;
import com.atrainingtracker.banalservice.devices.DeviceType;
import com.atrainingtracker.banalservice.sensor.MySensorManager;

public class BTLEBikeCadenceDevice extends BTLEBikeDevice {
    private static final String TAG = "BTLEBikeCadenceDevice";
    private static final boolean DEBUG = BANALService.DEBUG & false;

    /**
     * constructor
     **/
    public BTLEBikeCadenceDevice(Context context, MySensorManager mySensorManager, long deviceID, String address) {
        super(context, mySensorManager, DeviceType.ROWING_CADENCE, deviceID, address);
        if (DEBUG) Log.i(TAG, "created device");
    }

    @Override
    protected void addSensors() {
        if (DEBUG) Log.i(TAG, "addSensors()");

        addCadenceSensor();
    }

}
