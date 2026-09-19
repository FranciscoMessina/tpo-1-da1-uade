package com.da_grupo9.ronda.data.model;

public class CreateReviewRequest {
    private final int rating;
    private final String comment;

    public CreateReviewRequest(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
