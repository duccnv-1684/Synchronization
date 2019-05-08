package com.ducnguyen2102.videosynchronization;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.model.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CentralizationAlgorithm extends SynchronizationAlgorithm
        implements AccessRequestQueue.OnQueueChangeListener {
    private Host mCoordinator;
    private String mCoordinatorId;
    private boolean mIsCoordinatorFound;
    private boolean mIsConnectedToCoordinator;
    private List<String> mQueue;

    public CentralizationAlgorithm(Context context, Looper looper, String id) {
        super(context, looper, id);
    }

    public void makeCoordinator() {
        mIsCoordinatorFound = true;
        mCoordinatorId = getId();
        mQueue = new AccessRequestQueue<>(this);
    }

    @Override
    public void onReceive(byte[] bytes, Host host) {
        String message = new String(bytes);
        String messagePrefix = Message.getMessagePrefix(message);
        switch (messagePrefix) {
            case Message.MESSAGE_REQUEST_COORDINATOR_PREFIX:
                //TODO Done
                if (mIsCoordinatorFound)
                    sendMessage(Message.messageReplyCoordinatorFound(mCoordinatorId), host);
                else sendMessage(Message.messageReplyCoordinatorNotFound(getId()), host);
                break;

            case Message.MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX:
                //TODO Done
                mIsCoordinatorFound = true;
                mCoordinatorId = Message.getMessageContent(message);
                findCoordinatorHost();

            case Message.MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX:
                //TODO Done
                break;

            case Message.MESSAGE_REQUEST_ENQUEUE_PREFIX:
                mQueue.add(Message.getMessageContent(message));
                sendMessage(Message.messageReplyEnqueue(getId()), host);
                break;

            case Message.MESSAGE_REPLY_ENQUEUE_PREFIX:
                //TODO Done
                break;

            case Message.MESSAGE_REQUEST_DEQUEUE_PREFIX:
                mQueue.remove(Message.getMessageContent(message));
                sendMessage(Message.messageReplyDequeue(getId()), host);
                break;

            case Message.MESSAGE_REPLY_DEQUEUE_PREFIX:
                //TODO Done
                break;

            case Message.MESSAGE_REPLY_GIVE_ACCESS:

            default:
                break;

        }
    }

    private void findCoordinatorHost() {
        for (Host host : new ArrayList<>(getHosts())) {
            if (host.getName().equals(mCoordinatorId)) {
                mCoordinator = host;
                break;
            }
        }
    }

    @Override
    public void onSendComplete(long l) {

    }

    @Override
    public void onSendFailure(Throwable throwable, long l) {

    }

    @Override
    public void onStartListenFailure(Throwable throwable) {

    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {
        setHosts(hosts);
        if (mIsConnectedToCoordinator) return;
        for (Host host : new ArrayList<>(getHosts())) {
            if (mIsCoordinatorFound) {
                if (host.getName().equals(mCoordinatorId)) {
                    mCoordinator = host;
                    mIsConnectedToCoordinator = true;
                    break;
                }
            } else
                sendMessage(Message.messageRequestCoordinator(getId()), host);
        }
    }

    public void enqueue() {
        sendMessage(Message.messageRequestEnqueue(getId()), mCoordinator);
    }

    public void dequeue() {
        sendMessage(Message.messageRequestDequeue(getId()), mCoordinator);
    }


    @Override
    public void onDiscoveryTimeout() {

    }

    @Override
    public void onDiscoveryFailure(Throwable throwable) {

    }

    @Override
    public void onDiscoverableTimeout() {

    }

    @Override
    public void onItemChanged() {
        String accessId = mQueue.get(0);
        for (Host host : getHosts()) {
            if (host.getName().equals(accessId)) {
                sendMessage(Message.messageReplyGiveAccess(getId()), host);
                break;
            }
        }
    }
}