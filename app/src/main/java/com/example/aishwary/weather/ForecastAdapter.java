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



    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

         public ViewHolder(View view){
             iconView = (ImageView)view.findViewById(R.id.list_item_icon);
             dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
             descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
             highTempView = (TextView)view.findViewById(R.id.list_item_high_textview);
             lowTempView = (TextView)view.findViewById(R.id.list_item_low_textview);

         }

    }
    public ForecastAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        //return view;
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch(viewType){
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
         ViewHolder viewHolder = (ViewHolder) view.getTag();
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType){
            case VIEW_TYPE_TODAY: {
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            }case VIEW_TYPE_FUTURE_DAY: {
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
        }

        // Read weather icon ID from cursor
        //int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        //use placeholder image for now
        //ImageView iconView = (ImageView)view.findViewById(R.id.list_item_icon);
        //iconView.setImageResource(R.drawable.ic_launcher);
        //read date from the cursor
         //viewHolder.iconView.setImageResource(R.drawable.ic_launcher);

        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        //TextView dateVIew = (TextView)view.findViewById(R.id.list_item_date_textview);
        //dateVIew.setText(Utility.getFriendlyDayString(context, dateInMillis));
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        //read weather forecast from cursor
         String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        //Find text view and set weather forecast on it
        //TextView descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
         viewHolder.descriptionView.setText(description);
        //read user prefernces for metric of imperial temperature units
          boolean isMetric = Utility.isMetric(context);

        //Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        //TextView highView = (TextView)view.findViewById(R.id.list_item_high_textview);
        //highView.setText(Utility.formatTemperature(high, isMetric));
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high, isMetric));

        //Read low temperature from cursor

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        //TextView lowView = (TextView)view.findViewById(R.id.list_item_low_textview);
        //lowView.setText(Utility.formatTemperature(low, isMetric));
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low, isMetric));
    }

    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position){
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }
    @Override
    public int getViewTypeCount(){
        return VIEW_TYPE_COUNT;
    }

}
