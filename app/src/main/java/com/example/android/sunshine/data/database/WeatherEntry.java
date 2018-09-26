package com.example.android.sunshine.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;


/**
 * Table schema for a single weather forecast (per day)
 */

@Entity(tableName = "weather", indices = {@Index(value = {"date"}, unique = true)})
public class WeatherEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private long date;
    @ColumnInfo(name = "weather_id")
    private int weatherId;
    private String description;
    private double min;
    private double max;
    private double humidity;
    private double pressure;
    @ColumnInfo(name = "wind")
    private double windSpeed;


    @ColumnInfo(name = "degrees")
    private double windDirection;


    /* Constructors */
    /**
     * This constructor is used by OpenWeatherJsonParser. When the network fetch has JSON data, it
     * converts this data to WeatherEntry objects using this constructor.
     * @param date Date of weather
     * @param weatherId Image id for weather
     * @param min Min temperature
     * @param max Max temperature
     * @param humidity Humidity for the day
     * @param pressure Barometric pressure
     * @param windSpeed Wind speed
     * @param windDirection Wind direction
     */
    @Ignore
    public WeatherEntry(long date, int weatherId, String description, double min, double max, double humidity,
                        double pressure, double windSpeed, double windDirection){

        this.date = date;
        this.weatherId = weatherId;
        this.description = description;
        this.min = min;
        this.max = max;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;

    }

    // This one is used by Room
    public WeatherEntry(int id, long date, int weatherId, String description, double min, double max, double humidity,
                        double pressure, double windSpeed, double windDirection){

        this.id = id;
        this.date = date;
        this.weatherId = weatherId;
        this.description = description;
        this.min = min;
        this.max = max;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;

    }

    /* Getters and setters for the fields */
    public int getId() { return id; }
    public void setId(int id) {this.id = id; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public int getWeatherId() {return weatherId; }
    public void setWeatherId(int weatherId) {this.weatherId = weatherId; }

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }

    public double getMax() {   return max; }
    public void setMax(double max) {    this.max = max; }

    public double getHumidity() {return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public double getPressure() {return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }

    public double getWindSpeed() {   return windSpeed; }
    public void setWindSpeed(double windSpeed) {    this.windSpeed = windSpeed; }

    public double getWindDirection() {    return windDirection; }
    public void setWindDirection(double windDirection) {    this.windDirection = windDirection; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("date " + getDate()
                + ", weatherId " + getWeatherId()
                + ", min " + getMin()
                + ", max " + getMax()
                + ", humidity " + getHumidity()
                + ", pressure " + getPressure()
                + ", windSpeed " + getWindSpeed()
                + ", windDirection " + getWindDirection()
                + ", description " + getDescription() + "\n");

        return builder.toString();
    }
}
