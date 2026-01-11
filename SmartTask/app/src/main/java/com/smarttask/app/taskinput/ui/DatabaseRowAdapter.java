package com.smarttask.app.taskinput.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarttask.app.R;

import java.util.ArrayList;
import java.util.List;

public class DatabaseRowAdapter extends RecyclerView.Adapter<DatabaseRowAdapter.RowViewHolder> {

    private final List<DatabaseRow> rows = new ArrayList<>();

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_database_row, parent, false);
        return new RowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        DatabaseRow row = rows.get(position);
        holder.clearRows();
        Context context = holder.itemView.getContext();
        for (String value : row.getValues()) {
            holder.rowContainer.addView(buildCell(context, value, false));
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public void submitRows(List<DatabaseRow> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        notifyDataSetChanged();
    }

    private TextView buildCell(Context context, String text, boolean isHeader) {
        TextView cell = new TextView(context);
        cell.setText(text);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cell.setLayoutParams(layoutParams);
        cell.setBackgroundResource(R.drawable.database_table_cell_border);
        int minWidth = (int) (160 * context.getResources().getDisplayMetrics().density);
        cell.setMinWidth(minWidth);
        int horizontalPadding = (int) (16 * context.getResources().getDisplayMetrics().density);
        int verticalPadding = (int) (12 * context.getResources().getDisplayMetrics().density);
        cell.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        cell.setTextAppearance(context, isHeader
                ? androidx.appcompat.R.style.TextAppearance_AppCompat_Body2
                : androidx.appcompat.R.style.TextAppearance_AppCompat_Body2);
        if (isHeader) {
            cell.setTypeface(cell.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return cell;
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout rowContainer;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            rowContainer = itemView.findViewById(R.id.database_row_container);
        }

        void clearRows() {
            int childCount = rowContainer.getChildCount();
            if (childCount > 0) {
                rowContainer.removeViews(0, childCount);
            }
        }
    }

    public static class DatabaseRow {
        private final List<String> values;

        public DatabaseRow(List<String> values) {
            this.values = values;
        }

        public List<String> getValues() {
            return values;
        }
    }
}
