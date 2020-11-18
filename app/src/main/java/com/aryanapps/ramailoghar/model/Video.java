package com.aryanapps.ramailoghar.model;

public class Video {
    private String id;
    private String url;
    private String userid;
    private float amount ;
    private int maxView ;
    private int currentView ;



    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getMaxView() {
        return maxView;
    }

    public void setMaxView(int maxView) {
        this.maxView = maxView;
    }

    public int getCurrentView() {
        return currentView;
    }

    public void setCurrentView(int currentView) {
        this.currentView = currentView;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
