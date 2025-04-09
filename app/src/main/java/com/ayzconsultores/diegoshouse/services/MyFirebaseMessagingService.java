package com.ayzconsultores.diegoshouse.services;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.ayzconsultores.diegoshouse.channel.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            if (title != null && body != null) {
                ShowNotification(title, body);
            }
        }

        Map<String, String> data = message.getData();
        String title = data.get("title");
        String body = data.get("body");

        if (title != null && body != null) {
            ShowNotification(title, body);
        }
    }


    private void ShowNotification(String title, String body){
        NotificationHelper helper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = helper.getNotification(title, body);
        helper.getNotificationManager().notify(1, builder.build());
    }
}
