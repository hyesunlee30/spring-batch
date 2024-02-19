package com.bdlee.spring.batch.entity;

import com.querydsl.core.annotations.QueryProjection;

public class MemberProjection {
    private String email;
    private Long ranking;

    private Long postCount;

    private Long commentCount;

    public MemberProjection(){};

    @QueryProjection
    public MemberProjection(String email, Long ranking, Long postCount, Long commentCount){
        this.email = email;
        this.ranking = ranking;
        this.postCount = postCount;
        this.commentCount = commentCount;
    }
}

