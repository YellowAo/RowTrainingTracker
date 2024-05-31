

package com.atrainingtracker.banalservice.helpers;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.devices.DeviceType;
import com.atrainingtracker.banalservice.Protocol;

public class UIHelper {
    public static int getIconId(DeviceType deviceType, Protocol protocol) {
        // TODO: must also depend on the protocol
        // TODO: remove this functionality from DeviceType!
        switch (protocol) {
            case ANT_PLUS:
                switch (deviceType) {
                    case HRM:
                        return R.drawable.hr;
                    case ROWING_SPEED:
                        return R.drawable.row_spd;
                    case ROWING_CADENCE:
                        return R.drawable.row_cad;
                    case ROWING_SPEED_AND_CADENCE:
                        return R.drawable.row_speed_and_cadence;
                    case ROWING_POWER:
                        return R.drawable.row_pwr;
                    case ENVIRONMENT:
                        return R.drawable.temp;
                }
            case BLUETOOTH_LE:
                switch (deviceType) {
                    case HRM:
                        return R.drawable.bt_hr;
                    case ROWING_SPEED:
                        return R.drawable.bt_row_spd;
                    case ROWING_CADENCE:
                        return R.drawable.bt_row_cad;
                    case ROWING_SPEED_AND_CADENCE:
                        return R.drawable.bt_row_speed_and_cadence;
                    case ROWING_POWER:
                        return R.drawable.bt_row_pwr;

                }
        }
        return protocol.getIconId();
        // return getIconId(protocol);			 // TODO: other (custom?) icon
    }

//	public static int getLargeIconId(DeviceType deviceType, Protocol protocol) 
//	{
//		// TODO: must also depend on the protocol
//		// TODO: remove this functionality from DeviceType!
//		switch (deviceType) {
//		case HRM:
//			return R.drawable.hr_large;
//		case ROWING_SPEED:
//			return R.drawable.row_spd_large;
//		case ROWING_CADENCE:
//			return R.drawable.row_cad_large;
//		case ROWING_SPEED_AND_CADENCE:
//			return R.drawable.row_speed_and_cadence_large;
//		case ROWING_POWER:
//			return R.drawable.row_pwr_large;
//		case RUN_SPEED:
//			return R.drawable.run_spd_large;
//		case ENVIRONMENT:
//			return R.drawable.temp_large;
//		default:
//			return R.drawable.ant_logo_large;	// TODO: other (custom?) icon
//		}
//	}

    public static int getNameId(DeviceType deviceType) {
        switch (deviceType) {
            case ALL:
                return R.string.device_type_all;
            case ALTITUDE_FROM_PRESSURE:
                return R.string.device_type_altitude_from_pressure;
            case ROWING_CADENCE:
                return R.string.device_type_row_cadence;
            case ROWING_POWER:
                return R.string.device_type_row_power;
            case ROWING_SPEED:
                return R.string.device_type_row_speed;
            case ROWING_SPEED_AND_CADENCE:
                return R.string.device_type_row_speed_and_cadence;
            case CLOCK:
                return R.string.device_type_clock;
            case DUMMY:
                return R.string.device_type_dummy;
            case ENVIRONMENT:
                return R.string.device_type_environment;
            case SPEED_AND_LOCATION_GPS:
            case SPEED_AND_LOCATION_NETWORK:
            case SPEED_AND_LOCATION_GOOGLE_FUSED:
                return R.string.device_type_gps_speed_and_location;
            case HRM:
                return R.string.device_type_heart_rate;
            case SENSOR_MANAGER:
                return R.string.device_type_sensor_manager;
        }
        return 0;
    }

    public static int getShortNameId(DeviceType deviceType) {
        switch (deviceType) {
            case ALL:
                return R.string.device_type_short_all;
            case ALTITUDE_FROM_PRESSURE:
                return R.string.device_type_short_altitude_from_pressure;
            case ROWING_CADENCE:
                return R.string.device_type_short_row_cadence;
            case ROWING_POWER:
                return R.string.device_type_short_row_power;
            case ROWING_SPEED:
                return R.string.device_type_short_row_speed;
            case ROWING_SPEED_AND_CADENCE:
                return R.string.device_type_short_row_speed_and_cadence;
            case CLOCK:
                return R.string.device_type_short_clock;
            case DUMMY:
                return R.string.device_type_short_dummy;
            case ENVIRONMENT:
                return R.string.device_type_short_environment;
            case SPEED_AND_LOCATION_GPS:
            case SPEED_AND_LOCATION_NETWORK:
            case SPEED_AND_LOCATION_GOOGLE_FUSED:
                return R.string.device_type_short_gps_speed_and_location;
            case HRM:
                return R.string.device_type_short_heart_rate;
            case SENSOR_MANAGER:
                return R.string.device_type_sensor_manager;
        }
        return 0;
    }

    public static int getNameId(Protocol protocol) {
        switch (protocol) {
            case ANT_PLUS:
                return R.string.protocol_ant_plus;
            case BLUETOOTH_LE:
                return R.string.protocol_bluetooth;
            case ALL:
                return R.string.protocol_all;
            case SMARTPHONE:
                return R.string.smartphone;
            default:
                return R.string.protocol_unknown;
        }
    }


//	public static int getIconId(Protocol protocol)
//	{
//        switch (protocol) {
//            case ANT_PLUS:
//                return R.drawable.ant_logo;
//            case BLUETOOTH_LE:
//                return R.drawable.logo_protocol_bluetooth;
//            case ALL:
//                return R.drawable.logo;
//            case SMARTPHONE:
//                return R.drawable.ic_phone_android_black_48dp;
//            default:
//                return R.drawable.logo;  // TODO: other icon?
//		}
//	}
}
