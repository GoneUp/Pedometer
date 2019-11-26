package de.j4velin.pedometer.ui.stats;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.j4velin.pedometer.R;
import de.j4velin.pedometer.util.Util;

public class FragmentStepChart extends Fragment {
    public static String ARG_START = "arg_start";
    public static String ARG_END = "arg_end";
    public static String ARG_RES = "arg_res";
    public static String ARG_MODE = "arg_mode";

    long start, end;
    StepChart.TimeResolution resolution;
    private StepChart stepChart;
    private TextView dateText;

    public FragmentStepChart(){}

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_stepchart, container, false);

        start = getArguments().getLong(ARG_START);
        end = getArguments().getLong(ARG_END);
        resolution = StepChart.TimeResolution.valueOf(getArguments().getString(ARG_RES));
        StepChart.ViewMode viewMode = StepChart.ViewMode.valueOf(getArguments().getString(ARG_MODE));

        stepChart = v.findViewById(R.id.bargraph);
        stepChart.setTimeRange(start, end, resolution);
        stepChart.setBaseMode(viewMode);

        //correction from a time range that begins before and ends after the searchen dataset is needed to display the correct date
        DateFormat df = new SimpleDateFormat("dd.MM.YYYY", Locale.getDefault());
        String startText = df.format(new Date(start + Util.CHART_CORRECTION_MILLIS * 2));
        String endText = df.format(new Date(end - Util.CHART_CORRECTION_MILLIS * 2));

        dateText = v.findViewById(R.id.dateText);
        dateText.setText(String.format("%s - %s", startText, endText));

        if (resolution != StepChart.TimeResolution.Day) {
            ((ViewGroup)dateText.getParent()).removeView(dateText);
        }

        v.invalidate();
        return v;
    }



    @Override
    public void onResume() {
        super.onResume();
    }



}
