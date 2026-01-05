package com.smarttask.app.taskinput.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.material.appbar.MaterialToolbar;
import com.smarttask.app.R;
import com.smarttask.app.contextacquisition.db.ContextDatabase;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.util.ArrayList;
import java.util.List;

public class DatabaseViewerActivity extends AppCompatActivity {

    private RecyclerView tableRecyclerView;
    private View progressBar;
    private TextView emptyView;
    private DatabaseTableAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_viewer);

        MaterialToolbar toolbar = findViewById(R.id.database_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tableRecyclerView = findViewById(R.id.database_table_recycler_view);
        progressBar = findViewById(R.id.database_progress);
        emptyView = findViewById(R.id.database_empty_view);

        adapter = new DatabaseTableAdapter();
        tableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tableRecyclerView.setAdapter(adapter);

        loadTables();
    }

    private void loadTables() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<DatabaseTableAdapter.TableData> tables = queryTables();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                adapter.submitTables(tables);
                emptyView.setVisibility(tables.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private List<DatabaseTableAdapter.TableData> queryTables() {
        List<DatabaseTableAdapter.TableData> tables = new ArrayList<>();
        collectTablesFromDatabase("Tasks", TaskDatabase.getInstance(getApplicationContext())
                .getOpenHelper()
                .getReadableDatabase(), tables);
        collectTablesFromDatabase("Context", ContextDatabase.getInstance(getApplicationContext())
                .getOpenHelper()
                .getReadableDatabase(), tables);
        return tables;
    }

    private void collectTablesFromDatabase(String databaseLabel, SupportSQLiteDatabase database, List<DatabaseTableAdapter.TableData> outTables) {
        Cursor cursor = null;
        try {
            cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name");
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                if ("android_metadata".equals(tableName) || "room_master_table".equals(tableName)) {
                    continue;
                }
                List<String> rows = queryRows(database, tableName);
                outTables.add(new DatabaseTableAdapter.TableData(databaseLabel, tableName, rows));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private List<String> queryRows(SupportSQLiteDatabase database, String tableName) {
        List<String> rows = new ArrayList<>();
        Cursor rowCursor = null;
        try {
            rowCursor = database.query("SELECT * FROM \"" + tableName + "\" ORDER BY rowid DESC LIMIT 50");
            String[] columnNames = rowCursor.getColumnNames();
            while (rowCursor.moveToNext()) {
                StringBuilder rowBuilder = new StringBuilder();
                for (int i = 0; i < columnNames.length; i++) {
                    if (i > 0) {
                        rowBuilder.append(" • ");
                    }
                    rowBuilder.append(columnNames[i]).append(": ").append(readValue(rowCursor, i));
                }
                rows.add(rowBuilder.toString());
            }
        } finally {
            if (rowCursor != null) {
                rowCursor.close();
            }
        }
        return rows;
    }

    private String readValue(Cursor cursor, int index) {
        switch (cursor.getType(index)) {
            case Cursor.FIELD_TYPE_NULL:
                return "null";
            case Cursor.FIELD_TYPE_INTEGER:
                return String.valueOf(cursor.getLong(index));
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
}
