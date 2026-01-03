package com.smarttask.app.taskinput.ui;
import com.smarttask.app.BuildConfig;


import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smarttask.app.R;
import com.smarttask.app.util.AppConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_SELECTED_LAT = "extra_selected_lat";
    public static final String EXTRA_SELECTED_LNG = "extra_selected_lng";
    public static final String EXTRA_SELECTED_ADDRESS = "extra_selected_address";

    public static final String EXTRA_INITIAL_LAT = "extra_initial_lat";
    public static final String EXTRA_INITIAL_LNG = "extra_initial_lng";
    public static final String EXTRA_INITIAL_ADDRESS = "extra_initial_address";

    private GoogleMap googleMap;
    private Marker selectedMarker;
    private LatLng selectedLatLng;
    private String selectedAddress;

    private TextView selectedAddressText;
    private ProgressBar searchProgress;
    private ListView searchResultsList;
    private SearchView searchView;

    private ArrayAdapter<String> resultsAdapter;
    private List<Address> lastResults = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    enableMyLocation();
                } else {
                    Toast.makeText(this, R.string.task_location_permission_rationale, Toast.LENGTH_LONG).show();
                    centerOnDefault();
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        //android.util.Log.d("MapPickerActivity", "GOOGLE_MAPS_API_KEY = " + BuildConfig.GOOGLE_MAPS_API_KEY);

        Toolbar toolbar = findViewById(R.id.map_toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());

        searchView = findViewById(R.id.location_search_view);
        selectedAddressText = findViewById(R.id.selected_address_text);
        searchProgress = findViewById(R.id.search_progress);
        searchResultsList = findViewById(R.id.search_results_list);
        Button confirmButton = findViewById(R.id.confirm_location_button);

        resultsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        searchResultsList.setAdapter(resultsAdapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < lastResults.size()) {
                Address address = lastResults.get(position);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                updateSelection(latLng, formatAddress(address));
                moveCamera(latLng, 15f);
            }
        });

        confirmButton.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, R.string.task_location_missing, Toast.LENGTH_SHORT).show();
                return;
            }
            android.content.Intent resultIntent = new android.content.Intent();
            resultIntent.putExtra(EXTRA_SELECTED_LAT, selectedLatLng.latitude);
            resultIntent.putExtra(EXTRA_SELECTED_LNG, selectedLatLng.longitude);
            if (!TextUtils.isEmpty(selectedAddress)) {
                resultIntent.putExtra(EXTRA_SELECTED_ADDRESS, selectedAddress);
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        double initialLat = getIntent().getDoubleExtra(EXTRA_INITIAL_LAT, Double.NaN);
        double initialLng = getIntent().getDoubleExtra(EXTRA_INITIAL_LNG, Double.NaN);
        String initialAddress = getIntent().getStringExtra(EXTRA_INITIAL_ADDRESS);
        if (!Double.isNaN(initialLat) && !Double.isNaN(initialLng)) {
            selectedLatLng = new LatLng(initialLat, initialLng);
            selectedAddress = initialAddress;
            updateSelection(selectedLatLng, selectedAddress);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {

        // ✅ Log and Toast to check if map is initialized
        android.util.Log.d("MapPickerActivity", "onMapReady called – map should display now");
        Toast.makeText(this, "Map is ready – check logcat", Toast.LENGTH_SHORT).show();

        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMapClickListener(latLng -> updateSelection(latLng, null));

        // Log that the map object is ready
        Log.d("MapPickerActivity", "Google Map object is ready.");

        // ✅ Add this to know when map tiles have actually loaded
        googleMap.setOnMapLoadedCallback(() -> {
            Log.d("MapPickerActivity", "Map finished loading!");
        });

        if (selectedLatLng != null) {
            moveCamera(selectedLatLng, 15f);
        } else if (hasLocationPermission()) {
            enableMyLocation();
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void enableMyLocation() {
        if (googleMap == null || !hasLocationPermission()) {
            return;
        }
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        moveCamera(userLatLng, 15f);
                    } else {
                        Toast.makeText(this, R.string.task_location_my_location_unavailable, Toast.LENGTH_SHORT).show();
                        centerOnDefault();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.task_location_my_location_unavailable, Toast.LENGTH_SHORT).show();
                    centerOnDefault();
                });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void centerOnDefault() {
        moveCamera(new LatLng(AppConstants.DEFAULT_LOCATION_LAT, AppConstants.DEFAULT_LOCATION_LNG), 14f);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
        updateMarker(latLng);
    }

    private void updateMarker(LatLng latLng) {
        if (googleMap == null || latLng == null) {
            return;
        }
        if (selectedMarker != null) {
            selectedMarker.setPosition(latLng);
        } else {
            selectedMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
        }
    }

    private void updateSelection(LatLng latLng, @Nullable String address) {
        selectedLatLng = latLng;
        if (!TextUtils.isEmpty(address)) {
            selectedAddress = address;
        } else {
            fetchAddressForLatLng(latLng);
        }
        updateMarker(latLng);
        updateSelectedAddressText();
    }

    private void updateSelectedAddressText() {
        if (selectedLatLng == null) {
            selectedAddressText.setText(R.string.task_location_not_set);
            return;
        }
        String addressText = !TextUtils.isEmpty(selectedAddress)
                ? selectedAddress
                : getString(R.string.task_location_coordinates, selectedLatLng.latitude, selectedLatLng.longitude);
        selectedAddressText.setText(getString(R.string.task_location_selected, addressText));
    }

    private void fetchAddressForLatLng(LatLng latLng) {
        if (!Geocoder.isPresent()) {
            selectedAddress = null;
            updateSelectedAddressText();
            return;
        }
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    selectedAddress = formatAddress(addresses.get(0));
                } else {
                    selectedAddress = null;
                }
            } catch (IOException e) {
                selectedAddress = null;
            }
            mainHandler.post(this::updateSelectedAddressText);
        }).start();
    }

    private void performSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, R.string.task_location_search_no_results, Toast.LENGTH_SHORT).show();
            return;
        }
        searchProgress.setVisibility(android.view.View.VISIBLE);

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> results;
            try {
                List<Address> geocoderResults = geocoder.getFromLocationName(query, 5);
                results = geocoderResults != null ? geocoderResults : Collections.emptyList();
            } catch (IOException ignored) {
                results = Collections.emptyList();
            }

            List<String> displayTexts = new ArrayList<>();
            for (Address address : results) {
                displayTexts.add(formatAddress(address));
            }

            // Copy to a final variable so the lambda can capture it
            final List<Address> resultsFinal = results;

            mainHandler.post(() -> {
                searchProgress.setVisibility(android.view.View.GONE);
                lastResults.clear();
                lastResults.addAll(resultsFinal);
                resultsAdapter.clear();
                if (displayTexts.isEmpty()) {
                    resultsAdapter.add(getString(R.string.task_location_search_no_results));
                    searchResultsList.setOnItemClickListener(null);
                } else {
                    resultsAdapter.addAll(displayTexts);
                    searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
                        if (position >= 0 && position < lastResults.size()) {
                            Address address = lastResults.get(position);
                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                            updateSelection(latLng, formatAddress(address));
                            moveCamera(latLng, 15f);
                        }
                    });
                }
                resultsAdapter.notifyDataSetChanged();
            });
        }).start();
    }



    private String formatAddress(Address address) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            String line = address.getAddressLine(i);
            if (!TextUtils.isEmpty(line)) {
                parts.add(line);
            }
        }
        if (parts.isEmpty()) {
            String feature = address.getFeatureName();
            String locality = address.getLocality();
            if (!TextUtils.isEmpty(feature)) {
                parts.add(feature);
            }
            if (!TextUtils.isEmpty(locality)) {
                parts.add(locality);
            }
        }
        if (parts.isEmpty()) {
            return getString(R.string.task_location_coordinates, address.getLatitude(), address.getLongitude());
        }
        return TextUtils.join(", ", parts);
    }
}
