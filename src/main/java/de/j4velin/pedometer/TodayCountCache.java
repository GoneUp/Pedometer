package de.j4velin.pedometer;

public class TodayCountCache {
    private static int todayStepNo = 0;

    public static int getTodayStepNo() {
        return todayStepNo;
    }
    public static void setTodayStepNo(int todayStepNo) {
        TodayCountCache.todayStepNo = todayStepNo;
    }
}
