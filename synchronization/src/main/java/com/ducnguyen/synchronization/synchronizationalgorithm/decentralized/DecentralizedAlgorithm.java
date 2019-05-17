package com.ducnguyen.synchronization.synchronizationalgorithm.decentralized;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.synchronization.synchronizationalgorithm.SynchronizationAlgorithm;
import com.ducnguyen.synchronization.wifip2p.model.Host;

import java.util.Set;

public final class DecentralizedAlgorithm extends SynchronizationAlgorithm {
    private OnSynchronizationEventListener mListener;

    public DecentralizedAlgorithm(Context context, Looper looper, String id, OnSynchronizationEventListener listener) {
        super(context, looper, id);
        this.mListener = listener;
    }

    @Override
    public void requestAccess() {

    }

    @Override
    public void cancelRequest() {

    }

    @Override
    public void onReceive(byte[] bytes, Host sender) {

    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {

    }
}
