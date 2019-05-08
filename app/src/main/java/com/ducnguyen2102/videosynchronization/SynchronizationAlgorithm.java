package com.ducnguyen2102.videosynchronization;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.connect.NearConnect;
import com.ducnguyen.wifip2p.discovery.NearDiscovery;
import com.ducnguyen.wifip2p.model.Host;

import java.util.Set;

public abstract class SynchronizationAlgorithm implements NearConnect.Listener, NearDiscovery.Listener {
    private static final long DISCOVERABLE_TIME_OUT = 30 * 1000L;
    private static final long DISCOVERY_TIME_OUT = 15 * 1000L;
    private static final long PING_INTERVAL_TIME_OUT = 5 * 1000L;
    private Context mContext;
    private Looper mLooper;
    private String mId;
    private NearDiscovery mNearDiscovery;
    private NearConnect mNearConnect;
    private Set<Host> mHosts;

    public SynchronizationAlgorithm(Context context, Looper looper, String id) {
        mContext = context;
        mLooper = looper;
        mId = id;
        initialize();
    }

    public final void initialize() {
        mNearDiscovery = new NearDiscovery.Builder()
                .setContext(mContext)
                .setDiscoverableTimeoutMillis(DISCOVERABLE_TIME_OUT)
                .setDiscoveryTimeoutMillis(DISCOVERY_TIME_OUT)
                .setDiscoverablePingIntervalMillis(PING_INTERVAL_TIME_OUT)
                .setDiscoveryListener(this, mLooper)
                .build();
        mNearConnect = new NearConnect.Builder()
                .setContext(mContext)
                .fromDiscovery(mNearDiscovery)
                .setListener(this, mLooper)
                .build();
    }

    public final void startSynchronize() {
        mNearDiscovery.makeDiscoverable(mId);
        mNearDiscovery.startDiscovery();
        mNearConnect.startReceiving();
    }

    public void stopSynchronize() {
        mNearConnect.stopReceiving(true);
        mNearDiscovery.makeNonDiscoverable();
        mNearDiscovery.stopDiscovery();
    }

    public final void sendMessage(String message, Host host) {
        mNearConnect.send(message.getBytes(), host);
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
