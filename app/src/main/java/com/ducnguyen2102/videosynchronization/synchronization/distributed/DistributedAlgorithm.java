package com.ducnguyen2102.videosynchronization.synchronization.distributed;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.model.Host;
import com.ducnguyen2102.videosynchronization.synchronization.SynchronizationAlgorithm;

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

    public DistributedAlgorithm(Context context, Looper looper, String id) {
        super(context, looper, id);
        mRequestQueue = new ArrayList<>();
    }

    public final void requestAccess() {
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

    private void startAccessing() {
        mIsAccessing = true;
        mIsRequesting = false;
    }

    private void stopAccessing() {
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
                if (!mIsAccessing && !mIsRequesting)
                    sendMessage(DistributedMessage.messageReplyOk(getId()), sender);
                else if (mIsRequesting) {
                    long timeStamp = Long.valueOf(DistributedMessage.getMessageContent(message));
                    if (this.mTimeStamp > timeStamp)
                        sendMessage(DistributedMessage.messageReplyOk(getId()), sender);
                    else mRequestQueue.add(sender);
                }
                break;
            case DistributedMessage.MESSAGE_REPLY_OK:
                mAcceptedHostIds.add(DistributedMessage.getMessageContent(message));
                if (!mIsAccessing && mAcceptedHostIds.size() == mRequestingHosts.size())
                    startAccessing();
                break;
        }
    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {
        setHosts(hosts);
    }
}
