package com.smarttask.app.taskinput.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
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
        for (RowProperty property : row.getProperties()) {
            TableRow tableRow = new TableRow(context);
            tableRow.addView(buildCell(context, property.getName(), false));
            tableRow.addView(buildCell(context, property.getValue(), false));
            holder.tableLayout.addView(tableRow);
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
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
        );
        cell.setLayoutParams(layoutParams);
        cell.setBackgroundResource(R.drawable.database_table_cell_border);
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
        final TableLayout tableLayout;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            tableLayout = itemView.findViewById(R.id.database_row_table);
        }

        void clearRows() {
            int childCount = tableLayout.getChildCount();
            if (childCount > 1) {
                tableLayout.removeViews(1, childCount - 1);
            }
        }
    }

    public static class DatabaseRow {
        private final List<RowProperty> properties;

        public DatabaseRow(List<RowProperty> properties) {
            this.properties = properties;
        }

        public List<RowProperty> getProperties() {
            return properties;
        }
    }

    public static class RowProperty {
        private final String name;
        private final String value;

        public RowProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
