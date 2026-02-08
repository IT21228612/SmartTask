package com.smarttask.app.taskinput.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.smarttask.app.R;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String LOCATION_TYPE_WORK = "Work";
    private static final String LOCATION_TYPE_HOME = "Home";

    private Spinner locationTypeSpinner;
    private LinearLayout locationEditorContainer;
    private TextView selectedLocationText;

    private WorkHomeLocationStore locationStore;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    private Double selectedLat;
    @Nullable
    private Double selectedLng;
    @Nullable
    private String selectedAddress;

    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                    return;
                }
                Intent data = result.getData();
                if (!data.hasExtra(MapPickerActivity.EXTRA_SELECTED_LAT)
                        || !data.hasExtra(MapPickerActivity.EXTRA_SELECTED_LNG)) {
                    return;
                }
                selectedLat = data.getDoubleExtra(MapPickerActivity.EXTRA_SELECTED_LAT, 0d);
                selectedLng = data.getDoubleExtra(MapPickerActivity.EXTRA_SELECTED_LNG, 0d);
                selectedAddress = data.getStringExtra(MapPickerActivity.EXTRA_SELECTED_ADDRESS);
                updateSelectedLocationText();
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        locationStore = new WorkHomeLocationStore(this);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());

        Button setWorkHomeMenuButton = findViewById(R.id.set_work_home_menu_button);
        locationEditorContainer = findViewById(R.id.work_home_editor_container);
        locationTypeSpinner = findViewById(R.id.work_home_type_spinner);
        selectedLocationText = findViewById(R.id.work_home_selected_location_text);
        Button pickLocationButton = findViewById(R.id.work_home_pick_location_button);
        Button saveLocationButton = findViewById(R.id.work_home_save_button);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.work_home_location_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationTypeSpinner.setAdapter(typeAdapter);

        setWorkHomeMenuButton.setOnClickListener(v -> {
            locationEditorContainer.setVisibility(View.VISIBLE);
            loadLocationForSelectedType();
        });

        locationTypeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadLocationForSelectedType();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        pickLocationButton.setOnClickListener(v -> openMapPicker());
        saveLocationButton.setOnClickListener(v -> saveCurrentLocation());
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }

    private void openMapPicker() {
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

    private void loadLocationForSelectedType() {
        String type = getSelectedLocationType();
        disposables.add(locationStore.getLocation(type)
                .subscribe(
                        location -> runOnUiThread(() -> {
                            selectedLat = location.getLat();
                            selectedLng = location.getLng();
                            selectedAddress = location.getAddress();
                            updateSelectedLocationText();
                        }),
                        throwable -> runOnUiThread(() ->
                                Toast.makeText(this, R.string.work_home_location_load_failed, Toast.LENGTH_SHORT).show())
                ));
    }

    private void saveCurrentLocation() {
        if (selectedLat == null || selectedLng == null) {
            Toast.makeText(this, R.string.work_home_location_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        String type = getSelectedLocationType();
        disposables.add(locationStore.saveLocation(type, selectedLat, selectedLng, selectedAddress)
                .subscribe(
                        preferences -> runOnUiThread(() ->
                                Toast.makeText(this, getString(R.string.work_home_location_saved, type), Toast.LENGTH_SHORT).show()),
                        throwable -> runOnUiThread(() -> {
                            Log.e(TAG, "Failed to save " + type + " location", throwable);
                            Toast.makeText(this, R.string.work_home_location_save_failed, Toast.LENGTH_SHORT).show();
                        })
                ));
    }

    private void updateSelectedLocationText() {
        if (selectedLat == null || selectedLng == null) {
            selectedLocationText.setText(R.string.work_home_location_not_set);
            return;
        }

        String type = getSelectedLocationType();
        String locationText = !TextUtils.isEmpty(selectedAddress)
                ? selectedAddress
                : getString(R.string.task_location_coordinates, selectedLat, selectedLng);
        selectedLocationText.setText(getString(R.string.work_home_location_selected, type, locationText));
    }

    private String getSelectedLocationType() {
        Object selectedItem = locationTypeSpinner.getSelectedItem();
        if (selectedItem == null) {
            return LOCATION_TYPE_WORK;
        }
        String value = selectedItem.toString();
        if (LOCATION_TYPE_HOME.equals(value)) {
            return LOCATION_TYPE_HOME;
        }
        return LOCATION_TYPE_WORK;
    }
}
