package ir.shariaty.mytriplist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ToDoListActivity extends AppCompatActivity {

    private EditText newItemEditText;
    private Button addItemButton, saveCheckedButton;
    private LinearLayout itemsLayout;

    // ذخیره‌سازی محلی
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "todo_prefs";
    private static final String KEY_ITEMS  = "items_json";

    // مدل داده‌ای در حافظه
    private final List<ToDoItem> items = new ArrayList<>();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        newItemEditText   = findViewById(R.id.newItemEditText);
        addItemButton     = findViewById(R.id.addItemButton);
        saveCheckedButton = findViewById(R.id.saveCheckedButton);
        itemsLayout       = findViewById(R.id.itemsLayout);

        // فونت ورودی
        Typeface typeface = ResourcesCompat.getFont(this, R.font.vazir);
        if (typeface != null) newItemEditText.setTypeface(typeface);

        // SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // لودِ محلی و ساخت UI
        loadFromPrefs();
        rebuildUIFromItems();

        // افزودن آیتم
        addItemButton.setOnClickListener(v -> {
            String txt = newItemEditText.getText().toString().trim();
            if (txt.isEmpty()) {
                Toast.makeText(this, "Please enter an item", Toast.LENGTH_SHORT).show();
                return;
            }
            ToDoItem item = new ToDoItem(txt, false);
            items.add(item);
            addItemRow(item);           // ردیف جدید توی UI
            newItemEditText.setText(""); // پاک کردن ورودی
        });

        // ذخیره‌ی همه‌ی وضعیت‌ها و بازگشت
        saveCheckedButton.setOnClickListener(v -> {
            // برای اطمینان: وضعیت UI را دوباره در لیست sync کن
            syncListFromUI();
            saveToPrefs();
            Toast.makeText(this, "All changes saved", Toast.LENGTH_SHORT).show();
            goToMainActivity2();
        });
    }

    /** از SharedPreferences آیتم‌ها را لود می‌کند */
    private void loadFromPrefs() {
        String json = prefs.getString(KEY_ITEMS, "[]");
        Type type = new TypeToken<List<ToDoItem>>() {}.getType();
        List<ToDoItem> loaded = gson.fromJson(json, type);
        items.clear();
        if (loaded != null) items.addAll(loaded);
    }

    /** در SharedPreferences ذخیره می‌کند */
    private void saveToPrefs() {
        String json = gson.toJson(items);
        prefs.edit().putString(KEY_ITEMS, json).apply();
    }

    /** تمام UI را از روی لیست دوباره می‌سازد */
    private void rebuildUIFromItems() {
        itemsLayout.removeAllViews();
        for (ToDoItem item : items) {
            addItemRow(item);
        }
    }

    /** یک ردیف (چک‌باکس + حذف) به UI اضافه می‌کند و به آیتم bind می‌کند */
    private void addItemRow(ToDoItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        row.setPadding(pad, pad, pad, pad);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // چک‌باکس
        CheckBox cb = new CheckBox(this);
        cb.setText(item.getItemText());
        cb.setChecked(item.isChecked());
        Typeface typeface = ResourcesCompat.getFont(this, R.font.vazir);
        if (typeface != null) cb.setTypeface(typeface);
        cb.setOnCheckedChangeListener((buttonView, checked) -> item.setChecked(checked));

        // دکمه حذف
        ImageButton del = new ImageButton(this);
        del.setImageResource(R.drawable.ic_trash_icon);
        del.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        delParams.setMargins(pad, 0, 0, 0);
        del.setLayoutParams(delParams);
        del.setOnClickListener(v -> {
            // حذف از لیست و UI
            items.remove(item);
            itemsLayout.removeView(row);
            // اگر می‌خوای همین‌جا هم ذخیره بشه (اختیاری):
            // saveToPrefs();
        });

        row.addView(cb);
        row.addView(del);
        itemsLayout.addView(row);
    }

    /** وضعیت UI را با لیست همگام می‌کند (متن + تیک‌ها) */
    private void syncListFromUI() {
        // متن چک‌باکس‌ها ممکن است تغییر نکرده باشد، ولی برای اطمینان sync می‌کنیم
        int childCount = itemsLayout.getChildCount();
        List<ToDoItem> newList = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View child = itemsLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                if (row.getChildCount() > 0 && row.getChildAt(0) instanceof CheckBox) {
                    CheckBox cb = (CheckBox) row.getChildAt(0);
                    String text   = cb.getText().toString();
                    boolean check = cb.isChecked();
                    newList.add(new ToDoItem(text, check));
                }
            }
        }
        items.clear();
        items.addAll(newList);
    }

    /** رفتن به منوی اصلی */
    private void goToMainActivity2() {
        Intent intent = new Intent(ToDoListActivity.this, MainActivity2.class);
        startActivity(intent);
        finish();
    }
}