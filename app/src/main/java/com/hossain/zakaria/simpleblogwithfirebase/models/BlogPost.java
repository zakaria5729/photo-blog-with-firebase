package com.hossain.zakaria.simpleblogwithfirebase.models;

public class BlogPost extends BlogPostId {

    private String userId, postImageUrl, postDescription, postThumbnailUrl, postDateAndTime;

    public BlogPost() {
        //empty constructor needed for retrieving data from fire-base
    }

    public BlogPost(String userId, String postImageUrl, String postDescription, String postThumbnailUrl, String postDateAndTime) {
        this.userId = userId;
        this.postImageUrl = postImageUrl;
        this.postDescription = postDescription;
        this.postThumbnailUrl = postThumbnailUrl;
        this.postDateAndTime = postDateAndTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public String getPostThumbnailUrl() {
        return postThumbnailUrl;
    }

    public String getPostDateAndTime() {
        return postDateAndTime;
    }
}
