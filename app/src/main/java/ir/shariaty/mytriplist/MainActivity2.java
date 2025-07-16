package ir.shariaty.mytriplist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity2 extends AppCompatActivity {

    private Button tripListButton, toDoListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // مقداردهی اولیه Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);  // مقداردهی اولیه Firebase
        }

        setContentView(R.layout.activity_main2);

        // Initialize buttons
        tripListButton = findViewById(R.id.tripListButton);
        toDoListButton = findViewById(R.id.toDoListButton);

        // Set listeners for buttons
        tripListButton.setOnClickListener(v -> {
            // Navigate to Home Activity (activity_home.xml)
            Intent intent = new Intent(MainActivity2.this, HomeActivity.class);
            startActivity(intent);
        });

        toDoListButton.setOnClickListener(v -> {
            // Navigate to ToDo List Activity
            Intent intent = new Intent(MainActivity2.this, ToDoListActivity.class);
            startActivity(intent);
        });
    }
}
