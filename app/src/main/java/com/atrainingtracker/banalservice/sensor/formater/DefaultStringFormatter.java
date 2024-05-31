

package com.atrainingtracker.banalservice.sensor.formater;

// import java.text.NumberFormat;

public class DefaultStringFormatter implements MyFormatter<String> {
    @Override
    public String format(String value) {
        return value;
    }

}
