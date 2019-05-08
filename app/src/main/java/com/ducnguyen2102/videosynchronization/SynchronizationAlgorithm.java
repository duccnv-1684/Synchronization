package com.ducnguyen2102.videosynchronization;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.connect.WifiP2pConnect;
import com.ducnguyen.wifip2p.discovery.WifiP2pDiscovery;
import com.ducnguyen.wifip2p.model.Host;

import java.util.Set;

public abstract class SynchronizationAlgorithm implements WifiP2pConnect.Listener, WifiP2pDiscovery.Listener {
    private static final long DISCOVERABLE_TIME_OUT = 30 * 1000L;
    private static final long DISCOVERY_TIME_OUT = 15 * 1000L;
    private static final long PING_INTERVAL_TIME_OUT = 5 * 1000L;
    private Context mContext;
    private Looper mLooper;
    private String mId;
    private WifiP2pDiscovery mWifiP2pDiscovery;
    private WifiP2pConnect mWifiP2pConnect;
    private Set<Host> mHosts;

    public SynchronizationAlgorithm(Context context, Looper looper, String id) {
        mContext = context;
        mLooper = looper;
        mId = id;
        initialize();
    }

    public final void initialize() {
        mWifiP2pDiscovery = new WifiP2pDiscovery.Builder()
                .setContext(mContext)
                .setDiscoverableTimeoutMillis(DISCOVERABLE_TIME_OUT)
                .setDiscoveryTimeoutMillis(DISCOVERY_TIME_OUT)
                .setDiscoverablePingIntervalMillis(PING_INTERVAL_TIME_OUT)
                .setDiscoveryListener(this, mLooper)
                .build();
        mWifiP2pConnect = new WifiP2pConnect.Builder()
                .setContext(mContext)
                .fromDiscovery(mWifiP2pDiscovery)
                .setListener(this, mLooper)
                .build();
    }

    public final void startSynchronize() {
        mWifiP2pDiscovery.makeDiscoverable(mId);
        mWifiP2pDiscovery.startDiscovery();
        mWifiP2pConnect.startReceiving();
    }

    public void stopSynchronize() {
        mWifiP2pConnect.stopReceiving(true);
        mWifiP2pDiscovery.makeNonDiscoverable();
        mWifiP2pDiscovery.stopDiscovery();
    }

    public final void sendMessage(String message, Host host) {
        mWifiP2pConnect.send(message.getBytes(), host);
    }

    public Set<Host> getHosts() {
        return mHosts;
    }

    public void setHosts(Set<Host> hosts) {
        mHosts = hosts;
    }

    public final String getId() {
        return mId;
    }
}
