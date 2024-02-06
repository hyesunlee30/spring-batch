package com.bdlee.spring.batch.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ranking {

    private Long memberId;
    private Long postId;
    private Long commentId;


    @QueryProjection
    public Ranking(Long memberId, Long postId, Long commentId) {
        this.memberId = memberId;
        this.postId = postId;
        this.commentId = commentId;
    }
}
