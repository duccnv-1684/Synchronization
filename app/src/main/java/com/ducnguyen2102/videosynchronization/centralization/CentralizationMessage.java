package com.ducnguyen2102.videosynchronization.centralization;

public final class CentralizationMessage {
    private CentralizationMessage() {
    }

    public static final String MESSAGE_REQUEST_COORDINATOR_PREFIX = "1000:";
    public static final String MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX = "1001:";
    public static final String MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX = "1002:";
    public static final String MESSAGE_REQUEST_ENQUEUE_PREFIX = "1003:";
    public static final String MESSAGE_REPLY_ENQUEUE_PREFIX = "1004:";
    public static final String MESSAGE_REQUEST_DEQUEUE_PREFIX = "1005:";
    public static final String MESSAGE_REPLY_DEQUEUE_PREFIX = "1006:";
    public static final String MESSAGE_REPLY_GIVE_ACCESS = "1007";

    public static String messageRequestCoordinator(String senderId) {
        return makeMessage(MESSAGE_REQUEST_COORDINATOR_PREFIX, senderId);
    }

    public static String messageReplyCoordinatorNotFound(String senderId) {
        return makeMessage(MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX, senderId);
    }

    public static String messageReplyCoordinatorFound(String coordinatorId) {
        return makeMessage(MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX, coordinatorId);
    }

    public static String messageRequestEnqueue(String senderId) {
        return makeMessage(MESSAGE_REQUEST_ENQUEUE_PREFIX, senderId);
    }

    public static String messageReplyEnqueue(String senderId) {
        return makeMessage(MESSAGE_REPLY_ENQUEUE_PREFIX, senderId);
    }

    public static String messageRequestDequeue(String senderId) {
        return makeMessage(MESSAGE_REQUEST_DEQUEUE_PREFIX, senderId);
    }

    public static String messageReplyDequeue(String senderId) {
        return makeMessage(MESSAGE_REPLY_DEQUEUE_PREFIX, senderId);
    }

    public static String messageReplyGiveAccess(String senderId) {
        return makeMessage(MESSAGE_REPLY_GIVE_ACCESS, senderId);
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
