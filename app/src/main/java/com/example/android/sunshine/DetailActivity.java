package com.example.android.sunshine;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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

    // String that holds weather data
    private String mWeatherText;
    // String constant for Intent key
    public static final String INTENT_ID_KEY = "weatherEntryId";
    private final int DEFAULT_WEATHER_ENTRY_ID = 0;
    private AppDatabase mDb;
    // Textview reference that displays weather data
    private TextView mWeatherTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Instantiate db
        mDb = AppDatabase.getsInstance(getApplication().getBaseContext());

        // Set the textview
        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_detail);

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

            weather.observe(this, new Observer<WeatherEntry>() {
                @Override
                public void onChanged(@Nullable WeatherEntry weatherEntry) {
                    Log.d(TAG, "DB update from LiveData in ViewModel");
                    // Set the weather text to the textview
                    /* TODO: This part is temporary
                    *        Gonna be updated later */
                    String date = SunshineDateUtils.getFriendlyDateString(DetailActivity.this,
                            weatherEntry.getDate(), false);
                    String highLow = SunshineWeatherUtils.formatHighLows(DetailActivity.this,
                            weatherEntry.getMin(), weatherEntry.getMax());
                    mWeatherText = date + " - " + weatherEntry.getDescription() + " - " + highLow;
                    mWeatherTextView.setText(mWeatherText);
                }
            });

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
                .setText(mWeatherText)
                .getIntent();

        // Make sure there is an app to handle this intent and start the intent
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }
}
