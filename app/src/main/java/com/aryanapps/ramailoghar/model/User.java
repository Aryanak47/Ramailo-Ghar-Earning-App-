package com.aryanapps.ramailoghar.model;

public class User {
    private String userName;
    private String email;
    private String profile_pic;
    private int totalPostLike;
    private int totalPost;
    private int totalPostILiked;
    private int totalPostLikeTarget;
    private int totalPostILikedTarget;
    private int videoAdsTarget;

    public int getVideoAdsTarget() {
        return videoAdsTarget;
    }

    public void setVideoAdsTarget(int videoAdsTarget) {
        this.videoAdsTarget = videoAdsTarget;
    }

    public int getTotalPostILiked() {
        return totalPostILiked;
    }

    public void setTotalPostILiked(int totalPostILiked) {
        this.totalPostILiked = totalPostILiked;
    }

    public int getTotalPostLikeTarget() {
        return totalPostLikeTarget;
    }

    public void setTotalPostLikeTarget(int totalPostLikeTarget) {
        this.totalPostLikeTarget = totalPostLikeTarget;
    }

    public int getTotalPostILikedTarget() {
        return totalPostILikedTarget;
    }

    public void setTotalPostILikedTarget(int totalPostILikedTarget) {
        this.totalPostILikedTarget = totalPostILikedTarget;
    }

    public int getTotalPost() {
        return totalPost;
    }

    public void setTotalPost(int totalPost) {
        this.totalPost = totalPost;
    }

    public int getTotalPostLike() {
        return totalPostLike;
    }

    public void setTotalPostLike(int totalPostLike) {
        this.totalPostLike = totalPostLike;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
