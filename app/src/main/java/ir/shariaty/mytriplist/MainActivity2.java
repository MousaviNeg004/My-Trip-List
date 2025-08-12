package ir.shariaty.mytriplist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

public class MainActivity2 extends AppCompatActivity {

    private Button tripListButton, toDoListButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        setContentView(R.layout.activity_main2);

        tripListButton = findViewById(R.id.tripListButton);
        toDoListButton = findViewById(R.id.toDoListButton);
        logoutButton   = findViewById(R.id.logoutButton);

        tripListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, HomeActivity.class);
            startActivity(intent);
        });

        toDoListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, ToDoListActivity.class);
            startActivity(intent);
        });

        // رفتن به صفحه‌ی اصلی (MainActivity) هنگام LogOut
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent);
            // اگر نمی‌خواهی با دکمه Back برگردی به این صفحه، خط زیر را هم فعال کن:
            // finish();
        });
    }
}