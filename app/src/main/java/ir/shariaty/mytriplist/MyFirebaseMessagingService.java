package ir.shariaty.mytriplist;

import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String startDate = remoteMessage.getData().get("startDate");

            saveTaskToFirestore(title, startDate);

            sendNotification(title);
        }
    }

    private void saveTaskToFirestore(String title, String startDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", title);
        taskMap.put("startDate", startDate);
        taskMap.put("duration", "");
        taskMap.put("travelers", "");
        taskMap.put("imageUrl", "");

        db.collection("tasks")
                .add(taskMap)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Task added successfully: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.out.println("Error adding task: " + e.getMessage()));
    }

    private void sendNotification(String title) {
        String channelId = "default";
        String channelName = "Trip Notifications";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("New Trip: " + title)
                .setContentText("You have a new trip to manage.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(0, notification);
    }
}
