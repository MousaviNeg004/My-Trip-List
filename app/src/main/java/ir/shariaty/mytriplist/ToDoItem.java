package ir.shariaty.mytriplist;

public class ToDoItem {
    private String itemText;
    private boolean isChecked;

    public ToDoItem() {}

    public ToDoItem(String itemText, boolean isChecked) {
        this.itemText = itemText;
        this.isChecked = isChecked;
    }

    // Getter و Setter
    public String getItemText() {
        return itemText;
    }

    public void setItemText(String itemText) {
        this.itemText = itemText;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
