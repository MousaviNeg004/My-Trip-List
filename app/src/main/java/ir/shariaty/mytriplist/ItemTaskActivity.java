package ir.shariaty.mytriplist;

import android.app.DatePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_task);

        tripTitleEditText = findViewById(R.id.tripTitle);
        startDateEditText = findViewById(R.id.startDate);
        durationEditText = findViewById(R.id.duration);
        travelersEditText = findViewById(R.id.travelers);
        alarmTimeEditText = findViewById(R.id.alarmTime);
        selectedImageView = findViewById(R.id.selectedImageView);
        saveTaskButton = findViewById(R.id.saveTaskButton);
        selectImageButton = findViewById(R.id.selectImageButton);

        storage = FirebaseStorage.getInstance();

        //if editing an existing task, populate the fields with data
        String docId = getIntent().getStringExtra("documentId");
        if (docId != null) {
            tripTitleEditText.setText(getIntent().getStringExtra("taskTitle"));
            startDateEditText.setText(getIntent().getStringExtra("startDate"));
            durationEditText.setText(getIntent().getStringExtra("duration"));
            travelersEditText.setText(getIntent().getStringExtra("travelers"));
        }

        //DatePicker Dialog for selecting start date
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

        //select image button
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        //show TimePickerDialog for alarm time
        alarmTimeEditText.setOnClickListener(v -> showTimePickerDialog());

        //save task button
        saveTaskButton.setOnClickListener(v -> {
            // Gather all the data and save to Firestore
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                saveTaskToFirestore(null);
            }

            //get the alarm time
            String alarmTime = alarmTimeEditText.getText().toString();
            if (!alarmTime.isEmpty()) {
                setAlarm(alarmTime);
            }

            Intent intent = new Intent(ItemTaskActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    //method to show TimePickerDialog for selecting alarm time
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

    //method to upload image to Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageRef.child("images/" + UUID.randomUUID().toString());

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> saveTaskToFirestore(uri.toString())))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image, saving without image", Toast.LENGTH_SHORT).show();
                    saveTaskToFirestore(null);
                });
    }

    //method to save or update task data in Firestore
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
        taskMap.put("imageUrl", imageUrl != null ? imageUrl : "");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //check if we are editing an existing task
        if (getIntent().hasExtra("documentId")) {
            //if documentId is present, update the existing task
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
            //if no documentId is found, it's a new task
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

    //method to set alarm for the day before the trip at the specified time
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
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);

            //send trip title to the AlarmReceiver
            intent.putExtra("tripTitle", tripTitleEditText.getText().toString());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Alarm set for " + alarmTime, Toast.LENGTH_SHORT).show();
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
