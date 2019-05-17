package com.ducnguyen.synchronization.synchronizationalgorithm.centralized;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.synchronization.synchronizationalgorithm.SynchronizationAlgorithm;
import com.ducnguyen.synchronization.wifip2p.model.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CentralizedAlgorithm extends SynchronizationAlgorithm
        implements CentralizedRequestQueue.OnQueueChangeListener {
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
    private OnSynchronizationEventListener mListener;
    private int mPreviousHopeCount;

    public CentralizedAlgorithm(Context context, Looper looper, String id, OnSynchronizationEventListener listener) {
        super(context, looper, id);
        mListener = listener;
    }

    @Override
    public void onReceive(byte[] bytes, Host host) {
        String message = new String(bytes);
        switch (CentralizedMessage.getMessagePrefix(message)) {
            case CentralizedMessage.MESSAGE_REQUEST_COORDINATOR_PREFIX:
                if (mIsCoordinatorFound)
                    sendMessage(CentralizedMessage.messageReplyCoordinatorFound(mCoordinatorId), host);
                else
                    sendMessage(CentralizedMessage.messageReplyCoordinatorNotFound(getId()), host);
                break;

            case CentralizedMessage.MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX:
                mIsFindingCoordinator = false;
                mIsCoordinatorFound = true;
                mCoordinatorId = CentralizedMessage.getMessageContent(message);
                connectToCoordinator();

            case CentralizedMessage.MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX:
                mCoordinatorHopeCount--;
                if (mCoordinatorHopeCount == 0) {
                    mIsFindingCoordinator = false;
                    if (mPreviousHopeCount != getHosts().size()) {
                        findCoordinator();
                    } else {
                        startCoordinatorElection();
                    }
                }
                break;

            case CentralizedMessage.MESSAGE_REQUEST_ELECTION_PREFIX:
                if (getId().compareTo(host.getName()) > 0) {
                    sendMessage(CentralizedMessage.messageReplyElectionDenyElection(getId()), host);
                    if (!mIsElecting && !mIsDenied) startCoordinatorElection();
                } else {
                    mIsDenied = true;
                    sendMessage(CentralizedMessage.messageReplyElectionAccept(getId()), host);
                }
                break;

            case CentralizedMessage.MESSAGE_REPLY_ELECTION_ACCEPT_PREFIX:
                mElectionHopeCount--;
                if (mElectionHopeCount == 0) {
                    mIsElecting = false;
                    mIsDenied = false;
                    setAsCoordinator();
                }
                break;
            case CentralizedMessage.MESSAGE_REPLY_ELECTION_DENY_PREFIX:
                mIsElecting = false;
                mIsDenied = true;
                break;

            case CentralizedMessage.MESSAGE_REQUEST_ENQUEUE_PREFIX:
                mRequestQueue.add(CentralizedMessage.getMessageContent(message));
                sendMessage(CentralizedMessage.messageReplyEnqueue(getId()), host);
                break;

            case CentralizedMessage.MESSAGE_REPLY_ENQUEUE_PREFIX:
                break;

            case CentralizedMessage.MESSAGE_REQUEST_DEQUEUE_PREFIX:
                mRequestQueue.remove(CentralizedMessage.getMessageContent(message));
                sendMessage(CentralizedMessage.messageReplyDequeue(getId()), host);
                break;

            case CentralizedMessage.MESSAGE_REPLY_DEQUEUE_PREFIX:
                break;

            case CentralizedMessage.MESSAGE_REPLY_GIVE_ACCESS_PREFIX:
                mListener.onAccepted();
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
                sendMessage(CentralizedMessage.messageReplyGiveAccess(getId()), host);
                break;
            }
        }
    }

    @Override
    public void requestAccess() {
        if (mCoordinatorId.equals(getId())) mRequestQueue.add(getId());
        else sendMessage(CentralizedMessage.messageRequestEnqueue(getId()), mCoordinator);
    }

    @Override
    public void cancelRequest() {
        sendMessage(CentralizedMessage.messageRequestDequeue(getId()), mCoordinator);
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
            sendMessage(CentralizedMessage.messageRequestElection(getId()), host);
        }
    }

    private void setAsCoordinator() {
        mIsDenied = false;
        mIsElecting = false;
        mIsCoordinatorFound = true;
        mIsConnectedToCoordinator = true;
        mCoordinatorId = getId();
        mRequestQueue = new CentralizedRequestQueue<>(this);
        List<Host> hosts = new ArrayList<>(getHosts());
        for (Host host : hosts)
            sendMessage(CentralizedMessage.messageReplyCoordinatorFound(mCoordinatorId), host);
        mListener.onReady();
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
        mPreviousHopeCount = mCoordinatorHopeCount;
        for (Host host : hosts)
            sendMessage(CentralizedMessage.messageRequestCoordinator(getId()), host);
    }

    private void connectToCoordinator() {
        for (Host host : new ArrayList<>(getHosts())) {
            if (host.getName().equals(mCoordinatorId)) {
                mCoordinator = host;
                mIsConnectedToCoordinator = true;
                mListener.onReady();
                return;
            }
        }
        connectToCoordinator();
    }
}