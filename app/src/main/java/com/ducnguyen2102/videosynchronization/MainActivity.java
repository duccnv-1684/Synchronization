package com.ducnguyen2102.videosynchronization;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.ducnguyen2102.videosynchronization.synchronization.centralization.CentralizationAlgorithm;

import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements CentralizationAlgorithm.OnCentralizationSynchronizationListener {
    private CentralizationAlgorithm mCentralizationAlgorithm;
    private String id = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView uuid = findViewById(R.id.uuid);
        uuid.setText(id);
        mCentralizationAlgorithm =
                new CentralizationAlgorithm(this, Looper.getMainLooper(), id, this);
        uuid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCentralizationAlgorithm.makeCoordinator();
            }
        });
        mCentralizationAlgorithm.startSynchronize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCentralizationAlgorithm.stopSynchronize();
    }

    @Override
    public void onAccepted() {

    }

    @Override
    public void onCoordinatorFound() {
        mCentralizationAlgorithm.requestAccess();
    }

}
