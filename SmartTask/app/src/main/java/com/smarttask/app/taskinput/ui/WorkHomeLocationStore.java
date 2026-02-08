package com.smarttask.app.taskinput.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;

import io.reactivex.rxjava3.core.Single;

public class WorkHomeLocationStore {

    private static final String DATASTORE_FILE_NAME = "app_settings.preferences_pb";
    private static final String TYPE_WORK = "Work";

    private final RxDataStore<Preferences> dataStore;

    public WorkHomeLocationStore(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        dataStore = new RxPreferenceDataStoreBuilder(appContext, DATASTORE_FILE_NAME).build();
    }

    public Single<WorkHomeLocation> getLocation(@NonNull String type) {
        Preferences.Key<Double> latKey = getLatKey(type);
        Preferences.Key<Double> lngKey = getLngKey(type);
        Preferences.Key<String> addressKey = getAddressKey(type);

        return dataStore.data()
                .firstOrError()
                .map(preferences -> {
                    Double lat = preferences.get(latKey);
                    Double lng = preferences.get(lngKey);
                    String address = preferences.get(addressKey);
                    return new WorkHomeLocation(lat, lng, address);
                });
    }

    public Single<Preferences> saveLocation(@NonNull String type,
                                            double lat,
                                            double lng,
                                            @Nullable String address) {
        Preferences.Key<Double> latKey = getLatKey(type);
        Preferences.Key<Double> lngKey = getLngKey(type);
        Preferences.Key<String> addressKey = getAddressKey(type);

        return dataStore.updateDataAsync(preferencesIn -> {
            MutablePreferences mutablePreferences = preferencesIn.toMutablePreferences();
            mutablePreferences.set(latKey, lat);
            mutablePreferences.set(lngKey, lng);
            if (address == null || address.trim().isEmpty()) {
                mutablePreferences.remove(addressKey);
            } else {
                mutablePreferences.set(addressKey, address);
            }
            return Single.just(mutablePreferences);
        });
    }

    private Preferences.Key<Double> getLatKey(String type) {
        return PreferencesKeys.doubleKey("location_" + normalizeType(type) + "_lat");
    }

    private Preferences.Key<Double> getLngKey(String type) {
        return PreferencesKeys.doubleKey("location_" + normalizeType(type) + "_lng");
    }

    private Preferences.Key<String> getAddressKey(String type) {
        return PreferencesKeys.stringKey("location_" + normalizeType(type) + "_address");
    }

    private String normalizeType(String type) {
        if (type == null) {
            return TYPE_WORK.toLowerCase();
        }
        return type.toLowerCase();
    }

    public static final class WorkHomeLocation {
        @Nullable
        private final Double lat;
        @Nullable
        private final Double lng;
        @Nullable
        private final String address;

        public WorkHomeLocation(@Nullable Double lat, @Nullable Double lng, @Nullable String address) {
            this.lat = lat;
            this.lng = lng;
            this.address = address;
        }

        @Nullable
        public Double getLat() {
            return lat;
        }

        @Nullable
        public Double getLng() {
            return lng;
        }

        @Nullable
        public String getAddress() {
            return address;
        }
    }
}
