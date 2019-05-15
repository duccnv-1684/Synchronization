package com.ducnguyen2102.videosynchronization;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.Objects;

public class VideoSynchronizationActivity extends AppCompatActivity implements View.OnClickListener {
    private SimpleExoPlayer mExoPlayer;
    private TextInputLayout mAddress, mApp, mKey;
    private PlayerView mPlayerView;
    private AlertDialog.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_synchronization);
        mAddress = findViewById(R.id.address);
        mApp = findViewById(R.id.app);
        mKey = findViewById(R.id.key);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        mPlayerView = findViewById(R.id.player_view);
        mBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.msg_title_dialog)
                .setItems(R.array.synchronization_algorithms, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                });
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        mPlayerView.setPlayer(mExoPlayer);
        Uri uri = Uri.parse("rtmp://184.72.239.149/vod/mp4:bigbuckbunny_1500.mp4");
        startPlayingVideo(uri);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                mAddress.setError(null);
                mApp.setError(null);
                mKey.setError(null);
                String address = Objects.requireNonNull(mAddress.getEditText()).getText().toString();
                if (address.isEmpty()) {
                    mAddress.setError(getString(R.string.error_address_invalid));
                    return;
                }
                String app = Objects.requireNonNull(mApp.getEditText()).getText().toString();
                if (app.isEmpty()) {
                    mApp.setError(getString(R.string.error_app_name_invalid));
                    return;
                }
                String key = Objects.requireNonNull(mKey.getEditText()).getText().toString();
                if (key.isEmpty()) {
                    mKey.setError(getString(R.string.error_id_invalid));
                    return;
                }
                mBuilder.show();
                break;
            case R.id.stop:
                break;
        }
    }

    private void startPlayingVideo(Uri uri) {
        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
        ExtractorMediaSource mediaSource = new ExtractorMediaSource
                .Factory(rtmpDataSourceFactory).createMediaSource(uri);
        mExoPlayer.prepare(mediaSource);
        mPlayerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        mExoPlayer.setPlayWhenReady(true);
    }
}
