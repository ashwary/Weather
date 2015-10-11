package com.tandon.aishwary.weather.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Aishwary on 8/23/2015.
 */
public class WeatherSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static WeatherSyncAdapter wWeatherSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("WeatherSyncService", "onCreate - WeatherSyncService");
        synchronized (sSyncAdapterLock) {
            if (wWeatherSyncAdapter == null) {
                wWeatherSyncAdapter = new WeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return wWeatherSyncAdapter.getSyncAdapterBinder();
    }
}