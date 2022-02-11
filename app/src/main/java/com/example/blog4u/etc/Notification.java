package com.example.blog4u.etc;

public class Notification {

    private String userActedImage;
    private String description;
    private String timestamp;

    public Notification(){}

    public Notification(String image, String description, String timestamp) {
        this.userActedImage = image;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getUserActedImage() {
        return userActedImage;
    }

    public void setUserActedImage(String userActedImage) {
        this.userActedImage = userActedImage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
