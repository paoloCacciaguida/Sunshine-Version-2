package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent detailIntent = new Intent(this, SettingsActivity.class);
            startActivity(detailIntent);
            return true;
        }

        if (id == R.id.action_show_location) {

            return showLocationOnMap();
        }

        return super.onOptionsItemSelected(item);
    }

    protected boolean showLocationOnMap(){
        Intent locationIntent = new Intent(Intent.ACTION_VIEW);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPrefs.getString(getString(R.string.pref_key_location), getString(R.string.pref_default_location));
        String uriString = "geo:0,0?q=" + Uri.encode(location);
        Uri geoLocation = Uri.parse(uriString).buildUpon().build();

        locationIntent.setData(geoLocation);
        if (locationIntent.resolveActivity(getPackageManager()) != null){
            startActivity(locationIntent);
            return true;
        }
        else {
            int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(this, "No maps app found", duration);
                toast.show();
            return false;
        }

    }

}
