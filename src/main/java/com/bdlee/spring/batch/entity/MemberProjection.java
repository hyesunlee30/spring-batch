package com.bdlee.spring.batch.entity;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberProjection {
    private Long id;

    private Long postCount;

    private Long commentCount;

    public MemberProjection(){};

    @QueryProjection
    public MemberProjection(Long id, Long postCount, Long commentCount){
        this.id = id;
        this.postCount = postCount;
        this.commentCount = commentCount;
    }
}

