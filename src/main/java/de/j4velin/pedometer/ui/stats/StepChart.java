package de.j4velin.pedometer.ui.stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.j4velin.pedometer.Database;
import de.j4velin.pedometer.TodayCountCache;
import de.j4velin.pedometer.ui.Fragment_Settings;
import de.j4velin.pedometer.util.Logger;
import de.j4velin.pedometer.util.TimeStepPair;
import de.j4velin.pedometer.util.Util;

import static android.content.ContentValues.TAG;

public class StepChart extends BarChart {
    public enum ViewMode {
        StepAvg,
        StepTotal,

        DistanceAvg,
        DistanceTotal
    }

    public enum Unit {
        CM,
        FT
    }

    public enum TimeResolution {
        Year,
        Month,
        Week,
        Day
    }

    private static int COLOR_GOAL_REACHED = Color.parseColor("#99CC00");
    private static int COLOR_GOAL_NOT_REACHED =  Color.parseColor("#0099cc");

    private Context context;
    private ViewMode baseMode;
    private Unit baseUnit;
    private float stepSize;
    private int goal;

    private long start;
    private long end;
    private TimeResolution resolution;

    public StepChart(Context context) {
        super(context);
    }

    public StepChart(Context context, AttributeSet attrs)  {
        super(context, attrs);
        this.context = context;
        this.setShowDecimal(false);

        baseMode = ViewMode.StepAvg;
        resolution = TimeResolution.Day;

        Logger.log("init ");
    }

    private void loadSettings() {
        SharedPreferences prefs = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        stepSize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
        if (prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT).equals("cm")) {
            baseUnit = Unit.CM;
        } else {
            baseUnit = Unit.FT;
        }
        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
    }

    public void setTimeRange(long start, long end, TimeResolution resolution) {
        setResolution(resolution);
        setTimeRange(start, end);
    }

    public void setTimeRange(long start, long end) {
        this.start = start;
        this.end = end;
        updateBars();
    }


    public void updateBars() {
        loadSettings();


        if (this.getData().size() > 0)
            this.clearChart();

        SimpleDateFormat df = getResolutionDateFormat();
        this.setShowDecimal(Util.isDistanceModeActive(baseMode)); // show decimal in distance view only
        BarModel bm;
        Database db = Database.getInstance(context);
        List<TimeStepPair> set = getDataset();
        db.close();
        for (int i = set.size() - 1; i >= 0; i--) {
            TimeStepPair current = set.get(i);
            int steps = current.second;
            if (steps > -1) {
                int color = COLOR_GOAL_NOT_REACHED;
                if (resolution == TimeResolution.Day && steps > goal) {
                    color = COLOR_GOAL_REACHED;
                }

                bm = new BarModel(formatBarText(current), 0, color);

                if (Util.isStepModeActive(baseMode)) {
                    bm.setValue(steps);
                } else {
                    bm.setValue(getDistance(steps));
                }
                this.addBar(bm);
            }
        }

        if (set.size() != 7) {
            Log.w("Ped", "updateBars: set is invalid! start " + df.format(new Date(start)));
            getDataset();
        }
    }

    private List<TimeStepPair> getDataset(){
        Database db = Database.getInstance(context);
        List<TimeStepPair> data = null;
        String queryFunction = Util.isAverageModeActive(baseMode) ? "AVG" : "SUM";

        switch (resolution) {
            case Day:
                data = db.getDayEntries(start, end);
                data = checkAndFillDayEntries(data);

                break;
            case Week:
                data = db.getWeekEntries(start, end, queryFunction);
                break;
            case Month:
                data = db.getMonthEntries(start, end, queryFunction);
                break;
            case Year:
                data = db.getYearEntries(queryFunction);
                break;
        }
        return data;
    }

    /**
     * Removes the current day and fills voids
     * 
     * @param data 
     */
    private List<TimeStepPair> checkAndFillDayEntries(List<TimeStepPair> data) {
        List<TimeStepPair> returnSet = new LinkedList<>();

        
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(start);
        c.add(Calendar.DAY_OF_YEAR, 1); //start is 23:45, so we need to move to next day and set to zero
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //odering is importnt
        for (int i = 0; i < Util.CHART_DAY_COUNT; i++) {
            TimeStepPair found = null;
            for (TimeStepPair day : data) {
                if (day.first == c.getTimeInMillis()) {
                    found = day;
                    break;
                }
            }

            if (found != null) {
                if (found.first != Util.getToday()) {
                    returnSet.add(found);
                } else {
                    //add tmp value of current day
                    returnSet.add(new TimeStepPair(Util.getToday(), TodayCountCache.getTodayStepNo()));
                }
            } else {
                returnSet.add(new TimeStepPair(c.getTimeInMillis(), 0));
            }

            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        Log.d(TAG, String.format("checkAndFillDayEntries: org %d new is %d ",data.size(), returnSet.size()));
        if (returnSet.size() != 7) {
            Log.i(TAG, "checkAndFillDayEntries: WTF " + returnSet.size());
        }
        Collections.reverse(returnSet);
        return returnSet;
    }

    private String formatBarText(TimeStepPair pair) {
        SimpleDateFormat df = getResolutionDateFormat();
        if (resolution != TimeResolution.Week) {
            //default case
            return df.format(new Date(pair.first));
        } else {
            //week has a special time range formatting
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(pair.first);
            c.add(Calendar.DAY_OF_YEAR, 6);

           return String.format("%s - %s", df.format(new Date(pair.first)), df.format(new Date(c.getTimeInMillis())));
        }
    }

    private SimpleDateFormat getResolutionDateFormat(){
        switch (resolution) {
            case Day:
                return new SimpleDateFormat("E", Locale.getDefault());
            case Week:
                return new SimpleDateFormat("dd.MM", Locale.getDefault());
            case Month:
                return new SimpleDateFormat("MM.YY", Locale.getDefault());
            case Year:
                return new SimpleDateFormat("YYYY", Locale.getDefault());
        }

        return null;
    }


    private float getDistance(int steps) {
        float distance = steps * stepSize;
        if (baseUnit == Unit.CM) {
            distance /= 100000;
        } else {
            distance /= 5280;
        }
        return Math.round(distance * 1000) / 1000f; // 3 decimals
    }




    public ViewMode getBaseMode() {
        return baseMode;
    }

    public void setBaseMode(ViewMode baseMode) {
        this.baseMode = baseMode;
        updateBars();
    }

    public TimeResolution getResolution() {
        return resolution;
    }

    public void setResolution(TimeResolution resolution) {
        this.resolution = resolution;
    }
}
