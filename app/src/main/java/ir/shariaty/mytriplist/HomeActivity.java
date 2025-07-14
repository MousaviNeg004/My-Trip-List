package ir.shariaty.mytriplist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ToDoListAdapter adapter;
    private List<ToDoList> todoList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        todoList = new ArrayList<>();

        // Setup Adapter
        adapter = new ToDoListAdapter(todoList, new ToDoListAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteTaskFromFirebase(position);
            }

            @Override
            public void onEditClick(int position) {
                editTask(position);
            }
        });

        recyclerView.setAdapter(adapter);

        // Add new task button
        FloatingActionButton addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ItemTaskActivity.class);
            startActivityForResult(intent, 1);
        });

        // Fetch tasks from Firebase
        fetchTasksFromFirebase();
    }

    // Fetch tasks from Firebase and display in RecyclerView
    private void fetchTasksFromFirebase() {
        db.collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todoList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();  // Get documentId
                        String title = document.getString("title");
                        String startDate = document.getString("startDate");
                        String endDate = document.getString("endDate");
                        String imageUrl = document.getString("imageUrl");
                        String duration = document.getString("duration");
                        String travelers = document.getString("travelers");

                        todoList.add(new ToDoList(id, title, startDate, endDate, imageUrl, duration, travelers));
                    }
                    adapter.notifyDataSetChanged(); // Notify the adapter to update the list
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this, "Error fetching tasks", Toast.LENGTH_SHORT).show()
                );
    }

    // Delete task from Firebase
    private void deleteTaskFromFirebase(int position) {
        ToDoList task = todoList.get(position);
        String docId = task.getDocumentId();  // Get documentId

        db.collection("tasks").document(docId)  // Delete based on documentId
                .delete()
                .addOnSuccessListener(aVoid -> {
                    todoList.remove(position);  // Remove the item from the list
                    adapter.notifyItemRemoved(position);  // Update RecyclerView
                    Toast.makeText(HomeActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show()
                );
    }

    // Edit task
    private void editTask(int position) {
        ToDoList task = todoList.get(position);
        Intent intent = new Intent(HomeActivity.this, ItemTaskActivity.class);
        intent.putExtra("documentId", task.getDocumentId());  // Send documentId
        intent.putExtra("taskTitle", task.getTitle());
        intent.putExtra("startDate", task.getStartDate());
        intent.putExtra("endDate", task.getEndDate());
        intent.putExtra("duration", task.getDuration());
        intent.putExtra("travelers", task.getTravelers());
        intent.putExtra("imageUrl", task.getImageUrl());
        startActivityForResult(intent, 2);  // Use requestCode for edit
    }

    // Handle result after adding or editing task
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == 1 || requestCode == 2)) {
            fetchTasksFromFirebase();  // Refresh the list after adding or editing
        }
    }
}
