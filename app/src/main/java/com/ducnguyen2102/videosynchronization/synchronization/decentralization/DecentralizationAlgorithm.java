package com.ducnguyen2102.videosynchronization.synchronization.decentralization;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.model.Host;
import com.ducnguyen2102.videosynchronization.synchronization.SynchronizationAlgorithm;

import java.util.Set;

public final class DecentralizationAlgorithm extends SynchronizationAlgorithm {
    public DecentralizationAlgorithm(Context context, Looper looper, String id) {
        super(context, looper, id);
    }

    @Override
    public void onReceive(byte[] bytes, Host sender) {

    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {

    }
}
