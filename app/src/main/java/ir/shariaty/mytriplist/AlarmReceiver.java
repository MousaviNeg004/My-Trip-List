package ir.shariaty.mytriplist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String tripTitle = intent.getStringExtra("tripTitle");
        if (tripTitle == null) tripTitle = "Trip";

        Intent overlay = new Intent(context, AlarmActivity.class);
        overlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        overlay.putExtra("tripTitle", tripTitle);
        context.startActivity(overlay);

        sendNotification(context, tripTitle);
    }

    private void sendNotification(Context context, String title) {
        String channelId = "default";
        String channelName = "Trip Notifications";

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(ch);
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle("Reminder: " + title)
                .setContentText("You have a trip tomorrow!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        nm.notify(1001, notification);
    }
}