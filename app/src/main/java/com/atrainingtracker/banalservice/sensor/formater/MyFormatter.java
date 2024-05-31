

package com.atrainingtracker.banalservice.sensor.formater;

public interface MyFormatter<T> {
    // TODO: add units???
    String format(T value);
}