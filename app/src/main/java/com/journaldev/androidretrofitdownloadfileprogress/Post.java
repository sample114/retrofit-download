package com.journaldev.androidretrofitdownloadfileprogress;

import com.google.gson.annotations.SerializedName;

public class Post {

    private int userID1;


    private String title;

    private int id;

    @SerializedName("body")
    private String text;

    public int getUserID() {
        return userID1;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}