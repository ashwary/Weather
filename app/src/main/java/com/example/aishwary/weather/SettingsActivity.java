package com.example.aishwary.weather;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.example.aishwary.weather.data.WeatherContract;
import com.example.aishwary.weather.sync.WeatherSyncAdapter;

/**
 * Created by Aishwary on 8/7/2015.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add general prefernces defined in the xml file
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }


    @Override
    protected void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();

    }

    @Override
    protected void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    //for all prefernces attach an on prefence change listener so that the ui
    //summary can be updated when the prefernce changes.

    // attaches  a listener so that the summary is always updated with the prefernce value
    //Also fires the listener once, to initialize the summary(so it shows up before the value is changed

    private void bindPreferenceSummaryToValue(Preference preference) {
        //Set the listener to watch for the value changes
        preference.setOnPreferenceChangeListener(this);
        // Trigger the listener immediately with the preferencess current value

        setPreferenceSummary(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    /**
     * A {@link Preference} that displays a list of entries as
     * a dialog.
     * <p/>
     * This preference will store a string into the SharedPreferences. This string will be the value
     * from the {@link (CharSequence[])} array.
     *
     * @attr ref android.R.styleable#ListPreference_entries
     * @attr ref android.R.styleable#ListPreference_entryValues
     */



    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();
        // Returns a string containing a concise, human-readable
        //form of this object
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (key.equals(getString(R.string.pref_location_key))) {
            @WeatherSyncAdapter.LocationStatus int status = Utility.getLocationStatus(this);
            switch (status) {
                case WeatherSyncAdapter.LOCATION_STATUS_OK:
                    preference.setSummary(stringValue);
                    break;

                case WeatherSyncAdapter.LOCATION_STATUS_UNKNOWN:
                    preference.setSummary(getString(R.string.pref_location_unknown_description, value.toString()));
                    break;

                case WeatherSyncAdapter.LOCATION_STATUS_INVALID:
                    preference.setSummary(getString(R.string.pref_location_error_description, value.toString()));
                    break;

                default:
                    preference.setSummary(stringValue);

            }
        } else {
            //for other preferences , set the summary to the value's simple string
            //representation
            preference.setSummary(stringValue);
        }
    }



    @Override

    public boolean onPreferenceChange(Preference preference, Object value) {
        setPreferenceSummary(preference, value);

        return true;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_key))) {
            //We ve changedthe location
            //first clear location status
            Utility.resetLocationStatus(this);
            WeatherSyncAdapter.syncImmediately(this);
        } else if (key.equals(getString(R.string.pref_units_key))) {

            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        } else if ( key.equals(getString(R.string.pref_location_status_key))) {
            Preference locationPreference = findPreference(getString(R.string.pref_location_key));
            bindPreferenceSummaryToValue(locationPreference);
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

}
