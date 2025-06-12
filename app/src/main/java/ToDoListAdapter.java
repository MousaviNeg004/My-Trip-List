import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ToDoViewHolder> {
    private List<ToDoList> taskList;

    // Constructor
    public ToDoListAdapter(List<ToDoList> taskList) {
        this.taskList = taskList;
    }

    @Override
    public ToDoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_title, parent, false);
        return new ToDoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ToDoViewHolder holder, int position) {
        ToDoList task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskDueDate.setText(task.getDueDate());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // ViewHolder class to hold the views
    public static class ToDoViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDueDate;

        public ToDoViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskDueDate = itemView.findViewById(R.id.task_due_date);
        }
    }
}
