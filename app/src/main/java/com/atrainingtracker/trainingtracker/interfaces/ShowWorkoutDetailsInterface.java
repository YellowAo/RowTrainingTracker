

package com.atrainingtracker.trainingtracker.interfaces;

import com.atrainingtracker.trainingtracker.exporter.FileFormat;


public interface ShowWorkoutDetailsInterface {
    void exportWorkout(long id, FileFormat fileFormat);

    // implemented as static method in TrainingApplication
    // void startWorkoutDetailsActivity(long workoutId, WorkoutDetailsActivity.SelectedFragment selectedFragment);
    void showExportStatusDialog(long workoutId);
}
