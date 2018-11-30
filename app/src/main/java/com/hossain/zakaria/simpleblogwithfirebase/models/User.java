package com.hossain.zakaria.simpleblogwithfirebase.models;

public class User {

    private String userName, userImageUrl;

    public User() {
        //empty constructor needed for retrieving data from fire-base
    }

    public User(String userName, String userImageUrl) {
        this.userName = userName;
        this.userImageUrl = userImageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }
}
