package ir.shariaty.mytriplist;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ToDoListActivity extends AppCompatActivity {

    private EditText newItemEditText;
    private Button addItemButton, saveCheckedButton;
    private LinearLayout itemsLayout;
    private FirebaseFirestore db;
    private final List<CheckBox> allCheckBoxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        db = FirebaseFirestore.getInstance();

        newItemEditText = findViewById(R.id.newItemEditText);
        addItemButton = findViewById(R.id.addItemButton);
        saveCheckedButton = findViewById(R.id.saveCheckedButton);
        itemsLayout = findViewById(R.id.itemsLayout);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.vazir);
        newItemEditText.setTypeface(typeface);

        addItemButton.setOnClickListener(v -> {
            String itemText = newItemEditText.getText().toString().trim();
            if (!itemText.isEmpty()) {
                addItemToList(itemText, false);
                newItemEditText.setText("");
                saveItemToFirebase(itemText, false);
            } else {
                Toast.makeText(ToDoListActivity.this, "Please enter an item", Toast.LENGTH_SHORT).show();
            }
        });

        saveCheckedButton.setOnClickListener(v -> saveAllCheckedStates());

        loadItemsFromFirebase();
    }

    private void addItemToList(String itemText, boolean isChecked) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(10, 10, 10, 10);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        CheckBox newCheckBox = new CheckBox(this);
        newCheckBox.setText(itemText);
        newCheckBox.setChecked(isChecked);
        newCheckBox.setTypeface(ResourcesCompat.getFont(this, R.font.vazir));

        newCheckBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            updateItemInFirebase(itemText, isChecked1);
        });

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(R.drawable.ic_trash_icon);
        deleteButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 0, 0);
        deleteButton.setLayoutParams(params);

        deleteButton.setOnClickListener(v -> {
            deleteItemFromFirebase(itemText);
            itemsLayout.removeView(itemLayout);
            allCheckBoxes.remove(newCheckBox);
        });

        itemLayout.addView(newCheckBox);
        itemLayout.addView(deleteButton);
        itemsLayout.addView(itemLayout);

        allCheckBoxes.add(newCheckBox);
    }

    private void saveItemToFirebase(String itemText, boolean isChecked) {
        ToDoItem newItem = new ToDoItem(itemText, isChecked);
        db.collection("todoItems")
                .add(newItem)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ToDoListActivity.this, "Item added to Firestore", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ToDoListActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Add error", e);
                });
    }

    private void updateItemInFirebase(String itemText, boolean isChecked) {
        db.collection("todoItems")
                .whereEqualTo("itemText", itemText)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("isChecked", isChecked);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ToDoListActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Update error", e);
                });
    }

    private void deleteItemFromFirebase(String itemText) {
        db.collection("todoItems")
                .whereEqualTo("itemText", itemText)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ToDoListActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ToDoListActivity.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                                    Log.e("Firestore", "Delete error", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ToDoListActivity.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Delete query error", e);
                });
    }

    private void loadItemsFromFirebase() {
        db.collection("todoItems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        itemsLayout.removeAllViews();
                        allCheckBoxes.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ToDoItem item = document.toObject(ToDoItem.class);
                            if (item.getItemText() != null && !item.getItemText().isEmpty()) {
                                addItemToList(item.getItemText(), item.isChecked());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ToDoListActivity.this, "Error loading items", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Load error", e);
                });
    }

    private void saveAllCheckedStates() {
        for (CheckBox checkBox : allCheckBoxes) {
            String text = checkBox.getText().toString();
            boolean isChecked = checkBox.isChecked();
            updateItemInFirebase(text, isChecked);
        }
        Toast.makeText(this, "All changes saved", Toast.LENGTH_SHORT).show();
    }
}