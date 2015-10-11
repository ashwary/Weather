package com.tandon.aishwary.weather;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tandon.aishwary.weather.data.WeatherContract;
import com.tandon.aishwary.weather.data.WeatherContract.WeatherEntry;


/**
 * Created by Aishwary on 8/19/2015.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    static final String DETAIL_URI = "URI";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";

    private static final String FORECAST_SHARE_HASHTAG = " #WeatherApp";

    private ShareActionProvider mShareActionProvider;

    private String mForecast;
    private Uri mUri;
    private boolean mTransitionAnimation;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            //this works because the waether provider returns location data joined
            //with weathr data, even though they are stored in two different places
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING

    };
    //these indices are tide to DETAIL_COLUMNS. If DETAIL_COLUMNS changes, these
    //must change

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mHumidityLabelView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mWindLabelView;
    private TextView mPressureLabelView;
    private TextView mPressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mHumidityLabelView = (TextView) rootView.findViewById(R.id.detail_humidity_label_textview);
        mPressureLabelView = (TextView) rootView.findViewById(R.id.detail_pressure_label_textview);
        mWindLabelView = (TextView) rootView.findViewById(R.id.detail_wind_label_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        return rootView;
    }

    private void finishCreatingMenu(Menu menu) {
        //Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflate the menu, this adds items to the action bar if they are present
        if (getActivity() instanceof DetailActivity) {
            inflater.inflate(R.menu.detailfragment, menu);
            finishCreatingMenu(menu);
        }

    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged(String newLocation) {
        // replace the uri since the uri has been changed.
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Log.v(LOG_TAG, "In onCreateLoader");
        //Intent intent = getActivity().getIntent();
        //modify the intent so that it doesnt expect the data uri
        //the reason the detail fragment can be left blank is that now it detail fragment can be
        //left inside the main activity
        //display hardcoded data in detail fragment even without incoming intent
        //if (intent == null || intent.getData() == null){
        //return null;
        if (null != mUri) {

            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );

        }
        ViewParent vp = getView().getParent();
        if ( vp instanceof CardView) {
            ((View) vp).setVisibility(View.INVISIBLE);
        }
        //now create and return a cursorloader that will take care of
        // creating a cursor for the data being displayed
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            ViewParent vp = getView().getParent();
            if ( vp instanceof CardView ) {
                ((View)vp).setVisibility(View.VISIBLE);
            }

            //Read com.tandon.aishwary.weather condition id from the cursor
            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

            // Use com.tandon.aishwary.weather Art
            // Use com.tandon.aishwary.weather art image
            if (Utility.usingLocalGraphics(getActivity())) {
                mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            } else {
                Glide.with(this)
                        .load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                        .error(Utility.getArtResourceForWeatherCondition(weatherId))
                        .crossFade()
                        .into(mIconView);


            }


            // mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            //read date from cursor and update views for day of week and date
            long date = data.getLong(COL_WEATHER_DATE);
            String dateText = Utility.getFullFriendlyDayString(getActivity(), date);
            mDateView.setText(dateText);

            //Read description from the cursor and update view
            String description = Utility.getStringForWeatherCondition(getActivity(), weatherId);
            mDescriptionView.setText(description);
            mDescriptionView.setContentDescription(getString(R.string.a11y_forecast, description));

            //For accessibility, add a content description to the icon field
            mIconView.setContentDescription(getString(R.string.a11y_forecast_icon, description));

            //Read high temperature from cursor and update view
            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(getActivity(), high);
            mHighTempView.setText(highString);
            //Read low temperature from cursor and update view
            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(getActivity(), low);
            mLowTempView.setText(lowString);


            //Read humidity from cursor and update view
            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
            mHumidityView.setContentDescription(getString(R.string.a11y_humidity, mHumidityView.getText()));
            mHumidityLabelView.setContentDescription(mHumidityView.getContentDescription());

            //Read wind speed and direction from cursor and update view
            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
            mWindView.setContentDescription(getString(R.string.a11y_wind, mWindView.getText()));
            mWindLabelView.setContentDescription(mWindView.getContentDescription());


            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            mPressureView.setText(getString(R.string.format_pressure, pressure));

            mPressureView.setContentDescription(getString(R.string.a11y_pressure, mPressureView.getText()));
            mPressureLabelView.setContentDescription(mPressureView.getContentDescription());
            // We still need this for the share intent
            mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);


            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        //need to start the enter transition after the data has loaded
        if ( mTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if (null != toolbarView) {
                activity.setSupportActionBar(toolbarView);
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

        }else {
            if (null != toolbarView) {
                Menu menu = toolbarView.getMenu();
                if (null != menu) menu.clear();
                toolbarView.inflateMenu(R.menu.detailfragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }
        @Override
        public void onLoaderReset (Loader<Cursor> loader) {}

    }



