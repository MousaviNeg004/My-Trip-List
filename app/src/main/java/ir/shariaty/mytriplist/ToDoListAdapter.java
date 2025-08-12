package ir.shariaty.mytriplist;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.TaskViewHolder> {

    private List<ToDoList> taskList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onEditClick(int position);
    }

    public ToDoListAdapter(List<ToDoList> taskList, OnItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        ToDoList task = taskList.get(position);
        holder.titleText.setText(task.getTitle());
        holder.dateText.setText(task.getStartDate());

        String img = task.getImageUrl();

        if (!TextUtils.isEmpty(img)) {
            if (img.startsWith("http")) {
                // تصویر آنلاین (قدیمی‌ها که روی Firebase بودن)
                Glide.with(holder.itemView.getContext())
                        .load(img)
                        .override(120, 80)
                        .centerCrop()
                        .into(holder.taskImage);
            } else {
                // مسیر لوکال داخل حافظه داخلی برنامه
                File file = new File(img);
                if (file.exists()) {
                    Glide.with(holder.itemView.getContext())
                            .load(file)
                            .override(120, 80)
                            .centerCrop()
                            .into(holder.taskImage);
                } else {
                    // اگر فایل حذف/جابجا شده بود، پلیس‌هولدر نشون بده
                    holder.taskImage.setImageResource(R.drawable.ic_placeholder);
                }
            }
        } else {
            holder.taskImage.setImageResource(R.drawable.ic_placeholder);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(position);
        });

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, dateText;
        Button deleteButton, editButton;
        ImageView taskImage;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.taskTitleText);
            dateText = itemView.findViewById(R.id.taskDateText);
            deleteButton = itemView.findViewById(R.id.delete_button);
            editButton = itemView.findViewById(R.id.edit_button);
            taskImage = itemView.findViewById(R.id.taskImage);
        }
    }
}