package ir.shariaty.mytriplist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemTaskActivity extends AppCompatActivity {

    private EditText tripTitleEditText, startDateEditText, durationEditText, travelersEditText, alarmTimeEditText;
    private ImageView selectedImageView;
    private Button saveTaskButton, selectImageButton;

    private FirebaseStorage storage;
    private Uri imageUri;
    private int selectedYear, selectedMonth, selectedDay;
    private String loadedImageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_task);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
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

        storage = FirebaseStorage.getInstance();

        // Populate fields if editing existing task
        String docId = getIntent().getStringExtra("documentId");
        if (docId != null) {
            tripTitleEditText.setText(getIntent().getStringExtra("taskTitle"));
            startDateEditText.setText(getIntent().getStringExtra("startDate"));
            durationEditText.setText(getIntent().getStringExtra("duration"));
            travelersEditText.setText(getIntent().getStringExtra("travelers"));
            loadedImageUrl = getIntent().getStringExtra("imageUrl");
            if (loadedImageUrl != null && !loadedImageUrl.isEmpty()) {
                Glide.with(this).load(loadedImageUrl).into(selectedImageView);
            }
        }

        startDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            selectedYear = calendar.get(Calendar.YEAR);
            selectedMonth = calendar.get(Calendar.MONTH);
            selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ItemTaskActivity.this,
                    R.style.CustomDatePickerDialogTheme,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;
                        startDateEditText.setText(String.format("%d-%d-%d", selectedDay, selectedMonth + 1, selectedYear));
                    },
                    selectedYear, selectedMonth, selectedDay
            );
            datePickerDialog.show();
        });

        selectImageButton.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        alarmTimeEditText.setOnClickListener(v -> showTimePickerDialog());

        saveTaskButton.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                saveTaskToFirestore(loadedImageUrl);
            }

            String alarmTime = alarmTimeEditText.getText().toString();
            if (!alarmTime.isEmpty()) {
                setAlarm(alarmTime);
            }

            Intent intent = new Intent(ItemTaskActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                R.style.CustomTimePickerDialogTheme,
                (view, hourOfDay, minuteOfHour) -> {
                    alarmTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minuteOfHour));
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference fileRef = storageRef.child("images/" + UUID.randomUUID().toString() + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // بعد از موفقیت‌آمیز بودن آپلود، URL تصویر را دریافت کرده و ذخیره می‌کنیم
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveTaskToFirestoreWithImage(uri.toString()); // تغییر نام متد
                        });
                    })
                    .addOnFailureListener(e -> {
                        // اگر آپلود ناموفق بود، پیغام خطا نمایش داده می‌شود
                        Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        saveTaskToFirestoreWithImage(loadedImageUrl); // ذخیره بدون تصویر
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // تغییر نام متد اصلی برای ذخیره تسک در Firestore
    private void saveTaskToFirestoreWithImage(String imageUrl) {
        String title = tripTitleEditText.getText().toString();
        String startDate = startDateEditText.getText().toString();
        String duration = durationEditText.getText().toString();
        String travelers = travelersEditText.getText().toString();

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", title);
        taskMap.put("startDate", startDate);
        taskMap.put("duration", duration);
        taskMap.put("travelers", travelers);
        taskMap.put("imageUrl", imageUrl != null ? imageUrl : ""); // ذخیره URL تصویر

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tasks").add(taskMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // نمایش پیغام موفقیت
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveTaskToFirestore(String imageUrl) {
        String title = tripTitleEditText.getText().toString();
        String startDate = startDateEditText.getText().toString();
        String duration = durationEditText.getText().toString();
        String travelers = travelersEditText.getText().toString();

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", title);
        taskMap.put("startDate", startDate);
        taskMap.put("duration", duration);
        taskMap.put("travelers", travelers);
        taskMap.put("imageUrl", imageUrl != null ? imageUrl : "");  // اگر تصویر نبود، رشته خالی ذخیره شود

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("documentId")) {
            String docId = getIntent().getStringExtra("documentId");
            db.collection("tasks").document(docId)
                    .update(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("tasks").add(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void setAlarm(String alarmTime) {
        String[] timeParts = alarmTime.split(":");
        if (timeParts.length == 2) {
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, selectedYear);
            calendar.set(Calendar.MONTH, selectedMonth);
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // آلارم را در زمان دقیق سفر تنظیم می‌کنیم
            if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, AlarmReceiver.class);
                intent.putExtra("tripTitle", tripTitleEditText.getText().toString());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Toast.makeText(this, "Alarm set for trip time", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "آلارم برای زمانی در گذشته است. لطفاً زمان معتبر انتخاب کن", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(selectedImageView);
        }
    }
}