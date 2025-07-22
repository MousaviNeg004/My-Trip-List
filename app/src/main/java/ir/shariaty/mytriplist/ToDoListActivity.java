package ir.shariaty.mytriplist;

import android.os.Bundle;
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
import com.google.firebase.firestore.QuerySnapshot;

public class ToDoListActivity extends AppCompatActivity {

    private EditText newItemEditText;
    private Button addItemButton;
    private LinearLayout itemsLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        db = FirebaseFirestore.getInstance();

        newItemEditText = findViewById(R.id.newItemEditText);
        addItemButton = findViewById(R.id.addItemButton);
        itemsLayout = findViewById(R.id.itemsLayout);

        //set font for the EditText using ResourcesCompat
        Typeface typeface = ResourcesCompat.getFont(this, R.font.vazir);
        newItemEditText.setTypeface(typeface);

        //item button listener
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

        loadItemsFromFirebase();
    }

    //method to add item to the list dynamically with a checkbox and a delete button
    private void addItemToList(String itemText, boolean isChecked) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(10, 10, 10, 10);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        //create a CheckBox for the item text
        CheckBox newCheckBox = new CheckBox(this);
        newCheckBox.setText(itemText);
        newCheckBox.setChecked(isChecked);
        newCheckBox.setTypeface(ResourcesCompat.getFont(this, R.font.vazir));

        newCheckBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            updateItemInFirebase(itemText, isChecked1);
        });

        //create a delete button
        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(R.drawable.ic_trash_icon);
        deleteButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        //delete button's position
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) deleteButton.getLayoutParams();
        params.setMargins(10, 0, 0, 0);  // Add some margin to move it a bit to the left if needed
        deleteButton.setLayoutParams(params);

        deleteButton.setOnClickListener(v -> {
            deleteItemFromFirebase(itemText);
            itemsLayout.removeView(itemLayout);
        });

        itemLayout.addView(newCheckBox);
        itemLayout.addView(deleteButton);

        itemsLayout.addView(itemLayout);
    }

    //method to save item to Firestore
    private void saveItemToFirebase(String itemText, boolean isChecked) {
        // Create a new ToDoItem object
        ToDoItem newItem = new ToDoItem(itemText, isChecked);

        // Save the item to Firestore
        db.collection("todoItems")
                .add(newItem)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ToDoListActivity.this, "Item added to Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ToDoListActivity.this, "Error adding item", Toast.LENGTH_SHORT).show();
                });
    }

    //method to update the item status in Firestore
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
                });
    }

    //method to delete the item from Firestore
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
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ToDoListActivity.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                });
    }

    //method to load items from Firebase and add them to the UI
    private void loadItemsFromFirebase() {
        db.collection("todoItems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        itemsLayout.removeAllViews();
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
                });
    }
}
