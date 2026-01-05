package com.smarttask.app.taskinput.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.smarttask.app.R;
import com.smarttask.app.taskinput.db.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    interface TaskClickListener {
        void onTaskClicked(Task task);

        void onTaskLongClicked(Task task);

        void onTaskCompletionToggled(Task task, boolean isCompleted);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final TaskClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    public TaskAdapter(TaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void submitList(List<Task> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        if (position >= 0 && position < tasks.size()) {
            return tasks.get(position);
        }
        return null;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;
        private final TextView dueDateView;
        private final TextView priorityView;
        private final ImageView locationIndicator;
        private final CheckBox completedCheckBox;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.item_task_title);
            dueDateView = itemView.findViewById(R.id.item_task_due);
            priorityView = itemView.findViewById(R.id.item_task_priority);
            locationIndicator = itemView.findViewById(R.id.item_task_location_icon);
            completedCheckBox = itemView.findViewById(R.id.item_task_completed_checkbox);
        }

        void bind(Task task) {
            titleView.setText(task.getTitle());

            if (task.getDueAt() != null) {
                Date date = new Date(task.getDueAt());
                dueDateView.setText(dateFormat.format(date));
                dueDateView.setVisibility(View.VISIBLE);
            } else {
                dueDateView.setText(null);
                dueDateView.setVisibility(View.GONE);
            }

            priorityView.setText(itemView.getContext().getString(R.string.task_priority_value, getPriorityLabel(task.getPriority())));
            locationIndicator.setVisibility(task.getLocationLat() != null ? View.VISIBLE : View.GONE);
            completedCheckBox.setOnCheckedChangeListener(null);
            completedCheckBox.setChecked(task.isCompleted());
            completedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onTaskCompletionToggled(task, isChecked));
            applyCompletionStyles(task.isCompleted());

            itemView.setOnClickListener(v -> listener.onTaskClicked(task));
            itemView.setOnLongClickListener(v -> {
                listener.onTaskLongClicked(task);
                return true;
            });
        }

        private String getPriorityLabel(int priority) {
            switch (priority) {
                case 2:
                    return itemView.getContext().getString(R.string.task_priority_high);
                case 1:
                    return itemView.getContext().getString(R.string.task_priority_medium);
                default:
                    return itemView.getContext().getString(R.string.task_priority_low);
            }
        }

        private void applyCompletionStyles(boolean isCompleted) {
            setStrikeThrough(titleView, isCompleted);
            setStrikeThrough(dueDateView, isCompleted);
            setStrikeThrough(priorityView, isCompleted);

            CardView cardView = (CardView) itemView;
            int backgroundColor = ContextCompat.getColor(
                    itemView.getContext(),
                    isCompleted ? R.color.task_completed_background : R.color.white
            );
            cardView.setCardBackgroundColor(backgroundColor);
        }

        private void setStrikeThrough(TextView textView, boolean enabled) {
            if (enabled) {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }
}
