package com.ducnguyen2102.videosynchronization.synchronization.distributed;

public final class DistributedMessage {
    private DistributedMessage() {
    }

    public static final String MESSAGE_REQUEST_ACCESS = "2000:";
    public static final String MESSAGE_REPLY_OK = "2001:";

    public static String messageRequestAccess(String timeStamp) {
        return makeMessage(MESSAGE_REQUEST_ACCESS, timeStamp);
    }

    public static String messageReplyOk(String senderId) {
        return makeMessage(MESSAGE_REPLY_OK, senderId);
    }


    private static String makeMessage(String prefix, String content) {
        return prefix + content;
    }

    public static String getMessagePrefix(String message) {
        return message.substring(0, 5);
    }

    public static String getMessageContent(String message) {
        return message.substring(5);
    }
}
