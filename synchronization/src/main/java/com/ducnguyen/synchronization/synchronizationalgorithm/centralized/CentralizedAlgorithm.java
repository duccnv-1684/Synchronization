package com.ducnguyen.synchronization.synchronizationalgorithm.centralized;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.synchronization.synchronizationalgorithm.SynchronizationAlgorithm;
import com.ducnguyen.synchronization.wifip2p.model.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CentralizedAlgorithm extends SynchronizationAlgorithm {
    private Host mCoordinator;
    private String mCoordinatorId;
    private int mCoordinatorHopeCount = 0;
    private int mElectionHopeCount = 0;
    private boolean mIsFindingCoordinator;
    private boolean mIsCoordinatorFound;
    private boolean mIsConnectedToCoordinator;
    private boolean mIsElecting;
    private boolean mIsDenied;
    private boolean mIsPending;
    private int mPreviousHopeCount;
    private List<String> mRequestQueue;
    private OnSynchronizationEventListener mListener;

    public CentralizedAlgorithm(Context context, Looper looper, String id, OnSynchronizationEventListener listener) {
        super(context, looper, id, listener);
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
                String coordinatorId = CentralizedMessage.getMessageContent(message);
                if (mCoordinator == null || coordinatorId.compareTo(mCoordinatorId) > 0) {
                    mIsFindingCoordinator = false;
                    mIsCoordinatorFound = true;
                    mCoordinatorId = CentralizedMessage.getMessageContent(message);
                    connectToCoordinator();
                }
                break;

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
                mListener.onQueueAdded(mRequestQueue);
                if (mRequestQueue.size() == 1) {
                    sendMessage(CentralizedMessage.messageReplyGiveAccess(getId()), host);
                }

                break;


            case CentralizedMessage.MESSAGE_REQUEST_DEQUEUE_PREFIX:
                int index = mRequestQueue.indexOf(CentralizedMessage.getMessageContent(message));
                mRequestQueue.remove(index);
                mListener.onQueueAdded(mRequestQueue);
                if (index == 0 && mRequestQueue.size() != 0) {
                    String accessId = mRequestQueue.get(0);
                    if (accessId.equals(getId())) {
                        mListener.onRequestAccepted();
                        return;
                    }
                    for (Host accessHost : getHosts()) {
                        if (accessHost.getName().equals(accessId)) {
                            sendMessage(CentralizedMessage.messageReplyGiveAccess(getId()), accessHost);
                            break;
                        }
                    }
                }
                break;


            case CentralizedMessage.MESSAGE_REPLY_GIVE_ACCESS_PREFIX:
                mListener.onRequestAccepted();
                break;

            default:
                break;

        }
    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {
        setHosts(hosts);
        mListener.onPeerFind(hosts.size());
    }

    @Override
    public void requestAccess() {
        if (!mIsConnectedToCoordinator) {
            findCoordinator();
            mIsPending = true;
        } else {
            if (mCoordinatorId.equals(getId())) {
                mRequestQueue.add(getId());
                mListener.onQueueAdded(mRequestQueue);
                if (mRequestQueue.get(0).equals(getId())) {
                    mListener.onRequestAccepted();
                }
            } else sendMessage(CentralizedMessage.messageRequestEnqueue(getId()), mCoordinator);
        }
    }

    @Override
    public void cancelRequest() {
        if (mCoordinatorId.equals(getId())) {
            int index = mRequestQueue.indexOf(getId());
            mRequestQueue.remove(index);
            mListener.onQueueAdded(mRequestQueue);
            if (index == 0 && mRequestQueue.size() != 0) {
                String accessId = mRequestQueue.get(0);
                for (Host accessHost : getHosts()) {
                    if (accessHost.getName().equals(accessId)) {
                        sendMessage(CentralizedMessage.messageReplyGiveAccess(getId()), accessHost);
                        break;
                    }
                }
            }
        } else sendMessage(CentralizedMessage.messageRequestDequeue(getId()), mCoordinator);
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
        mRequestQueue = new ArrayList<>();
        List<Host> hosts = new ArrayList<>(getHosts());
        for (Host host : hosts)
            sendMessage(CentralizedMessage.messageReplyCoordinatorFound(mCoordinatorId), host);
        if (mIsPending) {
            mIsPending = false;
            requestAccess();
        }
        mListener.onSetAsCoordinator(getId());
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
        if (mIsConnectedToCoordinator) return;
        for (Host host : new ArrayList<>(getHosts())) {
            if (host.getName().equals(mCoordinatorId)) {
                mCoordinator = host;
                mListener.onSetAsCoordinator(mCoordinatorId);
                mIsConnectedToCoordinator = true;
                if (mIsPending) {
                    mIsPending = false;
                    requestAccess();
                }
                return;
            }
        }
        connectToCoordinator();
    }
}