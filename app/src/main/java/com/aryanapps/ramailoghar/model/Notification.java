package com.aryanapps.ramailoghar.model;

public class Notification {
    private String userId;
    private String postId;
    private boolean ispost;
    private boolean clicked;
    private String text;
    private String timeStamp;

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    private boolean isSeen;

    public Notification(){}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
