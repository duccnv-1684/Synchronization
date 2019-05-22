package com.ducnguyen2102.videosynchronization;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.ducnguyen.synchronization.synchronizationalgorithm.SynchronizationAlgorithm;
import com.ducnguyen.synchronization.synchronizationalgorithm.SynchronizationAlgorithmType;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;
import java.util.UUID;

public class VideoSynchronizationActivity extends AppCompatActivity implements View.OnClickListener, SynchronizationAlgorithm.OnSynchronizationEventListener {
    private SimpleExoPlayer mExoPlayer;
    private PlayerView mPlayerView;
    private AlertDialog.Builder mDialogBuilder;
    private SynchronizationAlgorithm mSynchronization;
    private SynchronizationAlgorithm.Builder mSynchronizationBuilder;
    private boolean mIsStarted;
    private TextView mStartStop;
    private ProgressDialog mProgressDialog;
    private TextView mUUID, mCoordinator, mNext, mPrevious, mCount, mQueue, mList;
    private String mId;
    private int mMessageCount = 0;
    private TextView mPeerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_synchronization);
        initUi();
        buildSynchronization();
        buildDialog();
        mDialogBuilder.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_stop:
                if (!mIsStarted) {
                    mStartStop.setText(R.string.stop_streaming);
                    mStartStop.setBackgroundResource(R.drawable.background_circle_red);
                    mSynchronization.requestAccess();
                    mIsStarted = true;
                } else {
                    mStartStop.setText(R.string.start_streaming);
                    mStartStop.setBackgroundResource(R.drawable.background_circle_blue);
                    mSynchronization.cancelRequest();
                    stopPlayingVideo();
                    mIsStarted = false;
                }
                break;
            case R.id.chage_algorithm:
                startActivity(new Intent(this, VideoSynchronizationActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mSynchronization != null) mSynchronization.stopSynchronize();
        super.onDestroy();
    }

    @Override
    public void onRequestAccepted() {
        startPlayingVideo();
    }

    @Override
    public void onSetAsCoordinator(String coordinator) {
        String message;
        if (coordinator.equals(mId)) message = "This is coordinator";
        else message = "Coordinator is " + coordinator;
        mCoordinator.setText(message);
    }

    @Override
    public void onNextHostFound(String id) {
        mNext.setText("Next: "+id);
    }

    @Override
    public void onPreviousHostFound(String id) {
        mPrevious.setText("Previous: "+id);
    }

    @Override
    public void onMessageSent() {
        mMessageCount++;
        mCount.setText(mMessageCount+" message(s) was sent");
    }

    @Override
    public void onQueueAdded(List<String> strings) {
        mQueue.setVisibility(View.VISIBLE);
        StringBuilder stringBuilder = new StringBuilder();
        for (String string:strings){            stringBuilder.append(string);

            stringBuilder.append("\n");
        }
        mList.setText(stringBuilder.toString());
    }

    @Override
    public void onPeerFind(int count) {
        mPeerCount.setText(count + "peer(s)");
    }

    private void startPlayingVideo() {
        Uri uri = Uri.parse("rtmp://192.168.0.108/live/ducnguyen");
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        mPlayerView.setPlayer(mExoPlayer);
        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
        ExtractorMediaSource mediaSource = new ExtractorMediaSource
                .Factory(rtmpDataSourceFactory).createMediaSource(uri);
        mExoPlayer.prepare(mediaSource);
        mPlayerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        mExoPlayer.setPlayWhenReady(true);
    }

    private void stopPlayingVideo() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
        }
    }

    private void initUi() {
        mStartStop = findViewById(R.id.start_stop);
        mStartStop.setOnClickListener(this);
        findViewById(R.id.chage_algorithm).setOnClickListener(this);
        mUUID = findViewById(R.id.uuid);
        mPlayerView = findViewById(R.id.player_view);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.R_string_msg_prepare));
        mProgressDialog.setCancelable(false);
        mCoordinator = findViewById(R.id.coordinator);
        mNext = findViewById(R.id.next);
        mPrevious = findViewById(R.id.previous);
        mCount = findViewById(R.id.count);
        mList = findViewById(R.id.list);
        mQueue = findViewById(R.id.queue);
        mPeerCount = findViewById(R.id.peers);
    }

    private void buildSynchronization() {
        mId = UUID.randomUUID().toString();
        mUUID.setText("ID: "+mId);
        mSynchronizationBuilder = new SynchronizationAlgorithm.Builder()
                .setId(mId)
                .setContext(this)
                .setLooper(Looper.getMainLooper())
                .setListener(this);
    }

    private void buildDialog() {
        mDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Select Synchronization Algorithm")
                .setCancelable(false)
                .setItems(R.array.synchronization_algorithms, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            mSynchronizationBuilder.setSynchronizationAlgorithmType(SynchronizationAlgorithmType.CENTRALIZED_ALGORITHM);
                            break;
                        case 1:
                            mSynchronizationBuilder.setSynchronizationAlgorithmType(SynchronizationAlgorithmType.DISTRIBUTED_ALGORITHM);
                            break;
                        case 2:
                            mSynchronizationBuilder.setSynchronizationAlgorithmType(SynchronizationAlgorithmType.TOKEN_RING_ALGORITHM);
                            break;
                        default:
                            break;
                    }
                    mSynchronization = mSynchronizationBuilder.build();
                    mSynchronization.startSynchronize();
                    mProgressDialog.show();
                    new Handler().postDelayed(() -> mProgressDialog.dismiss(), 3000);
                });
    }

}
