package com.ayzconsultores.diegoshouse.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.ayzconsultores.diegoshouse.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.ayzconsultores.diegoshouse";
    private static final String CHANNEL_NAME  = "Diego's House";

    private NotificationManager manager;


    public NotificationHelper(Context context) {
        super(context);
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        );
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.GRAY);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getNotificationManager().createNotificationChannel(channel);
    }

    public NotificationManager getNotificationManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager!= null) {
                createChannel();
            }
        }
        return manager;
    }

    public NotificationCompat.Builder getNotification(String titulo, String body){
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(Color.GRAY)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(titulo).setBigContentTitle(body));
    }
}
