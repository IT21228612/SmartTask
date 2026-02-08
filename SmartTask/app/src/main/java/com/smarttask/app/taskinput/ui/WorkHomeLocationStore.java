package com.smarttask.app.taskinput.ui;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import io.reactivex.rxjava3.core.Single;

public class WorkHomeLocationStore {

    private static final String PREFERENCES_FILE_NAME = "app_settings";
    private static final String TYPE_WORK = "Work";

    private final SharedPreferences sharedPreferences;

    public WorkHomeLocationStore(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        sharedPreferences = appContext.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public Single<WorkHomeLocation> getLocation(@NonNull String type) {
        return Single.fromCallable(() -> {
            String keyType = normalizeType(type);
            String latKey = getLatKey(keyType);
            String lngKey = getLngKey(keyType);
            String addressKey = getAddressKey(keyType);

            Double lat = sharedPreferences.contains(latKey)
                    ? Double.longBitsToDouble(sharedPreferences.getLong(latKey, 0L))
                    : null;
            Double lng = sharedPreferences.contains(lngKey)
                    ? Double.longBitsToDouble(sharedPreferences.getLong(lngKey, 0L))
                    : null;
            String address = sharedPreferences.getString(addressKey, null);

            return new WorkHomeLocation(lat, lng, address);
        });
    }

    public Single<Boolean> saveLocation(@NonNull String type,
                                        double lat,
                                        double lng,
                                        @Nullable String address) {
        return Single.fromCallable(() -> {
            String keyType = normalizeType(type);
            String latKey = getLatKey(keyType);
            String lngKey = getLngKey(keyType);
            String addressKey = getAddressKey(keyType);

            SharedPreferences.Editor editor = sharedPreferences.edit()
                    .putLong(latKey, Double.doubleToRawLongBits(lat))
                    .putLong(lngKey, Double.doubleToRawLongBits(lng));

            if (address == null || address.trim().isEmpty()) {
                editor.remove(addressKey);
            } else {
                editor.putString(addressKey, address);
            }

            return editor.commit();
        });
    }

    private String getLatKey(String normalizedType) {
        return "location_" + normalizedType + "_lat";
    }

    private String getLngKey(String normalizedType) {
        return "location_" + normalizedType + "_lng";
    }

    private String getAddressKey(String normalizedType) {
        return "location_" + normalizedType + "_address";
    }

    private String normalizeType(String type) {
        if (type == null) {
            return TYPE_WORK.toLowerCase(Locale.US);
        }
        return type.toLowerCase(Locale.US);
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
