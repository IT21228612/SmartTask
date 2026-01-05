package com.smarttask.app.taskinput.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smarttask.app.R;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskCreateActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private TaskDao taskDao;
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText estimatedDurationInput;
    private EditText locationLabelInput;
    private EditText locationRadiusInput;
    private TextView dueDateDisplay;
    private TextView preferredStartDisplay;
    private TextView preferredEndDisplay;
    private Spinner prioritySpinner;
    private Spinner categorySpinner;
    private Switch notificationsSwitch;
    private Long selectedDueDate;
    private Long selectedPreferredStart;
    private Long selectedPreferredEnd;
    private long editingTaskId = -1L;
    private long createdAt = -1L;
    @Nullable
    private Double selectedLat;
    @Nullable
    private Double selectedLng;
    @Nullable
    private String selectedAddress;

    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);

        taskDao = TaskDatabase.getInstance(this).taskDao();

        titleInput = findViewById(R.id.task_title_input);
        descriptionInput = findViewById(R.id.task_description_input);
        estimatedDurationInput = findViewById(R.id.task_estimated_duration_input);
        locationLabelInput = findViewById(R.id.task_location_label_input);
        locationRadiusInput = findViewById(R.id.task_location_radius_input);
        dueDateDisplay = findViewById(R.id.task_due_date_display);
        preferredStartDisplay = findViewById(R.id.task_preferred_start_display);
        preferredEndDisplay = findViewById(R.id.task_preferred_end_display);
        prioritySpinner = findViewById(R.id.task_priority_spinner);
        categorySpinner = findViewById(R.id.task_category_spinner);
        notificationsSwitch = findViewById(R.id.task_notifications_switch);
        TextView locationDisplay = findViewById(R.id.task_location_display);
        Button dueDateButton = findViewById(R.id.task_due_date_button);
        Button clearDueDateButton = findViewById(R.id.task_clear_due_date_button);
        Button preferredStartButton = findViewById(R.id.task_preferred_start_button);
        Button clearPreferredStartButton = findViewById(R.id.task_clear_preferred_start_button);
        Button preferredEndButton = findViewById(R.id.task_preferred_end_button);
        Button clearPreferredEndButton = findViewById(R.id.task_clear_preferred_end_button);
        Button locationButton = findViewById(R.id.task_location_button);
        Button saveButton = findViewById(R.id.save_task_button);
        Button cancelButton = findViewById(R.id.cancel_task_button);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.task_priority_options,
                android.R.layout.simple_spinner_item
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.task_category_options,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        dueDateButton.setOnClickListener(v -> openDueDatePicker());
        clearDueDateButton.setOnClickListener(v -> {
            selectedDueDate = null;
            updateDueDateDisplay();
        });
        preferredStartButton.setOnClickListener(v -> openTimeSelector(TimeType.PREFERRED_START));
        clearPreferredStartButton.setOnClickListener(v -> {
            selectedPreferredStart = null;
            updatePreferredStartDisplay();
        });
        preferredEndButton.setOnClickListener(v -> openTimeSelector(TimeType.PREFERRED_END));
        clearPreferredEndButton.setOnClickListener(v -> {
            selectedPreferredEnd = null;
            updatePreferredEndDisplay();
        });
        locationButton.setOnClickListener(v -> openLocationPicker());
        saveButton.setOnClickListener(v -> saveTask());
        cancelButton.setOnClickListener(v -> finish());

        long taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1L);
        if (taskId != -1L) {
            loadTask(taskId);
            locationButton.setText(R.string.task_update_location);
        } else {
            createdAt = System.currentTimeMillis();
            updateLocationDisplay(locationDisplay);
            notificationsSwitch.setChecked(true);
        }
    }

    private void loadTask(long taskId) {
        Task task = taskDao.getTaskById(taskId);
        if (task == null) {
            Toast.makeText(this, R.string.task_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        editingTaskId = taskId;
        createdAt = task.getCreatedAt();
        titleInput.setText(task.getTitle());
        descriptionInput.setText(task.getDescription());
        estimatedDurationInput.setText(task.getEstimatedDurationMin() != null ? String.valueOf(task.getEstimatedDurationMin()) : "");
        locationLabelInput.setText(task.getLocationLabel());
        locationRadiusInput.setText(task.getLocationRadius() != null ? String.valueOf(task.getLocationRadius()) : "");
        selectedDueDate = task.getDueAt();
        updateDueDateDisplay();
        prioritySpinner.setSelection(Math.max(0, Math.min(task.getPriority(), 2)));
        int categoryIndex = getCategoryIndex(task.getCategory());
        categorySpinner.setSelection(categoryIndex);
        selectedPreferredStart = task.getPreferredStartTime();
        selectedPreferredEnd = task.getPreferredEndTime();
        updatePreferredStartDisplay();
        updatePreferredEndDisplay();
        notificationsSwitch.setChecked(task.isNotificationsEnabled());
        selectedLat = task.getLocationLat();
        selectedLng = task.getLocationLng();
        selectedAddress = task.getLocationLabel();
        updateLocationDisplay(findViewById(R.id.task_location_display));
    }

    private void openDueDatePicker() {
        calendar.setTimeInMillis(selectedDueDate != null ? selectedDueDate : System.currentTimeMillis());
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    openTimePicker(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void openTimePicker(Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    selectedDueDate = calendar.getTimeInMillis();
                    updateDueDateDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void openTimeSelector(TimeType timeType) {
        calendar.setTimeInMillis(getInitialTimeForType(timeType));
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    openTimePickerForType(timeType, calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private long getInitialTimeForType(TimeType timeType) {
        Long existing = timeType == TimeType.PREFERRED_START ? selectedPreferredStart : selectedPreferredEnd;
        return existing != null ? existing : System.currentTimeMillis();
    }

    private void openTimePickerForType(TimeType timeType, Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    long value = calendar.getTimeInMillis();
                    if (timeType == TimeType.PREFERRED_START) {
                        selectedPreferredStart = value;
                        updatePreferredStartDisplay();
                    } else {
                        selectedPreferredEnd = value;
                        updatePreferredEndDisplay();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDueDateDisplay() {
        if (selectedDueDate == null) {
            dueDateDisplay.setText(R.string.task_due_date_not_set);
        } else {
            dueDateDisplay.setText(dateFormat.format(selectedDueDate));
        }
    }

    private void updatePreferredStartDisplay() {
        if (selectedPreferredStart == null) {
            preferredStartDisplay.setText(R.string.task_preferred_time_not_set);
        } else {
            preferredStartDisplay.setText(dateFormat.format(selectedPreferredStart));
        }
    }

    private void updatePreferredEndDisplay() {
        if (selectedPreferredEnd == null) {
            preferredEndDisplay.setText(R.string.task_preferred_time_not_set);
        } else {
            preferredEndDisplay.setText(dateFormat.format(selectedPreferredEnd));
        }
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String estimatedDurationText = estimatedDurationInput.getText().toString().trim();
        String locationRadiusText = locationRadiusInput.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleInput.setError(getString(R.string.task_title_required));
            return;
        }

        if (createdAt == -1L) {
            createdAt = System.currentTimeMillis();
        }

        int priority = prioritySpinner.getSelectedItemPosition();
        String category = categorySpinner.getSelectedItem().toString().toLowerCase(Locale.getDefault());
        Integer estimatedDuration = parseIntegerSafe(estimatedDurationText);
        Float locationRadius = parseFloatSafe(locationRadiusText);
        boolean notificationsEnabled = notificationsSwitch.isChecked();
        long now = System.currentTimeMillis();

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category);
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(now);
        task.setDueAt(selectedDueDate);
        task.setPriority(priority);
        task.setLocationLat(selectedLat);
        task.setLocationLng(selectedLng);
        task.setLocationRadius(locationRadius);
        task.setLocationLabel(locationLabelInput.getText().toString().trim());
        task.setEstimatedDurationMin(estimatedDuration);
        task.setPreferredStartTime(selectedPreferredStart);
        task.setPreferredEndTime(selectedPreferredEnd);
        task.setNotificationsEnabled(notificationsEnabled);
        task.setCompleted(false);
        task.setArchived(false);
        task.setSnoozeUntil(null);
        task.setCompletedAt(null);

        if (editingTaskId != -1L) {
            task.setId(editingTaskId);
            task.setUpdatedAt(now);
            taskDao.updateTask(task);
        } else {
            long newId = taskDao.insertTask(task);
            task.setId(newId);
        }

        Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void openLocationPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        if (selectedLat != null && selectedLng != null) {
            intent.putExtra(MapPickerActivity.EXTRA_INITIAL_LAT, selectedLat);
            intent.putExtra(MapPickerActivity.EXTRA_INITIAL_LNG, selectedLng);
            if (!TextUtils.isEmpty(selectedAddress)) {
                intent.putExtra(MapPickerActivity.EXTRA_INITIAL_ADDRESS, selectedAddress);
            }
        }
        mapPickerLauncher.launch(intent);
    }

    private void updateLocationDisplay(TextView locationDisplay) {
        if (selectedLat != null && selectedLng != null) {
            String addressText = selectedAddress;
            if (TextUtils.isEmpty(addressText)) {
                addressText = getString(R.string.task_location_coordinates, selectedLat, selectedLng);
            }
            locationDisplay.setText(getString(R.string.task_location_selected, addressText));
        } else {
            locationDisplay.setText(R.string.task_location_not_set);
        }
    }

    private int getCategoryIndex(String category) {
        String[] categories = getResources().getStringArray(R.array.task_category_options);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(category)) {
                return i;
            }
        }
        return 0;
    }

    @Nullable
    private Integer parseIntegerSafe(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private Float parseFloatSafe(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private enum TimeType {
        PREFERRED_START,
        PREFERRED_END
    }

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double lat = data.getDoubleExtra(MapPickerActivity.EXTRA_SELECTED_LAT, Double.NaN);
                    double lng = data.getDoubleExtra(MapPickerActivity.EXTRA_SELECTED_LNG, Double.NaN);
                    if (!Double.isNaN(lat) && !Double.isNaN(lng)) {
                        selectedLat = lat;
                        selectedLng = lng;
                        selectedAddress = data.getStringExtra(MapPickerActivity.EXTRA_SELECTED_ADDRESS);
                        if (!TextUtils.isEmpty(selectedAddress)) {
                            locationLabelInput.setText(selectedAddress);
                        }
                        updateLocationDisplay(findViewById(R.id.task_location_display));
                        Toast.makeText(this, R.string.task_location_saved, Toast.LENGTH_SHORT).show();
                    }
                }
            });
}
