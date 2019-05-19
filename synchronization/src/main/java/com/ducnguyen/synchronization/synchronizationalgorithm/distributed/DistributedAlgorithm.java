package com.ducnguyen.synchronization.synchronizationalgorithm.distributed;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.synchronization.synchronizationalgorithm.SynchronizationAlgorithm;
import com.ducnguyen.synchronization.wifip2p.model.Host;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public final class DistributedAlgorithm extends SynchronizationAlgorithm {
    private boolean mIsRequesting;
    private boolean mIsAccessing;
    private List<Host> mRequestingHosts;
    private List<String> mAcceptedHostIds;
    private List<Host> mRequestQueue;
    private long mTimeStamp;
    private OnSynchronizationEventListener mListener;

    public DistributedAlgorithm(Context context, Looper looper, String id, OnSynchronizationEventListener listener) {
        super(context, looper, id);
        mListener = listener;
        mRequestQueue = new ArrayList<>();
    }

    @Override
    public void requestAccess() {
        mRequestingHosts = new ArrayList<>(getHosts());
        mIsAccessing = false;
        mIsRequesting = true;
        mAcceptedHostIds = new ArrayList<>();
        mTimeStamp = Calendar.getInstance().getTimeInMillis();
        for (Host host : mRequestingHosts) {
            String timeStamp = String.valueOf(mTimeStamp);
            sendMessage(DistributedMessage.messageRequestAccess(timeStamp), host);
        }
    }

    @Override
    public void cancelRequest() {
        mIsAccessing = false;
        mIsRequesting = false;
        for (Host host : mRequestQueue)
            sendMessage(DistributedMessage.messageReplyOk(getId()), host);
    }

    @Override
    public void onReceive(byte[] bytes, Host sender) {
        String message = new String(bytes);
        switch (DistributedMessage.getMessagePrefix(message)) {
            case DistributedMessage.MESSAGE_REQUEST_ACCESS:
                if (mIsAccessing) {
                    mRequestQueue.add(sender);
                } else if (mIsRequesting) {
                    long timeStamp = Long.valueOf(DistributedMessage.getMessageContent(message));
                    if (this.mTimeStamp > timeStamp)
                        sendMessage(DistributedMessage.messageReplyOk(getId()), sender);
                    else mRequestQueue.add(sender);
                } else {
                    sendMessage(DistributedMessage.messageReplyOk(getId()), sender);
                }
                break;
            case DistributedMessage.MESSAGE_REPLY_OK:
                mAcceptedHostIds.add(DistributedMessage.getMessageContent(message));
                if (!mIsAccessing && mAcceptedHostIds.size() == mRequestingHosts.size()) {
                    mIsAccessing = true;
                    mIsRequesting = false;
                    mListener.onRequestAccepted();
                }
                break;
        }
    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {
        setHosts(hosts);
    }
}
