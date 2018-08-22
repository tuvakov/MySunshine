package com.example.android.sunshine;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    // String that holds weather data
    private String mWeatherText;
    // Textview reference that displays weather data
    private TextView mWeatherTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Set the textview
        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_detail);

        // Get intent
        Intent comingIntent = getIntent();

        // Check if it has the extra
        if(comingIntent != null && comingIntent.hasExtra(Intent.EXTRA_TEXT)){
            // Get the extra
            mWeatherText = comingIntent.getStringExtra(Intent.EXTRA_TEXT);
            // Set the weather text to the textview
            mWeatherTextView.setText(mWeatherText);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get menu item Id
        int itemId = item.getItemId();

        switch (itemId){
            case R.id.action_share_weather:
                shareWeatherData();
                return true;
        }

        return super.onOptionsItemSelected(item);

        /**
         * Another way of starting an intent directly through an menu item
         * getMenuInflater().inflate(R.menu.detail, menu);
         MenuItem menuItem = menu.findItem(R.id.action_share);
         menuItem.setIntent(createShareForecastIntent());
         */
    }

    /**
     * Shares the weather detail through an implicit intent
     */
    private void shareWeatherData(){
        // ShareCompact parameters
        String mimeType = "text/plain";
        String chooserTitle = "Weather data";

        // Build an Intent object via ShareCompact
        Intent intent = ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(chooserTitle)
                .setType(mimeType)
                .setText(mWeatherText)
                .getIntent();

        // Make sure there is an app to handle this intent and start the intent
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }
}
