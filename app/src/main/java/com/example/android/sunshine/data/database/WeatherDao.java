package com.example.android.sunshine.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface WeatherDao {

    /**
     * Selects all {@link WeatherEntry} entries after a give date, inclusive. The LiveData will
     * be kept in sync with the database, so that it will automatically notify observers when the
     * values in the table change.
     *
     * @param date A {@link long} from which to select all future weather
     * @return List of all {@link WeatherEntry} objects after date
     */
    @Query("SELECT * FROM weather WHERE date >= :date")
    LiveData<List<WeatherEntry>> getWeatherForecasts(long date);


    /**
     * Gets the weather for a single day
     *
     * @param date The date you want weather for
     * @return {@link WeatherEntry} with weather for a single day
     */
    @Query("SELECT * FROM weather WHERE date = :date")
    WeatherEntry getWeatherByDate(long date);

    /**
     * Gets the weather for a single day
     *
     * @param id The id you want weather for
     * @return {@link WeatherEntry} with weather for a single day
     */
    @Query("SELECT * FROM weather WHERE id = :id")
    LiveData<WeatherEntry> getWeatherById(int id);

    /**
     * Inserts a list of {@link WeatherEntry} into the weather table. If there is a conflicting id
     * or date the weather entry uses the {@link OnConflictStrategy} of replacing the weather
     * forecast. The required uniqueness of these values is defined in the {@link WeatherEntry}.
     *
     * @param weather A list of weather forecasts to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(WeatherEntry... weather);

    /**
     * Deletes any weather data older than the given day
     *
     * @param date The date to delete all prior weather from (exclusive)
     */
    @Query("DELETE FROM weather WHERE date < :date")
    void deleteOldWeather(long date);

    /**
     * Selects all ids entries after a give date, inclusive. This is for easily seeing
     * what entries are in the database without pulling all of the data.
     *
     * @return Number of future weather forecasts stored in the database
     */
    @Query("SELECT COUNT(id) FROM weather")
    int countAll();

    /**
     * Selects all ids entries after a give date, inclusive. This is for easily seeing
     * what entries are in the database without pulling all of the data.
     *
     * @param date The date to select after (inclusive)
     * @return Number of future weather forecasts stored in the database
     */
    @Query("SELECT COUNT(id) FROM weather WHERE date >= :date")
    int countAllFutureWeather(long date);

}