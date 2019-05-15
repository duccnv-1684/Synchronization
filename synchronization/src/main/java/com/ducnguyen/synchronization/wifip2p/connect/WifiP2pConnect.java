package com.ducnguyen.synchronization.wifip2p.connect;

import android.content.Context;
import android.os.Looper;


import com.ducnguyen.synchronization.wifip2p.discovery.WifiP2pDiscovery;
import com.ducnguyen.synchronization.wifip2p.model.Host;

import java.util.Set;

/**
 * Created by pv on 21/06/17.
 */

public interface WifiP2pConnect {

    long send(byte[] bytes, Host peer);

    void startReceiving();

    void stopReceiving(boolean abortCurrentTransfers);

    Set<Host> getPeers();

    boolean isReceiving();

    class Builder {
        private Context mContext;
        private Listener mListener;
        private Looper mListenerLooper;
        private Set<Host> mPeers;

        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }

        public Builder setListener(Listener listener, Looper listenerLooper) {
            mListener = listener;
            mListenerLooper = listenerLooper;
            return this;
        }

        public Builder fromDiscovery(WifiP2pDiscovery discovery) {
            if (discovery != null) {
                mPeers = discovery.getAllAvailablePeers();
            } else {
                mPeers = null;
            }
            return this;
        }

        public Builder forPeers(Set<Host> peers) {
            mPeers = peers;
            return this;
        }

        public WifiP2pConnect build() {
            return new WifiP2pConnectImpl(mContext, mListener, mListenerLooper, mPeers);
        }
    }

    interface Listener {
        void onReceive(byte[] bytes, Host sender);

        void onSendComplete(long jobId);

        void onSendFailure(Throwable e, long jobId);

        void onStartListenFailure(Throwable e);
    }
}
