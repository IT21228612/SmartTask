package com.smarttask.app.taskinput.ui;

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

public class DatabaseTableAdapter extends RecyclerView.Adapter<DatabaseTableAdapter.TableViewHolder> {

    private final List<TableData> tables = new ArrayList<>();

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_database_table, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableData table = tables.get(position);
        holder.tableName.setText(table.getDisplayName());
        holder.tableEmptyRows.setVisibility(table.getRows().isEmpty() ? View.VISIBLE : View.GONE);
        holder.tableRowsContainer.removeAllViews();

        if (!table.getRows().isEmpty()) {
            for (String row : table.getRows()) {
                TextView rowView = new TextView(holder.itemView.getContext());
                rowView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                rowView.setPadding(0, 4, 0, 4);
                rowView.setText(row);
                rowView.setTextAppearance(holder.itemView.getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Body2);
                holder.tableRowsContainer.addView(rowView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    public void submitTables(List<TableData> newTables) {
        tables.clear();
        if (newTables != null) {
            tables.addAll(newTables);
        }
        notifyDataSetChanged();
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {
        final TextView tableName;
        final TextView tableEmptyRows;
        final LinearLayout tableRowsContainer;

        TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tableName = itemView.findViewById(R.id.table_name);
            tableEmptyRows = itemView.findViewById(R.id.table_empty_rows);
            tableRowsContainer = itemView.findViewById(R.id.table_rows_container);
        }
    }

    public static class TableData {
        private final String databaseName;
        private final String tableName;
        private final List<String> rows;

        public TableData(String databaseName, String tableName, List<String> rows) {
            this.databaseName = databaseName;
            this.tableName = tableName;
            this.rows = rows;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getTableName() {
            return tableName;
        }

        public List<String> getRows() {
            return rows;
        }

        public String getDisplayName() {
            if (databaseName == null || databaseName.isEmpty()) {
                return tableName;
            }
            return databaseName + " • " + tableName;
        }
    }
}
