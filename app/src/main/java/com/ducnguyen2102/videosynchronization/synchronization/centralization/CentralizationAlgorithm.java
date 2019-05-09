package com.ducnguyen2102.videosynchronization.synchronization.centralization;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.model.Host;
import com.ducnguyen2102.videosynchronization.synchronization.SynchronizationAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CentralizationAlgorithm extends SynchronizationAlgorithm
        implements CentralizationRequestQueue.OnQueueChangeListener {
    private Host mCoordinator;
    private String mCoordinatorId;
    private boolean mIsCoordinatorFound;
    private boolean mIsConnectedToCoordinator;
    private List<String> mRequestQueue;
    private OnCentralizationSynchronizationListener mListener;

    public CentralizationAlgorithm(Context context, Looper looper, String id, OnCentralizationSynchronizationListener listener) {
        super(context, looper, id);
        mListener = listener;
    }

    @Override
    public void onReceive(byte[] bytes, Host host) {
        String message = new String(bytes);
        switch (CentralizationMessage.getMessagePrefix(message)) {
            case CentralizationMessage.MESSAGE_REQUEST_COORDINATOR_PREFIX:
                //TODO Done
                if (mIsCoordinatorFound)
                    sendMessage(CentralizationMessage.messageReplyCoordinatorFound(mCoordinatorId), host);
                else
                    sendMessage(CentralizationMessage.messageReplyCoordinatorNotFound(getId()), host);
                break;

            case CentralizationMessage.MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX:
                //TODO Done
                mIsCoordinatorFound = true;
                mCoordinatorId = CentralizationMessage.getMessageContent(message);
                findCoordinatorHost();

            case CentralizationMessage.MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX:
                //TODO Done
                break;

            case CentralizationMessage.MESSAGE_REQUEST_ENQUEUE_PREFIX:
                mRequestQueue.add(CentralizationMessage.getMessageContent(message));
                sendMessage(CentralizationMessage.messageReplyEnqueue(getId()), host);
                break;

            case CentralizationMessage.MESSAGE_REPLY_ENQUEUE_PREFIX:
                //TODO Done
                break;

            case CentralizationMessage.MESSAGE_REQUEST_DEQUEUE_PREFIX:
                mRequestQueue.remove(CentralizationMessage.getMessageContent(message));
                sendMessage(CentralizationMessage.messageReplyDequeue(getId()), host);
                break;

            case CentralizationMessage.MESSAGE_REPLY_DEQUEUE_PREFIX:
                //TODO Done
                break;

            case CentralizationMessage.MESSAGE_REPLY_GIVE_ACCESS:
                mListener.onAccepted();
            default:
                break;

        }
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
                sendMessage(CentralizationMessage.messageRequestCoordinator(getId()), host);
        }
    }

    @Override
    public void onDataChanged() {
        String accessId = mRequestQueue.get(0);
        for (Host host : getHosts()) {
            if (host.getName().equals(accessId)) {
                sendMessage(CentralizationMessage.messageReplyGiveAccess(getId()), host);
                break;
            }
        }
    }

    public void makeCoordinator() {
        mIsCoordinatorFound = true;
        mCoordinatorId = getId();
        mRequestQueue = new CentralizationRequestQueue<>(this);
        mListener.onCoordinatorFound();
    }

    @Override
    public void requestAccess() {
        if (mCoordinatorId.equals(getId())) mRequestQueue.add(getId());
        else sendMessage(CentralizationMessage.messageRequestEnqueue(getId()), mCoordinator);
    }

    @Override
    public void cancelRequest() {
        sendMessage(CentralizationMessage.messageRequestDequeue(getId()), mCoordinator);
    }

    private void findCoordinatorHost() {
        for (Host host : new ArrayList<>(getHosts())) {
            if (host.getName().equals(mCoordinatorId)) {
                mCoordinator = host;
                break;
            }
        }
    }

    public interface OnCentralizationSynchronizationListener {
        void onAccepted();

        void onCoordinatorFound();
    }
}