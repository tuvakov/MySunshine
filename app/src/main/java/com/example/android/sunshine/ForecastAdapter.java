/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import java.util.List;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts to a
 * {@link android.support.v7.widget.RecyclerView}
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private List<WeatherEntry> mWeatherData;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final ForecastAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface ForecastAdapterOnClickHandler {
        void onClick(int weatherEntryId);
    }

    /**
     * Creates a ForecastAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public ForecastAdapter(ForecastAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        /* Instances for each text view and icon image view */
        final TextView tvDate;
        final TextView tvDescription;
        final TextView tvHigh;
        final TextView tvLow;
        final ImageView ivWeatherIconView;

        public ForecastAdapterViewHolder(View view) {
            super(view);

            /* Find views */
            tvDate = (TextView) view.findViewById(R.id.tv_date);
            tvDescription = (TextView) view.findViewById(R.id.tv_weather_description);
            tvHigh = (TextView) view.findViewById(R.id.tv_high_temperature);
            tvLow = (TextView) view.findViewById(R.id.tv_low_temperature);
            ivWeatherIconView = (ImageView) view.findViewById(R.id.iv_weather_icon);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            // Pass the clicked weather entry's DB id.
            mClickHandler.onClick(mWeatherData.get(adapterPosition).getId());
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.forecast_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ForecastAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        Context context = forecastAdapterViewHolder.ivWeatherIconView.getContext();

        /* Prepare data to show in MainActivity UI */
        WeatherEntry weatherEntry = mWeatherData.get(position);

        /* Get human readable string using our utility method */
        String dateString = SunshineDateUtils.getFriendlyDateString(context, weatherEntry.getDate(), false);

        /* Get icon id and set */
        int weatherImageId = SunshineWeatherUtils
                .getSmallArtResourceIdForWeatherCondition(weatherEntry.getWeatherId());
        forecastAdapterViewHolder.ivWeatherIconView.setImageResource(weatherImageId);

        /* Format temperatures */
        String highString = SunshineWeatherUtils.formatTemperature(context, weatherEntry.getMax());
        String lowString = SunshineWeatherUtils.formatTemperature(context, weatherEntry.getMin());

        /* Set TextViews */
        forecastAdapterViewHolder.tvDate.setText(dateString);
        forecastAdapterViewHolder.tvDescription.setText(weatherEntry.getDescription());
        forecastAdapterViewHolder.tvHigh.setText(highString);
        forecastAdapterViewHolder.tvLow.setText(lowString);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mWeatherData) return 0;
        return mWeatherData.size();
    }

    /**
     * This method is used to set the weather forecast on a ForecastAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new ForecastAdapter to display it.
     *
     * @param weatherData The new weather data to be displayed.
     */
    public void setWeatherData(List<WeatherEntry> weatherData) {
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }

    /* Gets a single WeatherEntry object and makes a String for it */
    private String prepareWeatherData(Context context, WeatherEntry entry){
        String date;
        String highAndLow;

        highAndLow = SunshineWeatherUtils.formatHighLows(context, entry.getMax(), entry.getMin());
        date = SunshineDateUtils.getFriendlyDateString(context, entry.getDate(), false);
        return date + " - " + entry.getDescription() + " - " + highAndLow;
    }

    /* Gets WeatherEntry objects and makes a String for each entry */
    private String[] prepareWeatherDataSet(Context context, List<WeatherEntry> entries){

        String[] data = new String[entries.size()];
        String date;
        String highAndLow;

        int i = 0;
        for (WeatherEntry entry: entries) {
            highAndLow = SunshineWeatherUtils.formatHighLows(context, entry.getMax(), entry.getMin());
            date = SunshineDateUtils.getFriendlyDateString(context, entry.getDate(), false);
            data[i++] = date + " - " + entry.getDescription() + " - " + highAndLow;
        }

        return data;
    }
}