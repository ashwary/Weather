package com.example.aishwary.weather;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Aishwary on 8/10/2015.
 */
public class ForecastAdapter extends CursorAdapter {

    public ForecastAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        //use placeholder image for now
        ImageView iconView = (ImageView)view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.drawable.ic_launcher);
        //read date from the cursor
        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        TextView dateVIew = (TextView)view.findViewById(R.id.list_item_date_textview);
        dateVIew.setText(Utility.getFriendlyDayString(context, dateInMillis));

        //read weather forecast from cursor
         String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        //Find text view and set weather forecast on it
        TextView descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);

        //read user prefernces for metric of imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        //Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = (TextView)view.findViewById(R.id.list_item_high_textview);
        highView.setText(Utility.formatTemperature(high, isMetric));
        //Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = (TextView)view.findViewById(R.id.list_item_low_textview);
        lowView.setText(Utility.formatTemperature(low, isMetric));
    }

}
