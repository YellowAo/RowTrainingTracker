

package com.atrainingtracker.banalservice.devices.ant_plus.search_new;

import android.content.Context;

import com.atrainingtracker.banalservice.devices.DeviceType;
import com.atrainingtracker.banalservice.devices.ant_plus.search_new.ANTSearchForNewDevicesEngineMultiDeviceSearch.IANTAsyncSearchEngineInterface;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

public class ANTBikePowerAsyncSearchDevice extends MyANTAsyncSearchDevice {

    public ANTBikePowerAsyncSearchDevice(Context context, IANTAsyncSearchEngineInterface callback, MultiDeviceSearchResult deviceFound, boolean pairingRecommendation) {
        super(context, callback, DeviceType.ROWING_POWER, deviceFound, pairingRecommendation);
    }

    @Override
    protected void subscribeCommonEvents(AntPluginPcc antPluginPcc) {
        onNewCommonPccFound((AntPlusBikePowerPcc) antPluginPcc);
    }

    @Override
    protected PccReleaseHandle requestAccess() {
        return AntPlusBikePowerPcc.requestAccess(mContext, mDeviceFound.getAntDeviceNumber(), 0, new MyResultReceiver<AntPlusBikePowerPcc>(), new MyDeviceStateChangeReceiver());
    }

}
