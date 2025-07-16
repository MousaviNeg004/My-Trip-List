package ir.shariaty.mytriplist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;

public class ToDoListActivity extends AppCompatActivity {

    private EditText newItemEditText;
    private Button addItemButton;
    private LinearLayout itemsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        // Initialize views
        newItemEditText = findViewById(R.id.newItemEditText);
        addItemButton = findViewById(R.id.addItemButton);
        itemsLayout = findViewById(R.id.itemsLayout);

        // Set font for the EditText
        newItemEditText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/vazir.ttf"));

        // Add item button listener
        addItemButton.setOnClickListener(v -> {
            String itemText = newItemEditText.getText().toString().trim();
            if (!itemText.isEmpty()) {
                addItemToList(itemText);
                newItemEditText.setText(""); // Clear the input field
            } else {
                Toast.makeText(ToDoListActivity.this, "Please enter an item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to add item to the list dynamically with a checkbox
    private void addItemToList(String itemText) {
        CheckBox newCheckBox = new CheckBox(this);
        newCheckBox.setText(itemText);

        // Set font to Vazir
        newCheckBox.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/vazir.ttf"));

        // Add the new checkbox to the layout
        itemsLayout.addView(newCheckBox);
    }
}
