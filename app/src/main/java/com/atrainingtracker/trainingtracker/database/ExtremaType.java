

package com.atrainingtracker.trainingtracker.database;

import com.atrainingtracker.R;
import com.atrainingtracker.trainingtracker.TrainingApplication;

import java.util.LinkedList;
import java.util.List;

public enum ExtremaType {
    MAX(R.string.max),
    MIN(R.string.min),
    AVG(R.string.average),
    START(R.string.start),
    MAX_LINE_DISTANCE(R.string.max_line_distance),
    END(R.string.end);

    public static final ExtremaType[] LOCATION_EXTREMA_TYPES = new ExtremaType[]{START, MAX_LINE_DISTANCE, END};
    private final int nameId;

    ExtremaType(int nameId) {
        this.nameId = nameId;
    }

    public static List<String> getLocationNameList() {
        List<String> result = new LinkedList<>();
        for (ExtremaType extremaType : LOCATION_EXTREMA_TYPES) {
            result.add(extremaType.toString());
        }

        return result;
    }

    @Override
    public String toString() {
        return TrainingApplication.getAppContext().getString(nameId);
    }
}
