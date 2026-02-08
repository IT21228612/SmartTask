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

    private static final int CELL_HORIZONTAL_PADDING_DP = 16;
    private static final int CELL_VERTICAL_PADDING_DP = 12;
    private static final int CELL_MAX_LINES = 4;

    private final List<DatabaseRow> rows = new ArrayList<>();
    private final List<Integer> columnWidthsPx = new ArrayList<>();

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
        List<String> values = row.getValues();
        for (int i = 0; i < values.size(); i++) {
            holder.rowContainer.addView(buildCell(context, values.get(i), getColumnWidth(i)));
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public void setColumnWidths(List<Integer> widthsPx) {
        columnWidthsPx.clear();
        if (widthsPx != null) {
            columnWidthsPx.addAll(widthsPx);
        }
        notifyDataSetChanged();
    }

    public void submitRows(List<DatabaseRow> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        notifyDataSetChanged();
    }

    private int getColumnWidth(int index) {
        if (index >= 0 && index < columnWidthsPx.size()) {
            return columnWidthsPx.get(index);
        }
        return LinearLayout.LayoutParams.WRAP_CONTENT;
    }

    private TextView buildCell(Context context, String text, int cellWidthPx) {
        TextView cell = new TextView(context);
        cell.setText(text);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                cellWidthPx,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cell.setLayoutParams(layoutParams);
        cell.setBackgroundResource(R.drawable.database_table_cell_border);
        int horizontalPadding = (int) (CELL_HORIZONTAL_PADDING_DP * context.getResources().getDisplayMetrics().density);
        int verticalPadding = (int) (CELL_VERTICAL_PADDING_DP * context.getResources().getDisplayMetrics().density);
        cell.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        cell.setMaxLines(CELL_MAX_LINES);
        cell.setSingleLine(false);
        cell.setTextAppearance(context, androidx.appcompat.R.style.TextAppearance_AppCompat_Body2);
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
