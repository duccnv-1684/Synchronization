package com.ducnguyen2102.videosynchronization.synchronization.tokenring;

final class TokenRingMessage {
    static final String MESSAGE_REQUEST_BECOME_NEXT_HOST_PREFIX = "4000:";
    static final String MESSAGE_REPLY_BECOME_NEXT_HOST_PREFIX = "4001:";
    static final String MESSAGE_REQUEST_UPDATE_NEXT_HOST_PREFIX = "4002:";
    static final String MESSAGE_REPLY_UPDATE_NEXT_HOST_PREFIX = "4003:";
    static final String MESSAGE_GIVE_TOKEN_PREFIX = "4004:";

    private TokenRingMessage() {
    }

    static String messageRequestBecomeNextHost(String senderId) {
        return makeMessage(MESSAGE_REQUEST_BECOME_NEXT_HOST_PREFIX, senderId);
    }

    static String messageReplyBecomeNextHost(String senderId) {
        return makeMessage(MESSAGE_REPLY_BECOME_NEXT_HOST_PREFIX, senderId);
    }

    static String messageRequestUpdateNextHost(String senderId) {
        return makeMessage(MESSAGE_REQUEST_UPDATE_NEXT_HOST_PREFIX, senderId);
    }

    static String messageReplyUpdateNextHost(String senderId) {
        return makeMessage(MESSAGE_REPLY_UPDATE_NEXT_HOST_PREFIX, senderId);
    }

    static String messageGiveToken(String senderId) {
        return makeMessage(MESSAGE_GIVE_TOKEN_PREFIX, senderId);
    }

    static String getMessagePrefix(String message) {
        return message.substring(0, 5);
    }

    static String getMessageContent(String message) {
        return message.substring(5);
    }

    private static String makeMessage(String prefix, String content) {
        return prefix + content;
    }
}
