package ir.shariaty.mytriplist;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ایجاد لیست از وظایف (To-Do List)
        List<ToDoList> todoList = new ArrayList<>();
        todoList.add(new ToDoList("Buy groceries", "2025-06-12"));
        todoList.add(new ToDoList("Finish homework", "2025-06-13"));
        todoList.add(new ToDoList("Attend meeting", "2025-06-14"));

        // تنظیم RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ToDoListAdapter adapter = new ToDoListAdapter(todoList);
        recyclerView.setAdapter(adapter);
    }
}
