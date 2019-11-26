/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.j4velin.pedometer.util;

import java.util.Calendar;
import java.util.Locale;

import de.j4velin.pedometer.ui.stats.StepChart;

public abstract class Util {
    public static final int CHART_DAY_COUNT = 7;
    public static final int CHART_WEEK_COUNT = 4;
    public static final int CHART_MONTH_COUNT = 6;
    public static final int CHART_YEAR_COUNT = 10;

    public static final int CHART_CORRECTION_MINS = 5;
    public static final int CHART_CORRECTION_MILLIS = CHART_CORRECTION_MINS * 60 * 1000;


    /**
     * @return milliseconds since 1.1.1970 for today 0:00:00 local timezone
     */
    public static long getToday() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /**
     * @return milliseconds since 1.1.1970 for tomorrow 0:00:01 local timezone
     */
    public static long getTomorrow() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DATE, 1);
        return c.getTimeInMillis();
    }



//would be so nice bitwise
    public static boolean isStepModeActive(StepChart.ViewMode current) {
        return (current == StepChart.ViewMode.StepAvg || current == StepChart.ViewMode.StepTotal);
    }

    public static boolean isDistanceModeActive(StepChart.ViewMode current) {
        return (current == StepChart.ViewMode.DistanceAvg || current == StepChart.ViewMode.DistanceTotal);
    }
    public static boolean isAverageModeActive(StepChart.ViewMode current) {
        return (current == StepChart.ViewMode.StepAvg || current == StepChart.ViewMode.DistanceAvg);
    }

    public static String formatDistance(StepChart.Unit mode, double distance){
        String unit = mode == StepChart.Unit.CM ? "km" : "m";

        return String.format(Locale.getDefault(), "%.2f %s", distance, unit);
    }

}
