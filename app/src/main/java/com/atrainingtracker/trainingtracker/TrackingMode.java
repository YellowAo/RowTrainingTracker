

package com.atrainingtracker.trainingtracker;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.BSportType;


public enum TrackingMode {
    WAITING_FOR_BANAL_SERVICE(R.string.waiting_for, R.string.banal_service, R.string.banal_service, R.string.banal_service),
    SEARCHING(R.string.searching_for, R.string.some_remote_device, R.string.some_remote_device, R.string.some_remote_device),
    READY(R.string.ready_to, R.string.ready_run, R.string.ready_row, R.string.ready_other),
    TRACKING(R.string.tracking, R.string.tracking_run, R.string.tracking_row, R.string.tracking_other),
    PAUSED(R.string.paused, R.string.paused_run, R.string.paused_row, R.string.paused_other);


    private final int titleId;
    private final int runId;
    private final int rowId;
    private final int otherId;

    TrackingMode(int titleId, int runId, int rowId, int otherId) {
        this.titleId = titleId;
        this.runId = runId;
        this.rowId = rowId;
        this.otherId = otherId;
    }

    public int getTitleId() {
        return titleId;
    }

    public int getSportId(BSportType sportType) {
        switch (sportType) {
            case RUN:
                return runId;

            case ROWING:
                return rowId;

            default:
                return otherId;
        }
    }

}
