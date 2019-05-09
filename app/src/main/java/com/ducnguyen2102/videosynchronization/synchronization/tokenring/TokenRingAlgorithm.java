package com.ducnguyen2102.videosynchronization.synchronization.tokenring;

import android.content.Context;
import android.os.Looper;

import com.ducnguyen.wifip2p.model.Host;
import com.ducnguyen2102.videosynchronization.synchronization.SynchronizationAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class TokenRingAlgorithm extends SynchronizationAlgorithm {
    private Host mPreviousHost;
    private Host mNextHost;
    private boolean isRequesting;
    private boolean mIsFinding;
    private boolean isNeedGiveToken;

    public TokenRingAlgorithm(Context context, Looper looper, String id) {
        super(context, looper, id);
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
                break;
            case TokenRingMessage.MESSAGE_REPLY_BECOME_NEXT_HOST_PREFIX:
                mIsFinding = false;
                mNextHost = sender;
                if (isNeedGiveToken) {
                    sendMessage(TokenRingMessage.messageGiveToken(getId()), mNextHost);
                    isNeedGiveToken = false;
                }
                break;
            case TokenRingMessage.MESSAGE_REQUEST_UPDATE_NEXT_HOST_PREFIX:
                mNextHost = null;
                sendMessage(TokenRingMessage.messageReplyUpdateNextHost(getId()), sender);
                findNextHost();
                break;
            case TokenRingMessage.MESSAGE_REPLY_UPDATE_NEXT_HOST_PREFIX:
                break;
            case TokenRingMessage.MESSAGE_GIVE_TOKEN_PREFIX:
                startAccessing();

        }
    }

    private void startAccessing() {

    }

    private void stopAccessing() {
        if (mNextHost != null) {
            sendMessage(TokenRingMessage.messageGiveToken(getId()), mNextHost);
            isNeedGiveToken = false;
        } else isNeedGiveToken = true;
    }

    @Override
    public void onPeersUpdate(Set<Host> hosts) {
        setHosts(hosts);
        if (!isFounded() && !mIsFinding) findNextHost();
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
        Host nextHope = null;
        for (Host host : hosts) {
            if (host.getName().equals(nextHostId)) {
                nextHope = host;
                break;
            }
        }
        sendMessage(TokenRingMessage.messageRequestBecomeNextHost(getId()), nextHope);
    }

    private boolean isFounded() {
        return mNextHost != null;
    }
}