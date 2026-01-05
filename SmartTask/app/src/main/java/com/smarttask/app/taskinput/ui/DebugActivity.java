package com.smarttask.app.taskinput.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.smarttask.app.R;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        MaterialToolbar toolbar = findViewById(R.id.debug_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        View databaseButton = findViewById(R.id.debug_view_database_button);
        View logButton = findViewById(R.id.debug_view_log_button);

        databaseButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DatabaseViewerActivity.class);
            startActivity(intent);
        });

        logButton.setOnClickListener(v -> {
            // Intentionally left blank for future log viewer implementation
        });
    }
}
