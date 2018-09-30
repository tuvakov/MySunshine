package com.example.android.sunshine;

import com.example.android.sunshine.databinding.ActivityDetailBinding;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.data.database.AppDatabase;
import com.example.android.sunshine.data.database.WeatherDao;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

public class DetailActivity extends AppCompatActivity {

    // Log TAG
    private final String TAG = this.getClass().getSimpleName();

    // String that holds weather data to share
    private String mForecastSummary;
    // String constant for Intent key
    public static final String INTENT_ID_KEY = "weatherEntryId";
    private final int DEFAULT_WEATHER_ENTRY_ID = 0;
    private AppDatabase mDb;

    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Instantiate mDetailBinding */
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Instantiate db
        mDb = AppDatabase.getsInstance(getApplication().getBaseContext());

        // Get intent
        Intent comingIntent = getIntent();

        // Check if it has the extra
        if(comingIntent != null && comingIntent.hasExtra(INTENT_ID_KEY)){
            // Get the extra
            int weatherId = comingIntent.getIntExtra(INTENT_ID_KEY, DEFAULT_WEATHER_ENTRY_ID);

            // ViewModelFactory
            DetailViewModelFactory modelFactory = new DetailViewModelFactory(weatherId, mDb);

            // ViewModel
            DetailViewModel viewModel =
                    ViewModelProviders.of(this, modelFactory).get(DetailViewModel.class);

            // Get LiveData
            final LiveData<WeatherEntry> weather = viewModel.getWeatherEntry();

            weather.observe(this, weatherEntry -> {
                Log.d(TAG, "DB update from LiveData in ViewModel");
                bindViews(weatherEntry);
            });
        }
    }

    private void bindViews(WeatherEntry weatherEntry) {

        /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
        int weatherId = weatherEntry.getWeatherId();
        /* Use our utility method to determine the resource ID for the proper art */
        int weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);
        /* Set the resource ID on the icon to display the art */
        mDetailBinding.primaryInfo.ivWeatherIcon.setImageResource(weatherImageId);

        /* Get user friendly date text and set */
        String dateText = SunshineDateUtils.getFriendlyDateString(this, weatherEntry.getDate(), true);
        mDetailBinding.primaryInfo.tvDate.setText(dateText);

        // Weather description
        mDetailBinding.primaryInfo.tvWeatherDescription.setText(weatherEntry.getDescription());

        /* Format high/low temp and set accordingly */
        String highString = SunshineWeatherUtils.formatTemperature(this, weatherEntry.getMax());
        String lowString = SunshineWeatherUtils.formatTemperature(this, weatherEntry.getMin());
        mDetailBinding.primaryInfo.tvHighTemperature.setText(highString);
        mDetailBinding.primaryInfo.tvLowTemperature.setText(lowString);

        /* Format humidity and set */
        double humidity = weatherEntry.getHumidity();
        String humidityString = getString(R.string.format_humidity, humidity);
        mDetailBinding.extraDetails.tvHumidity.setText(humidityString);

        /* Wind speed and direction */
        float windDirection = (float) weatherEntry.getWindDirection();
        float windSpeed = (float) weatherEntry.getWindSpeed();
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        mDetailBinding.extraDetails.tvWindMeasurement.setText(windString);

        /* Pressure */
        String pressureString = getString(R.string.format_pressure, weatherEntry.getPressure());
        mDetailBinding.extraDetails.tvPressure.setText(pressureString);

        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, weatherEntry.getDescription(), highString, lowString);
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
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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
                .setText(mForecastSummary)
                .getIntent();

        // Make sure there is an app to handle this intent and start the intent
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }
}
