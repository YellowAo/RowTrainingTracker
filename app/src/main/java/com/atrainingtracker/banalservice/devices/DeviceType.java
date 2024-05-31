

package com.atrainingtracker.banalservice.devices;

import com.atrainingtracker.banalservice.BANALService;
import com.atrainingtracker.banalservice.BSportType;
import com.atrainingtracker.banalservice.Protocol;
import com.atrainingtracker.banalservice.sensor.SensorType;
import com.atrainingtracker.trainingtracker.TrainingApplication;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public enum DeviceType {
    DUMMY(SensorType.TIME_TOTAL, BSportType.UNKNOWN, 1),
    ALL(SensorType.TIME_TOTAL, BSportType.UNKNOWN, 1),

    HRM(SensorType.HR, BSportType.UNKNOWN, 1),
    ROWING_SPEED(SensorType.SPEED_mps, BSportType.ROWING, BANALService.DEFAULT_ROWING_CALIBRATION_FACTOR),
    ROWING_CADENCE(SensorType.CADENCE, BSportType.ROWING, 1),
    ROWING_SPEED_AND_CADENCE(SensorType.SPEED_mps, BSportType.ROWING, BANALService.DEFAULT_ROWING_CALIBRATION_FACTOR),
    ROWING_POWER(SensorType.POWER, BSportType.ROWING, BANALService.DEFAULT_ROWING_CALIBRATION_FACTOR),
    //RUN_SPEED(SensorType.SPEED_mps, BSportType.RUN, 1),
    ENVIRONMENT(SensorType.TEMPERATURE, BSportType.UNKNOWN, 1),

    SPEED_AND_LOCATION_GPS(SensorType.ACCURACY, BSportType.UNKNOWN, 1),
    SPEED_AND_LOCATION_NETWORK(SensorType.ACCURACY, BSportType.UNKNOWN, 1),
    SPEED_AND_LOCATION_GOOGLE_FUSED(SensorType.ACCURACY, BSportType.UNKNOWN, 1),

    ALTITUDE_FROM_PRESSURE(SensorType.ALTITUDE, BSportType.UNKNOWN, 1),


    SENSOR_MANAGER(SensorType.SENSORS, BSportType.UNKNOWN, 1),
    CLOCK(SensorType.TIME_TOTAL, BSportType.UNKNOWN, 1);   // only last one has ';' rest has ','!

    private final SensorType mainSensorType;
    private final BSportType sportType;
    private final double defaultCalibrationFactor;

    DeviceType(SensorType mainSensorType, BSportType sportType, double defaultCalibrationFactor) {
        this.mainSensorType = mainSensorType;
        this.sportType = sportType;
        this.defaultCalibrationFactor = defaultCalibrationFactor;
    }

    public static final DeviceType[] getRemoteDeviceTypes(Protocol protocol) {
        switch (protocol) {
            case ANT_PLUS:
                return new DeviceType[]{HRM, ROWING_SPEED, ROWING_CADENCE, ROWING_SPEED_AND_CADENCE, ROWING_POWER, ENVIRONMENT}; // add ALL?
            case BLUETOOTH_LE:
                return new DeviceType[]{HRM, ROWING_SPEED, ROWING_CADENCE, ROWING_SPEED_AND_CADENCE, ROWING_POWER};
            default:
                return new DeviceType[]{HRM, ROWING_SPEED, ROWING_CADENCE, ROWING_SPEED_AND_CADENCE, ROWING_POWER};
        }
    }

    public static com.dsi.ant.plugins.antplus.pcc.defines.DeviceType getAntPluginDeviceType(DeviceType deviceType) {
        switch (deviceType) {
            case HRM:
                return com.dsi.ant.plugins.antplus.pcc.defines.DeviceType.HEARTRATE;
            case ROWING_SPEED:
                return com.dsi.ant.plugins.antplus.pcc.defines.DeviceType.BIKE_SPD;
            case ROWING_CADENCE:
                return com.dsi.ant.plugins.antplus.pcc.defines.DeviceType.BIKE_CADENCE;
            case ROWING_SPEED_AND_CADENCE:
                return com.dsi.ant.plugins.antplus.pcc.defines.DeviceType.BIKE_SPDCAD;
            case ROWING_POWER:
                return com.dsi.ant.plugins.antplus.pcc.defines.DeviceType.BIKE_POWER;
            case ENVIRONMENT:
                return com.dsi.ant.plugins.antplus.pcc.defines.DeviceType.ENVIRONMENT;
            default:
                return null;
        }
    }

    public static DeviceType getDeviceType(com.dsi.ant.plugins.antplus.pcc.defines.DeviceType deviceType) {
        switch (deviceType) {
            case HEARTRATE:
                return HRM;
            case BIKE_SPD:
                return ROWING_SPEED;
            case BIKE_CADENCE:
                return ROWING_CADENCE;
            case BIKE_SPDCAD:
                return ROWING_SPEED_AND_CADENCE;
            case BIKE_POWER:
                return ROWING_POWER;
            case ENVIRONMENT:
                return ENVIRONMENT;
            default:
                return null;
        }
    }

    public static Set<DeviceType> getDeviceTypeList(SensorType sensorType) {
        Set<DeviceType> deviceTypes = new HashSet<>();

        switch (sensorType) {
            // no real sensor attached => not selectable
            case ACCUMULATED_SENSORS:
            case LAP_NR:
            case SENSORS:
            case TIME_ACTIVE:
            case TIME_LAP:
            case TIME_OF_DAY:
            case TIME_TOTAL:
                return null;

            // all location sources
            case ACCURACY:
            case BEARING:
            case LINE_DISTANCE_m:
            case LATITUDE:
            case LONGITUDE:
                deviceTypes.addAll(getEnabledLocationDevices());
                break;

            // either from pressure of from location
            case ALTITUDE:
                deviceTypes.add(DeviceType.ALTITUDE_FROM_PRESSURE);
                deviceTypes.addAll(getEnabledLocationDevices());
                break;

            case CADENCE:
                deviceTypes.add(DeviceType.ROWING_CADENCE);
                deviceTypes.add(DeviceType.ROWING_SPEED_AND_CADENCE);
                deviceTypes.add(DeviceType.ROWING_POWER);      // but not all
                break;



            // all devices that support speed / distance...
            case DISTANCE_m:
            case DISTANCE_m_LAP:
            case PACE_spm:
            case SPEED_mps:
                deviceTypes.add(DeviceType.ROWING_SPEED);
                deviceTypes.add(DeviceType.ROWING_SPEED_AND_CADENCE);
                deviceTypes.add(DeviceType.ROWING_POWER);      // but not all
                deviceTypes.addAll(getEnabledLocationDevices());
                break;

            // only HR
            case HR:
                deviceTypes.add(DeviceType.HRM);
                break;

            // only in Power
            case PEDAL_POWER_BALANCE:
            case PEDAL_SMOOTHNESS_L:
            case PEDAL_SMOOTHNESS_R:
            case PEDAL_SMOOTHNESS:
            case POWER:
            case TORQUE:
            case TORQUE_EFFECTIVENESS_L:
            case TORQUE_EFFECTIVENESS_R:
                deviceTypes.add(DeviceType.ROWING_POWER);     // but not all
                break;

            // only in environment (temperature) device
            case TEMPERATURE:
            case TEMPERATURE_MAX:
            case TEMPERATURE_MIN:
                deviceTypes.add(DeviceType.ENVIRONMENT);
                break;

        }

        return deviceTypes;
    }

    protected static List<DeviceType> getEnabledLocationDevices() {
        LinkedList<DeviceType> enabledLocationDevices = new LinkedList<>();
        if (TrainingApplication.useLocationSourceGPS()) {
            enabledLocationDevices.add(DeviceType.SPEED_AND_LOCATION_GPS);
        }
        if (TrainingApplication.useLocationSourceNetwork()) {
            enabledLocationDevices.add(DeviceType.SPEED_AND_LOCATION_NETWORK);
        }
        if (TrainingApplication.useLocationSourceGoogleFused()) {
            enabledLocationDevices.add(DeviceType.SPEED_AND_LOCATION_GOOGLE_FUSED);
        }

        return enabledLocationDevices;
    }

    public SensorType getMainSensorType() {
        return mainSensorType;
    }

    public BSportType getSportType() {
        return sportType;
    }

    public double getDefaultCalibrationFactor() {
        return defaultCalibrationFactor;
    }
}
