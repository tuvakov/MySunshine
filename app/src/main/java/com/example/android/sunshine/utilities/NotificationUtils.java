package com.example.android.sunshine.utilities;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.android.sunshine.DetailActivity;
import com.example.android.sunshine.R;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.database.AppDatabase;
import com.example.android.sunshine.data.database.WeatherEntry;

public class NotificationUtils {

    private static final String FORECAST_NOTIFICATION_CHANNEL_ID = "forecast-noti-channel";
    private static final int FORECAST_NOTIFICATION_ID = 454;

    /**
     * Constructs and displays a notification for the newly updated weather for today.
     *
     * @param context Context used to query our ContentProvider and use various Utility methods
     */
    public static void notifyUserOfNewWeather(final Context context) {

        long today = SunshineDateUtils.normalizeDate(System.currentTimeMillis());
        WeatherEntry todayWeather = AppDatabase.getsInstance(context).weatherDao().getWeatherByDate(today);

        /*
         * If our cursor is not null, we want to show the notification.
         */
        if (todayWeather != null) {

            /* Weather ID as returned by API, used to identify the icon to be used */
            int weatherId = todayWeather.getWeatherId();
            double high = todayWeather.getMax();
            double low = todayWeather.getMin();

            Resources resources = context.getResources();
            int largeArtResourceId = SunshineWeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId);

            String notificationTitle = context.getString(R.string.app_name);

            final String notificationText = getNotificationText(context, weatherId, high, low);

            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            int smallArtResourceId = SunshineWeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId);

            /* Use NotificationCompat.Builder to begin building the notification */
            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(
                    context, FORECAST_NOTIFICATION_CHANNEL_ID);
            notiBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                    .setAutoCancel(true);

            /* Create an Intent with the weather entry id to start the DetailActivity */
            Intent detailIntentForToday = new Intent(context, DetailActivity.class);
            detailIntentForToday.putExtra(DetailActivity.INTENT_ID_KEY, todayWeather.getId());

            /* Use TaskStackBuilder to create the proper PendingIntent */
            TaskStackBuilder taskStackBuilder;
            PendingIntent resultPendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                taskStackBuilder = TaskStackBuilder.create(context);
                taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
                resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                // Set the content Intent of the NotificationBuilder
                notiBuilder.setContentIntent(resultPendingIntent);
            }

            /* Get reference to NotificationManager */
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

            /* Create a notification channel for Android O devices */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        FORECAST_NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.main_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            /* If the build version is greater than JELLY_BEAN and lower than OREO,
             * set the notification's priority to PRIORITY_HIGH.
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                notiBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }

            // Notify the user with the ID WEATHER_NOTIFICATION_ID
            notificationManager.notify(FORECAST_NOTIFICATION_ID, notiBuilder.build());

            // Save the time at which the notification occurred using SunshinePreferences
            SunshinePreferences.saveLastNotificationTime(context, System.currentTimeMillis());
        }
    }

    /**
     * Constructs and returns the summary of a particular day's forecast using various utility
     * methods and resources for formatting. This method is only used to create the text for the
     * notification that appears when the weather is refreshed.
     * <p>
     * The String returned from this method will look something like this:
     * <p>
     * Forecast: Sunny - High: 14°C Low 7°C
     *
     * @param context   Used to access utility methods and resources
     * @param weatherId ID as determined by Open Weather Map
     * @param high      High temperature (either celsius or fahrenheit depending on preferences)
     * @param low       Low temperature (either celsius or fahrenheit depending on preferences)
     * @return Summary of a particular day's forecast
     */
    private static String getNotificationText(Context context, int weatherId, double high, double low) {

        /*
         * Short description of the weather, as provided by the API.
         * e.g "clear" vs "sky is clear".
         */
        String shortDescription = SunshineWeatherUtils
                .getStringForWeatherCondition(context, weatherId);

        String notificationFormat = context.getString(R.string.format_notification);

        /* Using String's format method, we create the forecast summary */
        String notificationText = String.format(notificationFormat,
                shortDescription,
                SunshineWeatherUtils.formatTemperature(context, high),
                SunshineWeatherUtils.formatTemperature(context, low));

        return notificationText;
    }
}
