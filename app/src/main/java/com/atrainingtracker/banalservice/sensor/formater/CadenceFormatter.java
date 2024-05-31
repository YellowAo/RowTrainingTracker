

package com.atrainingtracker.banalservice.sensor.formater;

public class CadenceFormatter implements MyFormatter<Number> {
    @Override
    public String format(Number value) {
        if (value == null) {
            return "--";
        } else {
            // return String.format("%.0f", value);
            return Integer.toString(value.intValue());
        }
    }
}
