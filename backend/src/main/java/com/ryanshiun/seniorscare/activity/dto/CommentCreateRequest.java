package com.ryanshiun.seniorscare.activity.dto;
public class CommentCreateRequest {
    private Long memberId;
    private String comment;
    private Integer rating; // 1~5 可為 null
    public Long getMemberId(){ return memberId; }
    public void setMemberId(Long memberId){ this.memberId = memberId; }
    public String getComment(){ return comment; }
    public void setComment(String comment){ this.comment = comment; }
    public Integer getRating(){ return rating; }
    public void setRating(Integer rating){ this.rating = rating; }
}