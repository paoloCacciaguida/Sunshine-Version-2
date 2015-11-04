package com.example.android.sunshine.app;

import android.app.LauncherActivity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Paolo Admin on 18/10/2015.
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment{
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Add line to allow handling of menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            String city = "25014,ITA";
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute(city);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create array of strings containing fake forecast data
        String[] fakeForecastArray = {
                "Today - sunny - 20/23",
                "Sunday - sunny - 20/23",
                "Monday - cloudy - 20/23",
                "Tuesday - storm - 10/13",
                "Wednesday - sunny - 20/23",
                "Thursday - cloudy - 20/23",
                "Friday - the perfect storm - -40/-13"
        };
        // Create list with fake forecast data
        List<String> fakeForecasts = new ArrayList<>(
                Arrays.asList(fakeForecastArray)
        );

        // Create adapter for array of strings
        mForecastAdapter = new ArrayAdapter<>(
                // Current context: the fragment's parent activity
                getActivity(),
                // ID of list item layout
                R.layout.list_item_forecast,
                // ID of text view within the layout
                R.id.list_item_forecast_textview,
                // list of data
                fakeForecasts
        );

        // Bind the adapter to the list view
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);


        // Attach listener to display toast message
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectecForecast = mForecastAdapter.getItem(position);
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(getActivity(), selectecForecast, duration);
                toast.show();
            }
        });


        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private final String QUERY_PARAM = "q";
        private final String FORMAT_PARAM = "mode";
        private final String UNITS_PARAM = "units";
        private final String DAYS_PARAM = "cnt";
        private final String APPID_PARAM = "APPID";


        @Override
        protected String[] doInBackground(String... strings) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            // Will contain an a string description of each day's forecast
            String[] realForecastsArray = null;

            // Determine postCode based on input parameter
            String postCode = "94043"; //default
            if (strings.length > 0 && strings[0] != null){
                postCode = strings[0];
            }

            // Build URL using Uri Builder
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("http");
            uriBuilder.authority("api.openweathermap.org");
            uriBuilder.appendPath("data/2.5/forecast/daily");
            uriBuilder.appendQueryParameter(QUERY_PARAM, postCode);
            uriBuilder.appendQueryParameter(FORMAT_PARAM, "json");
            uriBuilder.appendQueryParameter(UNITS_PARAM, "metric");
            uriBuilder.appendQueryParameter(DAYS_PARAM, "7");

            Uri builtUri = uriBuilder.build();

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(builtUri.toString());
Log.v(LOG_TAG,"Built URL: " + url);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
Log.v(LOG_TAG,"Connection established");
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
Log.v(LOG_TAG,"Buffered reader created");
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
Log.v(LOG_TAG,"Weather API response:");
Log.v(LOG_TAG,forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                realForecastsArray = WeatherDataParser.getWeatherDataFromJson(forecastJsonStr,7);
                Log.v(LOG_TAG, Arrays.toString(realForecastsArray));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing JSON response");
                return null;
            }

            return realForecastsArray;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            mForecastAdapter.clear();
            for (String item : strings){
                mForecastAdapter.add(item);
            }
            // mForecastAdapter.addAll(realForecasts);

            // The view is updated without the need to call mForecastAdapter.notifyDataSetChanged();
            // because the add method of array adapter already does it internally.

            super.onPostExecute(strings);
        }
    }
}
