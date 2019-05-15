package com.ducnguyen.synchronization.synchronizationalgorithm.distributed;

final class DistributedMessage {
    private DistributedMessage() {
    }

    static final String MESSAGE_REQUEST_ACCESS = "2000:";
    static final String MESSAGE_REPLY_OK = "2001:";

    static String messageRequestAccess(String timeStamp) {
        return makeMessage(MESSAGE_REQUEST_ACCESS, timeStamp);
    }

    static String messageReplyOk(String senderId) {
        return makeMessage(MESSAGE_REPLY_OK, senderId);
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
