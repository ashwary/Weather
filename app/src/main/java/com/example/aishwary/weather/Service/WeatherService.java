package com.example.aishwary.weather.Service;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.aishwary.weather.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Aishwary on 8/22/2015.
 */
public class WeatherService extends IntentService {
    private ArrayAdapter<String> mForecastAdapter;
    public static final String LOCATION_QUERY_EXTRA = "lqe";
    private final String LOG_TAG = WeatherService.class.getSimpleName();

    public WeatherService() {
        super("Weather");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String locationQuery = intent.getStringExtra(LOCATION_QUERY_EXTRA);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw Json response as a String

        String forecastJsonStr = null;
        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();

            URL url = new URL(builtUri.toString());
            //create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //Read the input stream into a String

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // nothing to do
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty . no point in attempting to parse it

            }

            forecastJsonStr = buffer.toString();
            getWeatherDataFromJson(forecastJsonStr, locationQuery);


        }catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);

            //If the code didnt successfully get the weather data, there is no point
            // in attemption to parse it

        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();

        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                }catch (final IOException e){
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }
               return;
    }

     private void getWeatherDataFromJson(String forecastJsonStr,
                                         String locationSetting)
     throws JSONException{
         final String OWM_CITY = "city";
         final String OWM_CITY_NAME = "name";
         final String OWM_COORD = "coord";

         //location coordinates
         final String OWM_LATITUDE = "lat";
         final String OWM_LONGITUDE = "long";

         //Weather Information
         final String OWM_LIST = "list";

         final String OWM_PRESSURE = "pressure";
         final String OWM_HUMIDITY = "humidity";
         final String OWM_WINDSPEED = "speed";
         final String OWM_WIND_DIRECTION = "deg";

         // All temperatures are children of the "temp" object.
         final String OWM_TEMPERATURE = "temp";
         final String OWM_MAX = "max";
         final String OWM_MIN = "min";

         final String OWM_WEATHER = "weather";
         final String OWM_DESCRIPTION = "main";
         final String OWM_WEATHER_ID = "id";

         try {  //forecast Json string will be used to fetch Raw data from json
             JSONObject forecastJSON = new JSONObject(forecastJsonStr);
             JSONArray weatherArray = forecastJSON.getJSONArray(OWM_LIST);

             JSONObject cityJson = forecastJSON.getJSONObject(OWM_CITY);
             String cityName = cityJson.getString(OWM_CITY_NAME);

             JSONObject cityCoord = forecastJSON.getJSONObject(OWM_COORD);
             double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
             double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

             long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

             //Insert the new weather Information into the database
             Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());
             // OWM returns daily forecasts based upon the local time of the city that is being
             // asked for, which means that we need to know the GMT offset to translate this data
             // properly.

             // Since this data is also sent in-order and the first day is always the
             // current day, we're going to take advantage of that to get a nice
             // normalized UTC date for all of our weather.

             Time dayTime = new Time();
             dayTime.setToNow();

             int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

             //
             dayTime = new Time();

             for (int i = 0; i<weatherArray.length(); i++) {
                 // these are the values that will be collected
                  long datetime;
                  double pressure;
                 int humidity;
                 double windSpeed;
                 double windDirection;

                 double high;
                 double low;

                 String description;
                 int weatherId;

                 //get the json object representing the day
                      JSONObject dayForecast = weatherArray.getJSONObject(i);

                 // convert this into UTC time,
                   datetime = dayTime.setJulianDay(julianStartDay + i);

                 pressure = dayForecast.getDouble(OWM_PRESSURE);
                 humidity = dayForecast.getInt(OWM_HUMIDITY);
                 windSpeed = dayForecast.getDouble(OWM_HUMIDITY);
                 windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);
                 // Description is in a child array called "weather", which is 1 element long.
                 // That element also contains a weather code.

                 JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                          description = weatherObject.getString(OWM_DESCRIPTION);
                          weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                 JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);

                      high = dayForecast.getDouble(OWM_MAX);
                      low = dayForecast.getDouble(OWM_MIN);

                 ContentValues weatherValues = new ContentValues();
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, datetime);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                 weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                    cVVector.add(weatherValues);
             }

                int inserted = 0;
             // add to database
             if (cVVector.size() > 0){
                 ContentValues[] cvArray = new ContentValues[cVVector.size()];
                 cVVector.toArray(cvArray);
                 this.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,cvArray);
             }
                 Log.d(LOG_TAG, "Weather Service Complete. " + cVVector.size() + " Inserted");
         } catch (JSONException e){
             Log.e(LOG_TAG, e.getMessage(), e);
             e.printStackTrace();
         }

     }
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        Cursor locationCursor = this.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

           if (locationCursor.moveToFirst()){
               int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
               locationId = locationCursor.getLong(locationIdIndex);
           }else {
               // Now that the content provider is set up, inserting rows of data is pretty simple.
               // First create a ContentValues object to hold the data you want to insert.
               ContentValues locationValues = new ContentValues();

               // Then add the data, along with the corresponding name of the data type,
               // so the content provider knows what kind of value is being inserted.
               locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
               locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
               locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
               locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

               // Finally, insert location data into the database
               Uri insertedUri = this.getContentResolver().insert(
                       WeatherContract.LocationEntry.CONTENT_URI,
                       locationValues
               );
               // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
               locationId = ContentUris.parseId(insertedUri);
           }
        locationCursor.close();
        // Wait, that worked?  Yes!
        return locationId;




    }

}

