package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    public ForecastFragment() {
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
        ArrayAdapter<String> forecastArrayAdapter = new ArrayAdapter<>(
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
        listView.setAdapter(forecastArrayAdapter);




        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        @Override
        protected String doInBackground(String... strings) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

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

            return null;
        }
    }
}
