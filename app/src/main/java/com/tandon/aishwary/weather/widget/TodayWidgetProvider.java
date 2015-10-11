package com.tandon.aishwary.weather.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tandon.aishwary.weather.sync.WeatherSyncAdapter;

/**
 * Created by Aishwary on 9/24/2015.
 */
public class TodayWidgetProvider extends AppWidgetProvider {
    /**
     * Provider for a widget showing today's weather.
     *
     * Delegates widget updating to {@link TodayWidgetIntentService} to ensure that
     * data retrieval is done on a background thread
     */


        @Override
        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            context.startService(new Intent(context, TodayWidgetIntentService.class));
        }

        @Override
        public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                              int appWidgetId, Bundle newOptions) {
            context.startService(new Intent(context, TodayWidgetIntentService.class));
        }

        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            super.onReceive(context, intent);
            if (WeatherSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
                context.startService(new Intent(context, TodayWidgetIntentService.class));
            }
        }
    }