package de.j4velin.pedometer.ui.stats;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.Pair;


import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.Temporal;


import java.util.Calendar;

import de.j4velin.pedometer.Database;
import de.j4velin.pedometer.util.Util;

/**
 * Created by henry on 17.02.19.
 */

public class StatPagerAdapter extends FragmentStatePagerAdapter {

    private StepChart.TimeResolution resolution;
    private StepChart.ViewMode mode;
    private Context dbContext;
    private Calendar baseDate;

    public StatPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void init(StepChart.TimeResolution resolution, StepChart.ViewMode mode, Context context) {
        this.resolution = resolution;
        this.mode = mode;
        dbContext = context;


        Database db = Database.getInstance(dbContext);
        baseDate= Calendar.getInstance();
        baseDate.setTimeInMillis(db.getStartDate());
        notifyDataSetChanged();
    }

    private Pair<Long, Long> getTimeRange(int offset) {
        long start = 0, end = 0;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(baseDate.getTimeInMillis());

        switch (resolution) {
            case Day:
                //always show mon-sun
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                cal.add(Calendar.DAY_OF_YEAR, Util.CHART_DAY_COUNT * offset);
                cal.add(Calendar.MINUTE, -Util.CHART_CORRECTION_MINS);
                start = cal.getTimeInMillis();
                cal.add(Calendar.DAY_OF_YEAR, Util.CHART_DAY_COUNT);
                end = cal.getTimeInMillis();
                break;
            case Week:
                //always 1-4
                cal.set(Calendar.DAY_OF_MONTH, 0);
                cal.set(Calendar.WEEK_OF_MONTH, 0);
                cal.add(Calendar.WEEK_OF_MONTH, Util.CHART_WEEK_COUNT * offset);
                cal.add(Calendar.MINUTE, -Util.CHART_CORRECTION_MINS);
                start = cal.getTimeInMillis();
                cal.add(Calendar.WEEK_OF_MONTH, Util.CHART_WEEK_COUNT);
                end = cal.getTimeInMillis();
                break;
            case Month:
                //always 6 months, no special bounds
                cal.set(Calendar.DAY_OF_MONTH, 0);
                cal.add(Calendar.MONTH, Util.CHART_MONTH_COUNT * offset);
                start = cal.getTimeInMillis();
                cal.add(Calendar.MONTH, Util.CHART_MONTH_COUNT);
                end = cal.getTimeInMillis();

                break;
            case Year:
                //
                cal.set(Calendar.DAY_OF_YEAR, 0);
                cal.add(Calendar.YEAR, Util.CHART_YEAR_COUNT * offset);
                start = cal.getTimeInMillis();
                cal.add(Calendar.YEAR, Util.CHART_YEAR_COUNT);
                end = cal.getTimeInMillis();
        }

        return new Pair<>(start, end);
    }


    // Returns total number of pages
    @Override
    public int getCount() {
        Database db = Database.getInstance(dbContext);

        long daysBetween = ChronoUnit.DAYS.between(Instant.ofEpochMilli(baseDate.getTimeInMillis()), Instant.now());
        double count = 0;

        switch (resolution) {
            case Day:
                count = Math.ceil(daysBetween / (double) Util.CHART_DAY_COUNT);
                break;
            case Week:
                count = Math.ceil(daysBetween / (double)(Util.CHART_WEEK_COUNT * 7));
                break;
            case Month:
                count = Math.ceil(daysBetween / (double) (Util.CHART_MONTH_COUNT * 7 * 4));
                break;
            case Year:
                count = Math.ceil((double) db.getYearEntries("AVG").size() / Util.CHART_YEAR_COUNT);
                break;
        }

        Log.i("Step", String.format("DayCount %d, res %s, SiteCount %f", db.getDays(), resolution, count));
        return (int) count;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        Pair<Long, Long> timeRange = getTimeRange(position);
        Bundle bundle = new Bundle();
        bundle.putLong(FragmentStepChart.ARG_START, timeRange.first);
        bundle.putLong(FragmentStepChart.ARG_END, timeRange.second);
        bundle.putString(FragmentStepChart.ARG_RES, resolution.toString());
        bundle.putString(FragmentStepChart.ARG_MODE, mode.toString());
        Fragment frag = new FragmentStepChart();
        frag.setArguments(bundle);

        Log.i("Step", String.format("get item %d, start %d, end %d", position, timeRange.first, timeRange.second));
        return frag;

    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }


    public StepChart.TimeResolution getResolution() {
        return resolution;
    }

    public void setResolution(StepChart.TimeResolution resolution) {
        this.resolution = resolution;
        notifyDataSetChanged();
    }

    public StepChart.ViewMode getMode() {
        return mode;
    }

    public void setMode(StepChart.ViewMode mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }
}
