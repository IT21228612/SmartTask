package com.smarttask.app.taskinput.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.appbar.MaterialToolbar;
import com.smarttask.app.R;
import com.smarttask.app.contextacquisition.db.ContextDatabase;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseViewerActivity extends AppCompatActivity {

    private RecyclerView tableRecyclerView;
    private View progressBar;
    private TextView emptyView;
    private DatabaseRowAdapter adapter;
    private MaterialAutoCompleteTextView tableSelector;
    private LinearLayout tableHeaderRow;
    private final List<TableData> tables = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_viewer);

        MaterialToolbar toolbar = findViewById(R.id.database_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tableRecyclerView = findViewById(R.id.database_table_recycler_view);
        progressBar = findViewById(R.id.database_progress);
        emptyView = findViewById(R.id.database_empty_view);
        tableSelector = findViewById(R.id.database_table_selector);
        tableHeaderRow = findViewById(R.id.database_table_header);

        adapter = new DatabaseRowAdapter();
        tableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tableRecyclerView.setAdapter(adapter);

        loadTables();
    }

    private void loadTables() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<TableData> tables = queryTables();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                this.tables.clear();
                this.tables.addAll(tables);
                updateTableSelector();
            });
        }).start();
    }

    private List<TableData> queryTables() {
        List<TableData> tables = new ArrayList<>();
        collectTablesFromDatabase("Tasks", TaskDatabase.getInstance(getApplicationContext())
                .getOpenHelper()
                .getReadableDatabase(), tables);
        collectTablesFromDatabase("Context", ContextDatabase.getInstance(getApplicationContext())
                .getOpenHelper()
                .getReadableDatabase(), tables);
        return tables;
    }

    private void collectTablesFromDatabase(String databaseLabel, SupportSQLiteDatabase database, List<TableData> outTables) {
        Cursor cursor = null;
        try {
            cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name");
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                if ("android_metadata".equals(tableName) || "room_master_table".equals(tableName)) {
                    continue;
                }
                TableRows tableRows = queryRows(database, tableName);
                outTables.add(new TableData(databaseLabel, tableName, tableRows.columnNames, tableRows.rows));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private TableRows queryRows(SupportSQLiteDatabase database, String tableName) {
        List<DatabaseRowAdapter.DatabaseRow> rows = new ArrayList<>();
        Cursor rowCursor = null;
        List<String> columnNames = new ArrayList<>();
        try {
            rowCursor = database.query("SELECT * FROM \"" + tableName + "\" ORDER BY rowid DESC LIMIT 25");
            String[] cursorColumnNames = rowCursor.getColumnNames();
            for (String name : cursorColumnNames) {
                columnNames.add(name);
            }
            while (rowCursor.moveToNext()) {
                List<String> values = new ArrayList<>();
                for (int i = 0; i < cursorColumnNames.length; i++) {
                    values.add(readValue(rowCursor, i, cursorColumnNames[i]));
                }
                rows.add(new DatabaseRowAdapter.DatabaseRow(values));
            }
        } finally {
            if (rowCursor != null) {
                rowCursor.close();
            }
        }
        return new TableRows(columnNames, rows);
    }

    private String readValue(Cursor cursor, int index, String columnName) {
        switch (cursor.getType(index)) {
            case Cursor.FIELD_TYPE_NULL:
                return "null";
            case Cursor.FIELD_TYPE_INTEGER:
                return formatIntegerValue(cursor.getLong(index), columnName);
            case Cursor.FIELD_TYPE_FLOAT:
                return String.valueOf(cursor.getDouble(index));
            case Cursor.FIELD_TYPE_STRING:
                return cursor.getString(index);
            case Cursor.FIELD_TYPE_BLOB:
                byte[] blob = cursor.getBlob(index);
                return "blob(" + (blob != null ? blob.length : 0) + " bytes)";
            default:
                return "";
        }
    }

    private String formatIntegerValue(long value, String columnName) {
        if (shouldFormatEpochMillis(columnName, value)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return formatter.format(new Date(value));
        }
        return String.valueOf(value);
    }

    private boolean shouldFormatEpochMillis(String columnName, long value) {
        if (value <= 0) {
            return false;
        }
        String normalized = columnName == null ? "" : columnName.toLowerCase(Locale.US);
        boolean hasDateHint = normalized.contains("time")
                || normalized.contains("date")
                || normalized.contains("timestamp")
                || columnName.endsWith("At");  // <-- new check
        boolean looksLikeMillis = value >= 100000000000L;
        return hasDateHint && looksLikeMillis;
    }

    private void updateTableSelector() {
        if (tables.isEmpty()) {
            emptyView.setText(R.string.database_empty_state);
            emptyView.setVisibility(View.VISIBLE);
            adapter.submitRows(new ArrayList<>());
            tableSelector.setText("", false);
            return;
        }

        List<String> tableNames = new ArrayList<>();
        for (TableData table : tables) {
            tableNames.add(table.getDisplayName());
        }

        ArrayAdapter<String> selectorAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                tableNames
        );
        tableSelector.setAdapter(selectorAdapter);
        TableData defaultTable = findDefaultTable();
        if (defaultTable == null) {
            defaultTable = tables.get(0);
        }
        tableSelector.setText(defaultTable.getDisplayName(), false);
        updateSelectedTable(defaultTable);

        tableSelector.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < tables.size()) {
                updateSelectedTable(tables.get(position));
            }
        });
    }

    private TableData findDefaultTable() {
        for (TableData table : tables) {
            if ("tasks".equalsIgnoreCase(table.getTableName())) {
                return table;
            }
        }
        return null;
    }

    private void updateSelectedTable(TableData selectedTable) {
        if (selectedTable == null) {
            emptyView.setText(R.string.database_empty_state);
            adapter.submitRows(new ArrayList<>());
            updateHeaderRow(new ArrayList<>());
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        updateHeaderRow(selectedTable.getColumnNames());
        adapter.submitRows(selectedTable.getRows());
        emptyView.setText(R.string.database_no_rows);
        emptyView.setVisibility(selectedTable.getRows().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateHeaderRow(List<String> columnNames) {
        tableHeaderRow.removeAllViews();
        if (columnNames == null || columnNames.isEmpty()) {
            return;
        }
        int minWidth = (int) (160 * getResources().getDisplayMetrics().density);
        int horizontalPadding = (int) (16 * getResources().getDisplayMetrics().density);
        int verticalPadding = (int) (12 * getResources().getDisplayMetrics().density);
        for (String columnName : columnNames) {
            TextView headerCell = new TextView(this);
            headerCell.setText(columnName);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            headerCell.setLayoutParams(layoutParams);
            headerCell.setBackgroundResource(R.drawable.database_table_cell_border);
            headerCell.setMinWidth(minWidth);
            headerCell.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            headerCell.setTextAppearance(this, androidx.appcompat.R.style.TextAppearance_AppCompat_Body2);
            headerCell.setTypeface(headerCell.getTypeface(), android.graphics.Typeface.BOLD);
            tableHeaderRow.addView(headerCell);
        }
    }

    static class TableRows {
        private final List<String> columnNames;
        private final List<DatabaseRowAdapter.DatabaseRow> rows;

        TableRows(List<String> columnNames, List<DatabaseRowAdapter.DatabaseRow> rows) {
            this.columnNames = columnNames;
            this.rows = rows;
        }
    }

    static class TableData {
        private final String databaseName;
        private final String tableName;
        private final List<String> columnNames;
        private final List<DatabaseRowAdapter.DatabaseRow> rows;

        TableData(String databaseName, String tableName, List<String> columnNames, List<DatabaseRowAdapter.DatabaseRow> rows) {
            this.databaseName = databaseName;
            this.tableName = tableName;
            this.columnNames = columnNames;
            this.rows = rows;
        }

        public String getTableName() {
            return tableName;
        }

        public List<DatabaseRowAdapter.DatabaseRow> getRows() {
            return rows;
        }

        public List<String> getColumnNames() {
            return columnNames;
        }

        public String getDisplayName() {
            if (databaseName == null || databaseName.isEmpty()) {
                return tableName;
            }
            return databaseName + " • " + tableName;
        }
    }
}
