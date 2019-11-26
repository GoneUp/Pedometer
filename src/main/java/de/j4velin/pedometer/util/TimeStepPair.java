package de.j4velin.pedometer.util;

import android.util.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStepPair extends Pair<Long, Integer> {
    /**
     * Constructor for a Pair.
     *
     * @param first  the first object in the Pair
     * @param second the second object in the pair
     */
    public TimeStepPair(Long first, Integer second) {
        super(first, second);
    }

    @Override
    public String toString() {
        DateFormat df  = SimpleDateFormat.getDateInstance();

        return "Date: " + df.format(new Date((long) first)) + " StepAvg: " + second;
    }
}
