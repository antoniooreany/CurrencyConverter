package com.antoniooreany.currencyconverter.Update;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.antoniooreany.currencyconverter.Activities.MainActivity;

public class UpdateNotifier {
    private static final int NOTIFICATION_ID = 123;
    private static String CHANNEL_ID = "update_channel";
    private static String CHANNEL_DESCRIPTION = "Show update status";
    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    Context context;

    public UpdateNotifier(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = notificationManager.getNotificationChannel("update_channel");

            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_DESCRIPTION, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Currencies update...").setContentText("Currencies update in running").setAutoCancel(false);
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
        notificationBuilder.setContentIntent(resultPendingIntent);


    }

    public void showNotification() {
        notificationBuilder.setContentText("Exchange rates are up to date!");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void removeNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }


}
