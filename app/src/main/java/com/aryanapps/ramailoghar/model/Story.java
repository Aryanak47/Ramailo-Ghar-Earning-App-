package com.aryanapps.ramailoghar.model;

public class Story {
    private String storyID;
    private String userId;
    private long startTime;
    private long endTime;
    private String imageId;

    public Story(String storyID, String userId, long startTime, long endTime, String imageId) {
        this.storyID = storyID;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageId = imageId;
    }

    public String getStoryID() {
        return storyID;
    }

    public void setStoryID(String storyID) {
        this.storyID = storyID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
