package de.j4velin.pedometer;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class PedoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
