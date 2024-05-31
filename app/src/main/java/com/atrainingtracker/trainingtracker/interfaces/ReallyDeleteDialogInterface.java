

package com.atrainingtracker.trainingtracker.interfaces;


public interface ReallyDeleteDialogInterface {
    void confirmDeleteWorkout(long workoutId);

    void reallyDeleteWorkout(long workoutId);
}
