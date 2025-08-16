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

import android.provider.Settings;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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

        String docId = getIntent().getStringExtra("documentId");
        if (docId != null) {
            tripTitleEditText.setText(getIntent().getStringExtra("taskTitle"));
            startDateEditText.setText(getIntent().getStringExtra("startDate"));
            durationEditText.setText(getIntent().getStringExtra("duration"));
            travelersEditText.setText(getIntent().getStringExtra("travelers"));
            loadedImageUrl = getIntent().getStringExtra("imageUrl");

            if (loadedImageUrl != null && !loadedImageUrl.isEmpty()) {
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
                    (view, year, monthOfYear, dayOfMonth) ->
                            startDateEditText.setText(String.format("%d-%d-%d", dayOfMonth, monthOfYear + 1, year)),
                    y, m, d
            );
            dp.show();
        });

        selectImageButton.setOnClickListener(v -> pickImageWithPermission());
        alarmTimeEditText.setOnClickListener(v -> showTimePickerDialog());

        saveTaskButton.setOnClickListener(v -> {
            String imagePathToSave = loadedImageUrl;
            if (imageUri != null) {
                String savedPath = saveImageToInternalStorage(imageUri);
                if (savedPath != null) imagePathToSave = savedPath;
            }

            saveTaskToFirestore(imagePathToSave);

            String alarmTime = alarmTimeEditText.getText().toString().trim();
            if (!alarmTime.isEmpty()) {
                // آلارم دقیقا یک روز قبل
                setAlarmOneDayBefore(alarmTime);
            }

            startActivity(new Intent(ItemTaskActivity.this, HomeActivity.class));
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
                (view, hourOfDay, minuteOfHour) ->
                        alarmTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minuteOfHour)),
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private String saveImageToInternalStorage(Uri src) {
        try {
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
            Glide.with(this).load(outFile).into(selectedImageView);
            return outFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("ItemTaskActivity", "saveImageToInternalStorage error", e);
            return null;
        }
    }

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
                    .addOnSuccessListener(aVoid -> setResult(RESULT_OK))
                    .addOnFailureListener(e -> {});
        } else {
            db.collection("tasks").add(taskMap)
                    .addOnSuccessListener(documentReference -> setResult(RESULT_OK))
                    .addOnFailureListener(e -> {});
        }
    }
    // ------------------------------------------------------


    private boolean ensureExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
                Toast.makeText(this, "لطفاً در صفحه‌ی بعد «Allow exact alarms» را روشن کن و بعد دوباره ذخیره کن.", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }


    private void setAlarmOneDayBefore(String alarmTime) {
        try {
            String dateStr = startDateEditText.getText().toString().trim();
            if (dateStr.isEmpty()) {
                Toast.makeText(this, "تاریخ شروع خالی است.", Toast.LENGTH_LONG).show();
                return;
            }

            DateTimeFormatter df = DateTimeFormatter.ofPattern("d-M-uuuu");
            LocalDate date = LocalDate.parse(dateStr, df);

            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime time = LocalTime.parse(alarmTime, tf);

            // زمان شروع سفر
            LocalDateTime tripDateTime = LocalDateTime.of(date, time);
            // یک روز قبل
            LocalDateTime alarmDateTime = tripDateTime.minusDays(1);

            long triggerAt = alarmDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            if (triggerAt <= System.currentTimeMillis()) {
                Toast.makeText(this, "آلارمِ یک روز قبل، از الان گذشته است.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!ensureExactAlarmPermission()) return;

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("tripTitle", tripTitleEditText.getText().toString());
            intent.putExtra("notifText", "You have a trip tomorrow!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                }Toast.makeText(this, "I’ve just set the alarm for one day before your trip.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ItemTaskActivity", "setAlarmOneDayBefore error", e);
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

