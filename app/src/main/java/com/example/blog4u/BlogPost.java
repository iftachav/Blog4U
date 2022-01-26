package com.example.blog4u;

public class BlogPost {
    private String userId, image_url, description, thumbnail, timestamp, likesCount, blogId;
    //TODO maybe need to put timestamp as Timestamp variable.


    public BlogPost(){}



    public BlogPost(String userId, String image_url, String description, String thumbnail, String timestamp, String likesCount, String blogId) {
        this.userId = userId;
        this.image_url = image_url;
        this.description = description;
        this.thumbnail = thumbnail;
        this.timestamp = timestamp;
        this.likesCount = likesCount;
        this.blogId = blogId;
    }

    public String getBlogId() {
        return blogId;
    }

    public void setBlogId(String blogId) {
        this.blogId = blogId;
    }

    public String getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(String likesCount) {
        this.likesCount = likesCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
