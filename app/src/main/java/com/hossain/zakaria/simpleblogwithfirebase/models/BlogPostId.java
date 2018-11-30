package com.hossain.zakaria.simpleblogwithfirebase.models;

import com.google.firebase.firestore.Exclude;

public class BlogPostId {

    @Exclude
    public String blogPostId;

    @Exclude
    public <T extends BlogPostId> T withId(String id) {
        this.blogPostId = id;
        return (T) this;
    }
}
