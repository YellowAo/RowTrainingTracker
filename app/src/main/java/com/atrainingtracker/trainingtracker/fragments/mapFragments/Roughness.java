

package com.atrainingtracker.trainingtracker.fragments.mapFragments;



public enum Roughness {
    ALL(1),
    MEDIUM(30);
    // ROUGH(5*60);

    public int stepSize;

    Roughness(int stepSize) {
        this.stepSize = stepSize;
    }
}
