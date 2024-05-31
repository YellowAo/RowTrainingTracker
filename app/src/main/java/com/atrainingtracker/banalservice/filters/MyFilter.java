

package com.atrainingtracker.banalservice.filters;

import com.atrainingtracker.banalservice.sensor.MySensor;
import com.atrainingtracker.banalservice.sensor.SensorType;

public abstract class MyFilter<T>
        implements MySensor.SensorListener<T> {
    private static final String TAG = MyFilter.class.getCanonicalName();
    protected String mDeviceName;
    protected SensorType mSensorType;

    public MyFilter(String deviceName, SensorType sensorType) {
        mDeviceName = deviceName;
        mSensorType = sensorType;
    }

    abstract T getFilteredValue();

    abstract FilterType getFilterType();

    abstract double getFilterConstant();

    public FilteredSensorData getFilteredSensorData() {
        // Log.i(TAG, "getFilteredSensorData(): " + getFilterType() + " " + mDeviceName + " " + mSensorType + ": " + getFilteredValue());

        T filteredValue = getFilteredValue();
        return new FilteredSensorData(mSensorType, filteredValue, mSensorType.getMyFormatter().format(filteredValue), mDeviceName, getFilterType(), getFilterConstant());
    }
}
