package ir.shariaty.mytriplist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemTaskActivity extends AppCompatActivity {

    private EditText tripTitleEditText, startDateEditText, durationEditText, travelersEditText;
    private ImageView selectedImageView;
    private Button saveTaskButton, selectImageButton;

    private FirebaseStorage storage;
    private Uri imageUri;
    private int selectedYear, selectedMonth, selectedDay;

    // List to hold dynamically added EditText views
    private List<EditText> newEditTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_task);

        // Initialize views
        tripTitleEditText = findViewById(R.id.tripTitle);
        startDateEditText = findViewById(R.id.startDate);
        durationEditText = findViewById(R.id.duration);
        travelersEditText = findViewById(R.id.travelers);
        selectedImageView = findViewById(R.id.selectedImageView);
        saveTaskButton = findViewById(R.id.saveTaskButton);
        selectImageButton = findViewById(R.id.selectImageButton);

        storage = FirebaseStorage.getInstance();

        // If editing an existing task, populate the fields with data
        String docId = getIntent().getStringExtra("documentId");
        if (docId != null) {
            tripTitleEditText.setText(getIntent().getStringExtra("taskTitle"));
            startDateEditText.setText(getIntent().getStringExtra("startDate"));
            durationEditText.setText(getIntent().getStringExtra("duration"));
            travelersEditText.setText(getIntent().getStringExtra("travelers"));
        }

        // DatePicker Dialog for selecting start date (for alarm)
        startDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            selectedYear = calendar.get(Calendar.YEAR);
            selectedMonth = calendar.get(Calendar.MONTH);
            selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ItemTaskActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;

                        // Set selected date to the EditText field
                        startDateEditText.setText(String.format("%d-%d-%d", selectedDay, selectedMonth + 1, selectedYear));
                    },
                    selectedYear, selectedMonth, selectedDay
            );
            datePickerDialog.show();
        });

        // Select image button
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        // Save task button
        saveTaskButton.setOnClickListener(v -> {
            // Gather all the data and save to Firestore
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                saveTaskToFirestore(null);
            }
            Intent intent = new Intent(ItemTaskActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Method to upload image to Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("images/" + UUID.randomUUID().toString());

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                                saveTaskToFirestore(uri.toString())))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image, saving without image", Toast.LENGTH_SHORT).show();
                    saveTaskToFirestore(null);
                });
    }

    // Method to save task data to Firestore
    private void saveTaskToFirestore(String imageUrl) {
        // Get all entered data
        String title = tripTitleEditText.getText().toString();
        String startDate = startDateEditText.getText().toString();
        String duration = durationEditText.getText().toString();
        String travelers = travelersEditText.getText().toString();

        // Create a map to send data to Firestore
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", title);
        taskMap.put("startDate", startDate);
        taskMap.put("duration", duration);
        taskMap.put("travelers", travelers);
        taskMap.put("imageUrl", imageUrl != null ? imageUrl : "");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // If editing, update the existing task
        if (getIntent().hasExtra("documentId")) {
            String docId = getIntent().getStringExtra("documentId");
            db.collection("tasks").document(docId)
                    .update(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);  // Notify HomeActivity to refresh
                        finish();  // Return to HomeActivity
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show());
        } else {
            // Add new task
            db.collection("tasks").add(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);  // Notify HomeActivity to refresh
                        finish();  // Return to HomeActivity
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show());
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
