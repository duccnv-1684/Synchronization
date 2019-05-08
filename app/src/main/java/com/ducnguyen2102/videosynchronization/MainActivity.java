package com.ducnguyen2102.videosynchronization;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.ducnguyen.wifip2p.connect.WifiP2pConnect;
import com.ducnguyen.wifip2p.discovery.WifiP2pDiscovery;
import com.ducnguyen.wifip2p.model.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements WifiP2pDiscovery.Listener, WifiP2pConnect.Listener {
    private WifiP2pDiscovery mWifiP2pDiscovery;
    private WifiP2pConnect mWifiP2pConnect;
    private String id = UUID.randomUUID().toString();
    private Host mHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWifiP2p();
        startDiscoveryAndReceive();
        TextView uuid = findViewById(R.id.uuid);
        uuid.setText(id);
        uuid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiP2pConnect.send(("Message from " + id + " to " + mHost.getName()).getBytes(), mHost);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("ducnguyen", "onDestroy");
        mWifiP2pConnect.stopReceiving(true);
        mWifiP2pDiscovery.makeNonDiscoverable();
        mWifiP2pDiscovery.stopDiscovery();
    }

    @Override
    public void onReceive(byte[] bytes, Host host) {
        String message = new String(bytes);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e("ducnguyen", message);
    }

    @Override
    public void onSendComplete(long l) {
        Log.e("ducnguyen", "onSendComplete");

    }

    @Override
    public void onSendFailure(Throwable throwable, long l) {
        Log.e("ducnguyen", "onSendFailure");
    }

    @Override
    public void onStartListenFailure(Throwable throwable) {
        Log.e("ducnguyen", "onStartListenFailure");
    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {
        List<Host> peers = new ArrayList<>(hosts);
        for (Host peer : peers) {
            Log.e("ducnguyen", "Found " + peer.getName());
            mHost = peer;
//            mWifiP2pConnect.send(("Message from " + id + "to " + peer.getName()).getBytes(), peer);
        }
    }

    @Override
    public void onDiscoveryTimeout() {
        Log.e("ducnguyen", "onDiscoveryTimeout");
    }

    @Override
    public void onDiscoveryFailure(Throwable throwable) {
        Log.e("ducnguyen", "onDiscoveryFailure");
    }

    @Override
    public void onDiscoverableTimeout() {
        Log.e("ducnguyen", "onDiscoverableTimeout");
    }

    private void initWifiP2p(){
        mWifiP2pDiscovery = new WifiP2pDiscovery.Builder()
                .setContext(this)
                .setDiscoverableTimeoutMillis(6000000)
                .setDiscoveryTimeoutMillis(2000000)
                .setDiscoverablePingIntervalMillis(15000)
                .setDiscoveryListener(this, Looper.getMainLooper())
                .build();
        mWifiP2pConnect = new WifiP2pConnect.Builder()
                .setContext(this)
                .fromDiscovery(mWifiP2pDiscovery)
                .setListener(this, Looper.getMainLooper())
                .build();
    }
    private void startDiscoveryAndReceive(){
        mWifiP2pDiscovery.makeDiscoverable(id);
        mWifiP2pDiscovery.startDiscovery();
        mWifiP2pConnect.startReceiving();
    }
}
