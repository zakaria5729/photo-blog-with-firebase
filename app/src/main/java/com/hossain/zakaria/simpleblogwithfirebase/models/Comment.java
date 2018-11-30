package com.hossain.zakaria.simpleblogwithfirebase.models;

import java.util.Date;

public class Comment {
    private String commentUserId, commentText;
    private String commentTimeStamp;

    public Comment() {
        //empty constructor needed for retrieving data from fire-base
    }

    public Comment(String commentText, String commentUserId, String commentTimeStamp) {
        this.commentText = commentText;
        this.commentUserId = commentUserId;
        this.commentTimeStamp = commentTimeStamp;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getCommentUserId() {
        return commentUserId;
    }

    public String getCommentTimeStamp() {
        return commentTimeStamp;
    }
}


