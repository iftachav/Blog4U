package com.example.blog4u.etc;

public class Comment {

    private String message, userId, timestamp, commentId;

    public Comment(){}

    public Comment(String message, String userId, String timestamp, String commentId) {
        this.message = message;
        this.userId = userId;
        this.timestamp = timestamp;
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
