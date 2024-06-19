package com.example.omrifit.classes;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.mlkit.nl.smartreply.TextMessage;

import java.util.Date;

/**
 * Represents a message in the chat, including metadata such as sender, timestamp, and read status.
 */
public class Message {
    public static final String SENT_BY_ME = "me";
    public static final String SENT_BY_OMRI = "Omri";

    private String message;
    private String sent_by;
    private boolean unread;
    private TextMessage textMessage;
    private String base64 = null;
    private long timestamp;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();

    /**
     * Default constructor for Firebase.
     */
    public Message() {
        this.unread = true;
    }

    /**
     * Constructor to initialize a Message object with specified message and sender.
     *
     * @param message The content of the message.
     * @param sent_by The sender of the message.
     */
    public Message(String message, String sent_by) {
        this.message = message;
        this.timestamp = new Date().getTime(); // Set current time as timestamp
        if (sent_by.equals(user.getUid()) || sent_by.equals(SENT_BY_ME)) {
            this.textMessage = TextMessage.createForLocalUser(message, timestamp);
        } else {
            this.textMessage = TextMessage.createForRemoteUser(message, timestamp, sent_by);
        }
        this.sent_by = sent_by;
        this.unread = true;
    }

    /**
     * Constructor to initialize a Message object with specified message, sender, read status, and timestamp.
     *
     * @param message   The content of the message.
     * @param sent_by   The sender of the message.
     * @param unread    The read status of the message.
     * @param timestamp The timestamp of when the message was sent.
     */
    public Message(String message, String sent_by, boolean unread, long timestamp) {
        this.message = message;
        this.sent_by = sent_by;
        this.unread = unread;
        this.timestamp = timestamp;
        if (sent_by.equals(user.getUid()) || sent_by.equals(SENT_BY_ME)) {
            this.textMessage = TextMessage.createForLocalUser(message, timestamp);
        } else {
            this.textMessage = TextMessage.createForRemoteUser(message, timestamp, sent_by);
        }
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public void read() {
        this.unread = false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSent_by() {
        return sent_by;
    }

    public void setSent_by(String sent_by) {
        this.sent_by = sent_by;
    }

    public TextMessage getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(TextMessage textMessage) {
        this.textMessage = textMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns a string representation of the Message object.
     *
     * @return A string representation of the Message object.
     */
    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", sent_by='" + sent_by + '\'' +
                ", unread=" + unread +
                ", timestamp=" + timestamp +
                ", base64='" + base64 + '\'' +
                '}';
    }
}
