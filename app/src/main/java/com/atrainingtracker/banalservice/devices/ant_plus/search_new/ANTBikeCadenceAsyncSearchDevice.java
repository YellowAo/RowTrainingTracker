

package com.atrainingtracker.banalservice.devices.ant_plus.search_new;

import android.content.Context;

import com.atrainingtracker.banalservice.devices.DeviceType;
import com.atrainingtracker.banalservice.devices.ant_plus.search_new.ANTSearchForNewDevicesEngineMultiDeviceSearch.IANTAsyncSearchEngineInterface;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

public class ANTBikeCadenceAsyncSearchDevice extends MyANTAsyncSearchDevice {

    public ANTBikeCadenceAsyncSearchDevice(Context context, IANTAsyncSearchEngineInterface callback, MultiDeviceSearchResult deviceFound, boolean pairingRecommendation) {
        super(context, callback, DeviceType.ROWING_CADENCE, deviceFound, pairingRecommendation);
    }

    @Override
    protected void subscribeCommonEvents(AntPluginPcc antPluginPcc) {
        onNewBikeSpdCadCommonPccFound((AntPlusBikeCadencePcc) antPluginPcc);
    }

    @Override
    protected PccReleaseHandle requestAccess() {
        return AntPlusBikeCadencePcc.requestAccess(mContext, mDeviceFound.getAntDeviceNumber(), 0, false, new MyResultReceiver<AntPlusBikeCadencePcc>(), new MyDeviceStateChangeReceiver());
    }

}
