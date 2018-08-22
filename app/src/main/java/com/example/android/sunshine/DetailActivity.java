package com.example.android.sunshine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        if(comingIntent.hasExtra(Intent.EXTRA_TEXT)){
            // Get the extra
            mWeatherText = comingIntent.getStringExtra(Intent.EXTRA_TEXT);
            // Set the weather text to the textview
            mWeatherTextView.setText(mWeatherText);
        }
    }
}
