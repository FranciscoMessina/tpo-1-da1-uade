package com.da_grupo9.ronda.data.model;

public class ReviewItem {
    private String id, comment, createdAt, reviewerName, operationType;
    private int rating;

    public String getId() { return id; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
    public String getReviewerName() { return reviewerName; }
}
