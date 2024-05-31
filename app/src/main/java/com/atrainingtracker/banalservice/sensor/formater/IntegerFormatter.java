

package com.atrainingtracker.banalservice.sensor.formater;

public class IntegerFormatter implements MyFormatter<Number> {

    @Override
    public String format(Number value) {
        if (value == null) {
            return "--";
        } else {
            return value.intValue() + "";
        }
    }
}
