package com.ducnguyen2102.videosynchronization.synchronization;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.connect.WifiP2pConnect;
import com.ducnguyen.wifip2p.discovery.WifiP2pDiscovery;
import com.ducnguyen.wifip2p.model.Host;
import com.ducnguyen2102.videosynchronization.synchronization.centralization.CentralizationAlgorithm;
import com.ducnguyen2102.videosynchronization.synchronization.decentralization.DecentralizationAlgorithm;
import com.ducnguyen2102.videosynchronization.synchronization.distributed.DistributedAlgorithm;
import com.ducnguyen2102.videosynchronization.synchronization.tokenring.TokenRingAlgorithm;

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

    public final void stopSynchronize() {
        mWifiP2pConnect.stopReceiving(true);
        mWifiP2pDiscovery.makeNonDiscoverable();
        mWifiP2pDiscovery.stopDiscovery();
    }

    public abstract void requestAccess();

    public abstract void cancelRequest();

    public final void sendMessage(String message, Host host) {
        mWifiP2pConnect.send(message.getBytes(), host);
    }

    public final Set<Host> getHosts() {
        return mHosts;
    }

    public final void setHosts(Set<Host> hosts) {
        mHosts = hosts;
    }

    public final String getId() {
        return mId;
    }

    @Override
    public final void onSendComplete(long jobId) {

    }

    @Override
    public final void onSendFailure(Throwable e, long jobId) {

    }

    @Override
    public final void onStartListenFailure(Throwable e) {

    }

    @Override
    public final void onDiscoveryTimeout() {
        mWifiP2pDiscovery.startDiscovery();
    }

    @Override
    public final void onDiscoveryFailure(Throwable e) {

    }

    @Override
    public final void onDiscoverableTimeout() {
        mWifiP2pDiscovery.makeDiscoverable(getId());
    }

    public interface OnRequestAcceptListener {
        void onAccepted();
    }

    public static class Builder {
        private Context mContext;
        private Looper mLooper;
        private String mId;
        private SynchronizationAlgorithmType mSynchronizationAlgorithmType;
        private OnRequestAcceptListener mListener;

        public Builder() {
        }

        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }

        public Builder setLooper(Looper looper) {
            mLooper = looper;
            return this;
        }

        public Builder setId(String id) {
            mId = id;
            return this;
        }

        public Builder setSynchronizationAlgorithmType(SynchronizationAlgorithmType synchronizationAlgorithmType) {
            mSynchronizationAlgorithmType = synchronizationAlgorithmType;
            return this;
        }

        public Builder setListener(OnRequestAcceptListener listener) {
            mListener = listener;
            return this;
        }

        public SynchronizationAlgorithm build() {
            switch (mSynchronizationAlgorithmType) {
                case CENTRALIZATION_ALGORITHM:
                    return new CentralizationAlgorithm(mContext, mLooper, mId, mListener);
                case DECENTRALIZATION_ALGORITHM:
                    return new DecentralizationAlgorithm(mContext, mLooper, mId, mListener);
                case DISTRIBUTED_ALGORITHM:
                    return new DistributedAlgorithm(mContext, mLooper, mId, mListener);
                case TOKEN_RING_ALGORITHM:
                    return new TokenRingAlgorithm(mContext, mLooper, mId, mListener);
                default:
                    return null;
            }
        }

    }
}
