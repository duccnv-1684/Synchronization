package com.ducnguyen2102.videosynchronization.synchronization.centralization;

final class CentralizationMessage {
    private CentralizationMessage() {
    }

    static final String MESSAGE_REQUEST_COORDINATOR_PREFIX = "1000:";
    static final String MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX = "1001:";
    static final String MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX = "1002:";
    static final String MESSAGE_REQUEST_ENQUEUE_PREFIX = "1003:";
    static final String MESSAGE_REPLY_ENQUEUE_PREFIX = "1004:";
    static final String MESSAGE_REQUEST_DEQUEUE_PREFIX = "1005:";
    static final String MESSAGE_REPLY_DEQUEUE_PREFIX = "1006:";
    static final String MESSAGE_REPLY_GIVE_ACCESS_PREFIX = "1007:";
    static final String MESSAGE_REQUEST_ELECTION_PREFIX = "1008:";
    static final String MESSAGE_REPLY_ELECTION_ACCEPT_PREFIX = "1009:";
    static final String MESSAGE_REPLY_ELECTION_DENY_PREFIX = "1010:";

    static String messageRequestCoordinator(String senderId) {
        return makeMessage(MESSAGE_REQUEST_COORDINATOR_PREFIX, senderId);
    }

    static String messageReplyCoordinatorNotFound(String senderId) {
        return makeMessage(MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX, senderId);
    }

    static String messageReplyCoordinatorFound(String coordinatorId) {
        return makeMessage(MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX, coordinatorId);
    }

    static String messageRequestEnqueue(String senderId) {
        return makeMessage(MESSAGE_REQUEST_ENQUEUE_PREFIX, senderId);
    }

    static String messageReplyEnqueue(String senderId) {
        return makeMessage(MESSAGE_REPLY_ENQUEUE_PREFIX, senderId);
    }

    static String messageRequestDequeue(String senderId) {
        return makeMessage(MESSAGE_REQUEST_DEQUEUE_PREFIX, senderId);
    }

    static String messageReplyDequeue(String senderId) {
        return makeMessage(MESSAGE_REPLY_DEQUEUE_PREFIX, senderId);
    }

    static String messageReplyGiveAccess(String senderId) {
        return makeMessage(MESSAGE_REPLY_GIVE_ACCESS_PREFIX, senderId);
    }

    static String messageRequestElection(String senderID){
        return makeMessage(MESSAGE_REQUEST_ELECTION_PREFIX,senderID);

    }
    static String messageReplyElectionAccept(String senderID){
        return makeMessage(MESSAGE_REPLY_ELECTION_ACCEPT_PREFIX,senderID);
    }
    static String messageReplyElectionDenyElection(String senderID){
        return makeMessage(MESSAGE_REPLY_ELECTION_DENY_PREFIX,senderID);
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
