package com.aryanapps.ramailoghar.model;

public class Post {
    private String postId;
    private String description;
    private int publisher;
    private String postImage;
    private String publisherName;

    public Post(String postId, String description, int publisher, String postImage, String publisherName) {
        this.postId = postId;
        this.description = description;
        this.publisher = publisher;
        this.postImage = postImage;
        this.publisherName = publisherName;
    }


    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public Post() {
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPublisher() {
        return publisher;
    }

    public void setPublisher(int publisher) {
        this.publisher = publisher;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }
}
