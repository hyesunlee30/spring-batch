package com.bdlee.spring.batch.entity;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.count;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustom {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    private QMember member = QMember.member;
    private QPost post = QPost.post;
    private QComment comment = QComment.comment;



    private Long postCount;

    private Long commentCount;
    List<MemberProjection> result = queryFactory
            .select(
                    Projections.bean(MemberProjection.class,
                        member.email, member.ranking,
                        JPAExpressions.select(count(post.id)).from(post),
                        JPAExpressions.select(count(comment.id)).from(comment)
                    )
            )
            .from(member,post, comment)
            .where(member.email.eq(post.email).or(member.email.eq(comment.email)))
            .fetch();

}
