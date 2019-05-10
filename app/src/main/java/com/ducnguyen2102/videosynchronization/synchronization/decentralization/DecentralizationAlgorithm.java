package com.ducnguyen2102.videosynchronization.synchronization.decentralization;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.model.Host;
import com.ducnguyen2102.videosynchronization.synchronization.SynchronizationAlgorithm;

import java.util.Set;

public final class DecentralizationAlgorithm extends SynchronizationAlgorithm {
    private OnRequestAcceptListener mListener;

    public DecentralizationAlgorithm(Context context, Looper looper, String id, OnRequestAcceptListener listener) {
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
