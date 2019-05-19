package com.ducnguyen.synchronization.synchronizationalgorithm.tokenring;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.synchronization.synchronizationalgorithm.SynchronizationAlgorithm;
import com.ducnguyen.synchronization.wifip2p.model.Host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class TokenRingAlgorithm extends SynchronizationAlgorithm {
    private Host mPreviousHost;
    private Host mNextHost;
    private boolean mIsFinding;
    private boolean mIsNeedGiveToken;
    private OnSynchronizationEventListener mListener;
    private boolean mIsTokenCreated;
    private boolean mIsKeepingToken;

    public TokenRingAlgorithm(Context context, Looper looper, String id, OnSynchronizationEventListener listener) {
        super(context, looper, id);
        mListener = listener;
    }

    @Override
    public void onReceive(byte[] bytes, Host sender) {
        String message = new String(bytes);
        String prefix = TokenRingMessage.getMessagePrefix(message);
        switch (prefix) {
            case TokenRingMessage.MESSAGE_REQUEST_BECOME_NEXT_HOST_PREFIX:
                if (mPreviousHost != null)
                    sendMessage(TokenRingMessage.messageRequestUpdateNextHost(getId()), mPreviousHost);
                mPreviousHost = sender;
                sendMessage(TokenRingMessage.messageReplyBecomeNextHost(getId()), sender);
                if (mNextHost == null && !mIsFinding) findNextHost();
                else if (!mIsTokenCreated) {
                    mIsTokenCreated = true;
                    sendMessage(TokenRingMessage.messageGiveToken(getId()), mNextHost);
                }
                break;
            case TokenRingMessage.MESSAGE_REPLY_BECOME_NEXT_HOST_PREFIX:
                mIsFinding = false;
                mNextHost = sender;
                break;
            case TokenRingMessage.MESSAGE_REQUEST_UPDATE_NEXT_HOST_PREFIX:
                mNextHost = null;
                sendMessage(TokenRingMessage.messageReplyUpdateNextHost(getId()), sender);
                findNextHost();
                break;
            case TokenRingMessage.MESSAGE_REPLY_UPDATE_NEXT_HOST_PREFIX:
                break;
            case TokenRingMessage.MESSAGE_GIVE_TOKEN_PREFIX:
                mIsTokenCreated = true;
                if (mIsNeedGiveToken) {
                    mListener.onRequestAccepted();
                    mIsKeepingToken = true;
                } else sendMessage(TokenRingMessage.messageGiveToken(getId()), mNextHost);
        }
    }

    @Override
    public void requestAccess() {
        if (mNextHost == null && !mIsFinding) findNextHost();
        mIsNeedGiveToken = true;
    }


    @Override
    public void cancelRequest() {
        mIsNeedGiveToken = false;
        if (mIsKeepingToken) {
            mIsKeepingToken = false;
            sendMessage(TokenRingMessage.messageGiveToken(getId()), mNextHost);
        }
    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {
        setHosts(hosts);
    }

    private void findNextHost() {
        if (getHosts().size() == 0) return;
        mIsFinding = true;
        List<Host> hosts = new ArrayList<>(getHosts());
        List<String> hostIds = new ArrayList<>();
        for (Host host : hosts) hostIds.add(host.getName());
        hostIds.add(getId());
        Collections.sort(hostIds);
        int hostIndex = hostIds.indexOf(getId());
        int nextHostIndex = (++hostIndex == hostIds.size()) ? 0 : hostIndex;
        String nextHostId = hostIds.get(nextHostIndex);
        Host nextHost = null;
        for (Host host : hosts) {
            if (host.getName().equals(nextHostId)) {
                nextHost = host;
                break;
            }
        }
        sendMessage(TokenRingMessage.messageRequestBecomeNextHost(getId()), nextHost);
    }
}
