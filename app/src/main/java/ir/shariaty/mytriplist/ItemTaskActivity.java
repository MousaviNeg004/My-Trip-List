package ir.shariaty.mytriplist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemTaskActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 100;
    private static final int REQ_READ_MEDIA = 101;

    private EditText tripTitleEditText, startDateEditText, durationEditText, travelersEditText, alarmTimeEditText;
    private ImageView selectedImageView;
    private Button saveTaskButton, selectImageButton;

    private Uri imageUri;
    private String loadedImageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_task);

        // نوتیفیکیشن برای اندروید 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 201);
            }
        }

        tripTitleEditText = findViewById(R.id.tripTitle);
        startDateEditText = findViewById(R.id.startDate);
        durationEditText = findViewById(R.id.duration);
        travelersEditText = findViewById(R.id.travelers);
        alarmTimeEditText = findViewById(R.id.alarmTime);
        selectedImageView = findViewById(R.id.selectedImageView);
        saveTaskButton = findViewById(R.id.saveTaskButton);
        selectImageButton = findViewById(R.id.selectImageButton);

        // اگر در حالت ادیت هستیم
        String docId = getIntent().getStringExtra("documentId");
        if (docId != null) {
            tripTitleEditText.setText(getIntent().getStringExtra("taskTitle"));
            startDateEditText.setText(getIntent().getStringExtra("startDate"));
            durationEditText.setText(getIntent().getStringExtra("duration"));
            travelersEditText.setText(getIntent().getStringExtra("travelers"));
            loadedImageUrl = getIntent().getStringExtra("imageUrl");

            if (loadedImageUrl != null && !loadedImageUrl.isEmpty()) {
                // هم لینک http و هم مسیر لوکال را پشتیبانی کن
                if (loadedImageUrl.startsWith("http")) {
                    Glide.with(this).load(loadedImageUrl).into(selectedImageView);
                } else {
                    Glide.with(this).load(new File(loadedImageUrl)).into(selectedImageView);
                }
            }
        }

        startDateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH);
            int d = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(
                    ItemTaskActivity.this,
                    R.style.CustomDatePickerDialogTheme,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        startDateEditText.setText(String.format("%d-%d-%d", dayOfMonth, monthOfYear + 1, year));
                    }, y, m, d
            );
            dp.show();
        });

        selectImageButton.setOnClickListener(v -> pickImageWithPermission());

        alarmTimeEditText.setOnClickListener(v -> showTimePickerDialog());

        saveTaskButton.setOnClickListener(v -> {
            // 1) تصویر را اگر انتخاب شده، به حافظه داخلی کپی کن و مسیرش را بگیر
            String imagePathToSave = loadedImageUrl; // پیش‌فرض اگر عکسی جدید انتخاب نشده
            if (imageUri != null) {
                String savedPath = saveImageToInternalStorage(imageUri);
                if (savedPath != null) {
                    imagePathToSave = savedPath;
                } else {
                    Toast.makeText(this, "ذخیره تصویر در حافظه داخلی ناموفق بود", Toast.LENGTH_LONG).show();
                }
            }

            // 2) ذخیره سند در Firestore (به‌همراه مسیر لوکال)
            saveTaskToFirestore(imagePathToSave);

            // 3) اگر ساعت آلارم وارد شده، آلارم تنظیم شود
            String alarmTime = alarmTimeEditText.getText().toString().trim();
            if (!alarmTime.isEmpty()) {
                setAlarm(alarmTime);
            }

            // برگشت به خانه
            Intent intent = new Intent(ItemTaskActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void pickImageWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQ_READ_MEDIA);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_READ_MEDIA);
                return;
            }
        }
        openGallery();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                R.style.CustomTimePickerDialogTheme,
                (view, hourOfDay, minuteOfHour) -> alarmTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minuteOfHour)),
                hour, minute, true
        );
        timePickerDialog.show();
    }

    /**
     * کپی کردن عکس انتخابی به حافظۀ داخلی app و برگرداندن مسیر فایل
     */
    private String saveImageToInternalStorage(Uri src) {
        try {
            // تعیین پسوند از روی mimeType
            String mime = getContentResolver().getType(src);
            String ext = "jpg";
            if (mime != null) {
                String guess = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
                if (guess != null) ext = guess;
            }

            String fileName = "trip_" + UUID.randomUUID() + "." + ext;
            File outFile = new File(getFilesDir(), fileName);

            try (InputStream in = getContentResolver().openInputStream(src);
                 OutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                out.flush();
            }

            // برای نمایش فوری در UI
            Glide.with(this).load(outFile).into(selectedImageView);

            return outFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("ItemTaskActivity", "saveImageToInternalStorage error", e);
            return null;
        }
    }

    /**
     * ذخیره سند در Firestore (با مسیر لوکال تصویر)
     */
    private void saveTaskToFirestore(String imagePath) {
        String title = tripTitleEditText.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String duration = durationEditText.getText().toString().trim();
        String travelers = travelersEditText.getText().toString().trim();

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", title);
        taskMap.put("startDate", startDate);
        taskMap.put("duration", duration);
        taskMap.put("travelers", travelers);
        taskMap.put("imageUrl", imagePath != null ? imagePath : "");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("documentId")) {
            String docId = getIntent().getStringExtra("documentId");
            db.collection("tasks").document(docId)
                    .update(taskMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("tasks").add(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * آلارم دقیق زمان سفر. تاریخ را از متن startDate می‌خوانیم تا مشکل «گذشته» پیش نیاید.
     * فرمت تاریخ: d-M-yyyy   (مثل 5-8-2025)
     */
    private void setAlarm(String alarmTime) {
        try {
            String[] dateParts = startDateEditText.getText().toString().trim().split("-");
            if (dateParts.length != 3) {
                Toast.makeText(this, "فرمت تاریخ درست نیست", Toast.LENGTH_LONG).show();
                return;
            }
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Calendar: 0-based
            int year = Integer.parseInt(dateParts[2]);

            String[] timeParts = alarmTime.split(":");
            if (timeParts.length != 2) {
                Toast.makeText(this, "فرمت ساعت درست نیست", Toast.LENGTH_LONG).show();
                return;
            }
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long triggerAt = calendar.getTimeInMillis();
            if (triggerAt <= System.currentTimeMillis()) {
                Toast.makeText(this, "آلارم برای زمانی در گذشته است. لطفاً زمان معتبر انتخاب کن", Toast.LENGTH_LONG).show();
                return;
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("tripTitle", tripTitleEditText.getText().toString());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                Toast.makeText(this, "Alarm set for trip time", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ItemTaskActivity", "setAlarm error", e);
            Toast.makeText(this, "خطا در تنظیم آلارم", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(selectedImageView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_READ_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "اجازۀ دسترسی به تصاویر داده نشد", Toast.LENGTH_LONG).show();
            }
        }
    }
}