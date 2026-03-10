package com.smarttask.app.taskinput.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.smarttask.app.R;
import com.smarttask.app.contextacquisition.db.ContextDatabase;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecuteSqlQueriesActivity extends AppCompatActivity {

    private static final Pattern TABLE_TOKEN_PATTERN = Pattern.compile("\\btable\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile("(['\"])(.*?)\\1");
    private static final Pattern SQL_DATETIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

    private MaterialAutoCompleteTextView tableSelector;
    private TextInputEditText queryInput;
    private MaterialButton executeButton;
    private RecyclerView resultRecyclerView;
    private LinearLayout resultHeaderRow;
    private TextView emptyResultView;
    private DatabaseRowAdapter adapter;

    private final List<TableReference> tableReferences = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_execute_sql_queries);

        MaterialToolbar toolbar = findViewById(R.id.execute_sql_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tableSelector = findViewById(R.id.execute_sql_table_selector);
        queryInput = findViewById(R.id.execute_sql_query_input);
        executeButton = findViewById(R.id.execute_sql_execute_button);
        resultRecyclerView = findViewById(R.id.execute_sql_result_recycler_view);
        resultHeaderRow = findViewById(R.id.execute_sql_result_header);
        emptyResultView = findViewById(R.id.execute_sql_empty_view);

        adapter = new DatabaseRowAdapter();
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultRecyclerView.setAdapter(adapter);

        loadTableOptions();

        executeButton.setOnClickListener(v -> executeQuery());
    }

    private void loadTableOptions() {
        new Thread(() -> {
            List<TableReference> tableOptions = queryTableOptions();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                tableReferences.clear();
                tableReferences.addAll(tableOptions);
                updateTableSelector();
            });
        }).start();
    }

    private List<TableReference> queryTableOptions() {
        List<TableReference> result = new ArrayList<>();
        collectTables("Tasks", DatabaseType.TASKS,
                TaskDatabase.getInstance(getApplicationContext()).getOpenHelper().getReadableDatabase(), result);
        collectTables("Context", DatabaseType.CONTEXT,
                ContextDatabase.getInstance(getApplicationContext()).getOpenHelper().getReadableDatabase(), result);
        return result;
    }

    private void collectTables(String databaseLabel, DatabaseType databaseType,
                               SupportSQLiteDatabase database, List<TableReference> out) {
        Cursor cursor = null;
        try {
            cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name");
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                if ("android_metadata".equals(tableName) || "room_master_table".equals(tableName)) {
                    continue;
                }
                out.add(new TableReference(databaseLabel, tableName, databaseType));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateTableSelector() {
        if (tableReferences.isEmpty()) {
            emptyResultView.setText(R.string.database_empty_state);
            emptyResultView.setVisibility(View.VISIBLE);
            return;
        }

        List<String> names = new ArrayList<>();
        for (TableReference tableReference : tableReferences) {
            names.add(tableReference.getDisplayName());
        }

        tableSelector.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));
        tableSelector.setText(names.get(0), false);
    }

    private void executeQuery() {
        TableReference selectedTable = getSelectedTable();
        Editable editable = queryInput.getText();
        String rawQuery = editable != null ? editable.toString().trim() : "";

        if (selectedTable == null) {
            Toast.makeText(this, R.string.execute_sql_select_table_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(rawQuery)) {
            Toast.makeText(this, R.string.execute_sql_query_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String transformedQuery = transformQuery(rawQuery, selectedTable);
        executeButton.setEnabled(false);

        new Thread(() -> {
            QueryResult queryResult = runQuery(selectedTable, transformedQuery);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                executeButton.setEnabled(true);
                if (queryResult.success) {
                    Toast.makeText(this, R.string.execute_sql_success, Toast.LENGTH_SHORT).show();
                    if (queryResult.isSelect) {
                        renderResultTable(queryResult.columnNames, queryResult.rows);
                    } else {
                        clearResultTable();
                    }
                } else {
                    clearResultTable();
                    Toast.makeText(this, getString(R.string.execute_sql_failed, queryResult.errorMessage), Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private QueryResult runQuery(TableReference tableReference, String query) {
        SupportSQLiteDatabase database = getWritableDatabase(tableReference.databaseType);
        String normalized = query.trim().toLowerCase(Locale.US);
        boolean isSelect = normalized.startsWith("select");

        if (isSelect) {
            Cursor cursor = null;
            try {
                cursor = database.query(query);
                String[] cursorColumnNames = cursor.getColumnNames();
                List<String> columns = new ArrayList<>();
                for (String columnName : cursorColumnNames) {
                    columns.add(columnName);
                }
                List<DatabaseRowAdapter.DatabaseRow> rows = new ArrayList<>();
                while (cursor.moveToNext()) {
                    List<String> values = new ArrayList<>();
                    for (int i = 0; i < cursorColumnNames.length; i++) {
                        values.add(readValue(cursor, i));
                    }
                    rows.add(new DatabaseRowAdapter.DatabaseRow(values));
                }
                return QueryResult.success(columns, rows, true);
            } catch (Exception e) {
                return QueryResult.failed(e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        try {
            database.execSQL(query);
            return QueryResult.success(new ArrayList<>(), new ArrayList<>(), false);
        } catch (Exception e) {
            return QueryResult.failed(e.getMessage());
        }
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

    private SupportSQLiteDatabase getWritableDatabase(DatabaseType databaseType) {
        if (databaseType == DatabaseType.CONTEXT) {
            return ContextDatabase.getInstance(getApplicationContext()).getOpenHelper().getWritableDatabase();
        }
        return TaskDatabase.getInstance(getApplicationContext()).getOpenHelper().getWritableDatabase();
    }

    private String transformQuery(String query, TableReference tableReference) {
        String withTableTokenReplaced = TABLE_TOKEN_PATTERN
                .matcher(query)
                .replaceAll(Matcher.quoteReplacement(tableReference.getEscapedTableName()));

        Matcher matcher = QUOTED_TEXT_PATTERN.matcher(withTableTokenReplaced);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String quotedValue = matcher.group(2);
            if (quotedValue != null && SQL_DATETIME_PATTERN.matcher(quotedValue).matches()) {
                Long epochMillis = parseSqlDateToEpochMillis(quotedValue);
                if (epochMillis != null) {
                    matcher.appendReplacement(output, String.valueOf(epochMillis));
                    continue;
                }
            }
            matcher.appendReplacement(output, Matcher.quoteReplacement(matcher.group(0)));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private Long parseSqlDateToEpochMillis(String dateText) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        format.setLenient(false);
        try {
            Date parsed = format.parse(dateText);
            return parsed != null ? parsed.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }

    private TableReference getSelectedTable() {
        String selected = tableSelector.getText() != null ? tableSelector.getText().toString() : "";
        for (TableReference tableReference : tableReferences) {
            if (tableReference.getDisplayName().equals(selected)) {
                return tableReference;
            }
        }
        return null;
    }

    private void clearResultTable() {
        adapter.submitRows(new ArrayList<>());
        adapter.setColumnWidths(new ArrayList<>());
        updateHeaderRow(new ArrayList<>(), new ArrayList<>());
        emptyResultView.setText(R.string.execute_sql_no_select_results);
        emptyResultView.setVisibility(View.VISIBLE);
    }

    private void renderResultTable(List<String> columns, List<DatabaseRowAdapter.DatabaseRow> rows) {
        if (columns == null || columns.isEmpty()) {
            clearResultTable();
            return;
        }

        List<Integer> widths = computeColumnWidths(columns, rows);
        updateHeaderRow(columns, widths);
        adapter.setColumnWidths(widths);
        adapter.submitRows(rows);
        emptyResultView.setText(R.string.execute_sql_no_rows);
        emptyResultView.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateHeaderRow(List<String> columnNames, List<Integer> columnWidths) {
        resultHeaderRow.removeAllViews();
        if (columnNames == null || columnNames.isEmpty()) {
            return;
        }
        int horizontalPadding = (int) (16 * getResources().getDisplayMetrics().density);
        int verticalPadding = (int) (12 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < columnNames.size(); i++) {
            TextView headerCell = new TextView(this);
            headerCell.setText(columnNames.get(i));
            int width = (i >= 0 && i < columnWidths.size())
                    ? columnWidths.get(i)
                    : LinearLayout.LayoutParams.WRAP_CONTENT;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    width,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            headerCell.setLayoutParams(layoutParams);
            headerCell.setBackgroundResource(R.drawable.database_table_cell_border);
            headerCell.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            headerCell.setMaxLines(2);
            headerCell.setSingleLine(false);
            headerCell.setTextAppearance(this, androidx.appcompat.R.style.TextAppearance_AppCompat_Body2);
            headerCell.setTypeface(headerCell.getTypeface(), android.graphics.Typeface.BOLD);
            resultHeaderRow.addView(headerCell);
        }
    }

    private List<Integer> computeColumnWidths(List<String> columnNames, List<DatabaseRowAdapter.DatabaseRow> rows) {
        List<Integer> widths = new ArrayList<>();
        if (columnNames == null || columnNames.isEmpty()) {
            return widths;
        }

        float density = getResources().getDisplayMetrics().density;
        int minWidth = (int) (120 * density);
        int maxWidth = (int) (220 * density);
        int horizontalPadding = (int) (16 * density);
        TextPaint paint = new TextPaint();
        paint.setTextSize(14 * density);

        for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
            float widestText = measureTextWidth(paint, columnNames.get(columnIndex));
            for (DatabaseRowAdapter.DatabaseRow row : rows) {
                if (row == null || row.getValues() == null || columnIndex >= row.getValues().size()) {
                    continue;
                }
                widestText = Math.max(widestText, measureTextWidth(paint, row.getValues().get(columnIndex)));
            }
            int desiredWidth = (int) Math.ceil(widestText) + (horizontalPadding * 2);
            widths.add(Math.max(minWidth, Math.min(maxWidth, desiredWidth)));
        }
        return widths;
    }

    private float measureTextWidth(TextPaint paint, String text) {
        if (text == null || text.isEmpty()) {
            return 0f;
        }
        return paint.measureText(text);
    }

    enum DatabaseType {
        TASKS,
        CONTEXT
    }

    static class TableReference {
        private final String databaseLabel;
        private final String tableName;
        private final DatabaseType databaseType;

        TableReference(String databaseLabel, String tableName, DatabaseType databaseType) {
            this.databaseLabel = databaseLabel;
            this.tableName = tableName;
            this.databaseType = databaseType;
        }

        String getDisplayName() {
            return databaseLabel + " • " + tableName;
        }

        String getEscapedTableName() {
            return "\"" + tableName.replace("\"", "\"\"") + "\"";
        }
    }

    static class QueryResult {
        private final boolean success;
        private final String errorMessage;
        private final List<String> columnNames;
        private final List<DatabaseRowAdapter.DatabaseRow> rows;
        private final boolean isSelect;

        private QueryResult(boolean success, String errorMessage,
                            List<String> columnNames,
                            List<DatabaseRowAdapter.DatabaseRow> rows,
                            boolean isSelect) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.columnNames = columnNames;
            this.rows = rows;
            this.isSelect = isSelect;
        }

        static QueryResult success(List<String> columnNames, List<DatabaseRowAdapter.DatabaseRow> rows, boolean isSelect) {
            return new QueryResult(true, null, columnNames, rows, isSelect);
        }

        static QueryResult failed(String message) {
            return new QueryResult(false, message != null ? message : "Unknown error", new ArrayList<>(), new ArrayList<>(), false);
        }
    }
}
