package com.smarttask.app.taskinput.ui;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapPickerActivity";

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
    private final List<PlacePrediction> lastPredictions = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String mapsApiKey;

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
            if (position >= 0 && position < lastPredictions.size()) {
                PlacePrediction prediction = lastPredictions.get(position);
                fetchPlaceDetailsAndSelect(prediction);
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
        new Thread(() -> {
            String address = null;
            try {
                address = fetchReverseGeocode(latLng);
            } catch (IOException | JSONException ignored) {
                address = null;
            }
            selectedAddress = address;
            mainHandler.post(this::updateSelectedAddressText);
        }).start();
    }

    private void performSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }
        if (TextUtils.isEmpty(getMapsApiKey())) {
            Toast.makeText(this, R.string.task_location_search_no_results, Toast.LENGTH_SHORT).show();
            return;
        }
        searchProgress.setVisibility(android.view.View.VISIBLE);

        new Thread(() -> {
            List<PlacePrediction> results;
            try {
                results = fetchPlacePredictions(query);
            } catch (IOException | JSONException ignored) {
                results = Collections.emptyList();
            }

            List<String> displayTexts = new ArrayList<>();
            for (PlacePrediction prediction : results) {
                displayTexts.add(prediction.description);
            }

            final List<PlacePrediction> resultsFinal = results;

            mainHandler.post(() -> {
                searchProgress.setVisibility(android.view.View.GONE);
                lastPredictions.clear();
                lastPredictions.addAll(resultsFinal);
                resultsAdapter.clear();
                if (displayTexts.isEmpty()) {
                    resultsAdapter.add(getString(R.string.task_location_search_no_results));
                    searchResultsList.setOnItemClickListener(null);
                } else {
                    resultsAdapter.addAll(displayTexts);
                    searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
                        if (position >= 0 && position < lastPredictions.size()) {
                            PlacePrediction prediction = lastPredictions.get(position);
                            fetchPlaceDetailsAndSelect(prediction);
                        }
                    });
                }
                resultsAdapter.notifyDataSetChanged();
            });
        }).start();
    }



    private void fetchPlaceDetailsAndSelect(PlacePrediction prediction) {
        if (prediction == null || TextUtils.isEmpty(prediction.placeId)) {
            return;
        }
        searchProgress.setVisibility(android.view.View.VISIBLE);
        new Thread(() -> {
            PlaceDetails details;
            try {
                details = fetchPlaceDetails(prediction.placeId);
            } catch (IOException | JSONException ignored) {
                details = null;
            }
            PlaceDetails finalDetails = details;
            mainHandler.post(() -> {
                searchProgress.setVisibility(android.view.View.GONE);
                if (finalDetails == null) {
                    Toast.makeText(this, R.string.task_location_search_no_results, Toast.LENGTH_SHORT).show();
                    return;
                }
                updateSelection(finalDetails.latLng, finalDetails.address);
                moveCamera(finalDetails.latLng, 15f);
            });
        }).start();
    }

    private List<PlacePrediction> fetchPlacePredictions(String query) throws IOException, JSONException {
        String apiKey = getMapsApiKey();
        if (TextUtils.isEmpty(apiKey)) {
            return Collections.emptyList();
        }
        String url = new android.net.Uri.Builder()
                .scheme("https")
                .authority("maps.googleapis.com")
                .path("maps/api/place/autocomplete/json")
                .appendQueryParameter("input", query)
                .appendQueryParameter("key", apiKey)
                .appendQueryParameter("types", "geocode")
                .appendQueryParameter("language", Locale.getDefault().getLanguage())
                .build()
                .toString();
        String response = fetchUrl(url, "places-autocomplete");
        JSONObject json = new JSONObject(response);
        logMapsStatus("places-autocomplete", json);
        JSONArray predictions = json.optJSONArray("predictions");
        List<PlacePrediction> results = new ArrayList<>();
        if (predictions == null) {
            return results;
        }
        for (int i = 0; i < predictions.length(); i++) {
            JSONObject prediction = predictions.getJSONObject(i);
            String description = prediction.optString("description");
            String placeId = prediction.optString("place_id");
            if (!TextUtils.isEmpty(placeId)) {
                results.add(new PlacePrediction(description, placeId));
            }
        }
        return results;
    }

    private PlaceDetails fetchPlaceDetails(String placeId) throws IOException, JSONException {
        String apiKey = getMapsApiKey();
        if (TextUtils.isEmpty(apiKey)) {
            return null;
        }
        String url = new android.net.Uri.Builder()
                .scheme("https")
                .authority("maps.googleapis.com")
                .path("maps/api/place/details/json")
                .appendQueryParameter("place_id", placeId)
                .appendQueryParameter("fields", "geometry/location,formatted_address,name")
                .appendQueryParameter("key", apiKey)
                .appendQueryParameter("language", Locale.getDefault().getLanguage())
                .build()
                .toString();
        String response = fetchUrl(url, "places-details");
        JSONObject json = new JSONObject(response);
        logMapsStatus("places-details", json);
        JSONObject result = json.optJSONObject("result");
        if (result == null) {
            return null;
        }
        JSONObject geometry = result.optJSONObject("geometry");
        JSONObject location = geometry != null ? geometry.optJSONObject("location") : null;
        if (location == null) {
            return null;
        }
        double lat = location.optDouble("lat", Double.NaN);
        double lng = location.optDouble("lng", Double.NaN);
        if (Double.isNaN(lat) || Double.isNaN(lng)) {
            return null;
        }
        String address = result.optString("formatted_address");
        if (TextUtils.isEmpty(address)) {
            address = result.optString("name");
        }
        return new PlaceDetails(new LatLng(lat, lng), address);
    }

    private String fetchReverseGeocode(LatLng latLng) throws IOException, JSONException {
        String apiKey = getMapsApiKey();
        if (TextUtils.isEmpty(apiKey)) {
            return null;
        }
        String latLngParam = latLng.latitude + "," + latLng.longitude;
        String url = new android.net.Uri.Builder()
                .scheme("https")
                .authority("maps.googleapis.com")
                .path("maps/api/geocode/json")
                .appendQueryParameter("latlng", latLngParam)
                .appendQueryParameter("key", apiKey)
                .appendQueryParameter("language", Locale.getDefault().getLanguage())
                .build()
                .toString();
        String response = fetchUrl(url, "geocode-reverse");
        JSONObject json = new JSONObject(response);
        logMapsStatus("geocode-reverse", json);
        JSONArray results = json.optJSONArray("results");
        if (results == null || results.length() == 0) {
            return null;
        }
        JSONObject first = results.getJSONObject(0);
        String address = first.optString("formatted_address");
        return TextUtils.isEmpty(address) ? null : address;
    }

    private String fetchUrl(String urlString, String requestLabel) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        try {
            int responseCode = connection.getResponseCode();
            boolean isError = responseCode >= 400;
            InputStream stream = isError ? connection.getErrorStream() : connection.getInputStream();
            String responseBody = readStream(stream);
            String safeUrl = sanitizeUrl(urlString);
            if (isError) {
                Log.e(TAG, "Maps API error (" + requestLabel + ") code=" + responseCode
                        + " url=" + safeUrl + " body=" + responseBody);
            } else {
                Log.d(TAG, "Maps API response (" + requestLabel + ") code=" + responseCode
                        + " url=" + safeUrl + " body=" + responseBody);
            }
            return responseBody;
        } finally {
            connection.disconnect();
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (InputStreamReader reader = new InputStreamReader(stream);
             BufferedReader buffered = new BufferedReader(reader)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = buffered.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private void logMapsStatus(String requestLabel, JSONObject json) {
        if (json == null) {
            Log.w(TAG, "Maps API response (" + requestLabel + ") returned null JSON.");
            return;
        }
        String status = json.optString("status");
        String errorMessage = json.optString("error_message");
        if (!TextUtils.isEmpty(status) || !TextUtils.isEmpty(errorMessage)) {
            Log.w(TAG, "Maps API status (" + requestLabel + ") status=" + status
                    + " error_message=" + errorMessage);
        }
    }

    private String sanitizeUrl(String urlString) {
        android.net.Uri uri = android.net.Uri.parse(urlString);
        android.net.Uri.Builder builder = uri.buildUpon().clearQuery();
        for (String param : uri.getQueryParameterNames()) {
            if ("key".equals(param)) {
                builder.appendQueryParameter(param, "REDACTED");
            } else {
                builder.appendQueryParameter(param, uri.getQueryParameter(param));
            }
        }
        return builder.build().toString();
    }

    private String getMapsApiKey() {
        if (mapsApiKey != null) {
            return mapsApiKey;
        }
        try {
            ApplicationInfo appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                mapsApiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            mapsApiKey = null;
        }
        return mapsApiKey;
    }

    private static class PlacePrediction {
        private final String description;
        private final String placeId;

        private PlacePrediction(String description, String placeId) {
            this.description = description;
            this.placeId = placeId;
        }
    }

    private static class PlaceDetails {
        private final LatLng latLng;
        private final String address;

        private PlaceDetails(LatLng latLng, String address) {
            this.latLng = latLng;
            this.address = address;
        }
    }
}
