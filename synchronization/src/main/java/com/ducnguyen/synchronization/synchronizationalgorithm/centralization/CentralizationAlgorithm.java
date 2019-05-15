package com.ducnguyen.synchronization.synchronizationalgorithm.centralization;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.synchronization.synchronizationalgorithm.SynchronizationAlgorithm;
import com.ducnguyen.synchronization.wifip2p.model.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CentralizationAlgorithm extends SynchronizationAlgorithm
        implements CentralizationRequestQueue.OnQueueChangeListener {
    private Host mCoordinator;
    private String mCoordinatorId;
    private int mCoordinatorHopeCount = 0;
    private int mElectionHopeCount = 0;
    private boolean mIsFindingCoordinator;
    private boolean mIsCoordinatorFound;
    private boolean mIsConnectedToCoordinator;
    private boolean mIsElecting;
    private boolean mIsDenied;
    private List<String> mRequestQueue;
    private OnRequestAcceptListener mListener;

    public CentralizationAlgorithm(Context context, Looper looper, String id, OnRequestAcceptListener listener) {
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
                mIsFindingCoordinator = false;
                mIsCoordinatorFound = true;
                mCoordinatorId = CentralizationMessage.getMessageContent(message);
                connectToCoordinator();

            case CentralizationMessage.MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX:
                mCoordinatorHopeCount--;
                if (mCoordinatorHopeCount == 0) {
                    mIsFindingCoordinator = false;
                    startCoordinatorElection();
                }
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

            case CentralizationMessage.MESSAGE_REPLY_GIVE_ACCESS_PREFIX:
                mListener.onAccepted();
                break;
            case CentralizationMessage.MESSAGE_REQUEST_ELECTION_PREFIX:
                if (getId().compareTo(host.getName()) > 0) {
                    sendMessage(CentralizationMessage.messageReplyElectionDenyElection(getId()), host);
                    if (!mIsElecting) startCoordinatorElection();
                } else {
                    mIsDenied = true;
                    sendMessage(CentralizationMessage.messageReplyElectionAccept(getId()), host);
                }
                break;
            case CentralizationMessage.MESSAGE_REPLY_ELECTION_ACCEPT_PREFIX:
                mElectionHopeCount--;
                if (mElectionHopeCount == 0) {
                    setAsCoordinator();
                    mIsElecting = false;
                    mIsDenied = false;
                }
                break;
            case CentralizationMessage.MESSAGE_REPLY_ELECTION_DENY_PREFIX:
                mIsElecting = false;
                mIsDenied = true;
                break;
            default:
                break;

        }
    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {
        setHosts(hosts);
        findCoordinator();
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

    @Override
    public void requestAccess() {
        if (mCoordinatorId.equals(getId())) mRequestQueue.add(getId());
        else sendMessage(CentralizationMessage.messageRequestEnqueue(getId()), mCoordinator);
    }

    @Override
    public void cancelRequest() {
        sendMessage(CentralizationMessage.messageRequestDequeue(getId()), mCoordinator);
    }

    private void startCoordinatorElection() {
        mIsCoordinatorFound = false;
        mIsConnectedToCoordinator = false;
        mIsElecting = true;
        mIsDenied = false;
        List<Host> hosts = new ArrayList<>(getHosts());
        mElectionHopeCount = hosts.size();
        for (Host host : hosts) {
            if (mIsDenied) return;
            sendMessage(CentralizationMessage.messageRequestElection(getId()), host);
        }
    }

    private void setAsCoordinator() {
        mIsDenied = false;
        mIsElecting = false;
        mIsCoordinatorFound = true;
        mIsConnectedToCoordinator = true;
        mCoordinatorId = getId();
        mRequestQueue = new CentralizationRequestQueue<>(this);
        List<Host> hosts = new ArrayList<>(getHosts());
        for (Host host : hosts)
            sendMessage(CentralizationMessage.messageReplyCoordinatorFound(mCoordinatorId), host);

    }

    private void findCoordinator() {
        if (mIsConnectedToCoordinator || mIsFindingCoordinator || mIsElecting) return;
        if (mIsCoordinatorFound) {
            connectToCoordinator();
            return;
        }
        mIsFindingCoordinator = true;
        List<Host> hosts = new ArrayList<>(getHosts());
        mCoordinatorHopeCount = hosts.size();
        for (Host host : hosts)
            sendMessage(CentralizationMessage.messageRequestCoordinator(getId()), host);
    }

    private void connectToCoordinator() {
        for (Host host : new ArrayList<>(getHosts())) {
            if (host.getName().equals(mCoordinatorId)) {
                mCoordinator = host;
                mIsCoordinatorFound = true;
                break;
            }
        }
    }
}