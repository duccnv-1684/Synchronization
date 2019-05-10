package com.ducnguyen2102.videosynchronization;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.ducnguyen2102.videosynchronization.synchronization.SynchronizationAlgorithm;
import com.ducnguyen2102.videosynchronization.synchronization.SynchronizationAlgorithmType;

import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements SynchronizationAlgorithm.OnRequestAcceptListener {
    private String mId = UUID.randomUUID().toString();
    private SynchronizationAlgorithm mSynchronizationAlgorithm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView uuid = findViewById(R.id.uuid);
        uuid.setText(mId);
        mSynchronizationAlgorithm = new SynchronizationAlgorithm.Builder()
                .setContext(this)
                .setId(mId)
                .setListener(this)
                .setLooper(Looper.getMainLooper())
                .setSynchronizationAlgorithmType(SynchronizationAlgorithmType.CENTRALIZATION_ALGORITHM)
                .build();
        mSynchronizationAlgorithm.startSynchronize();
        uuid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        mSynchronizationAlgorithm.stopSynchronize();
        super.onDestroy();
    }

    @Override
    public void onAccepted() {

    }

}
