package com.bdlee.spring.batch.support;

import com.bdlee.spring.batch.domain.Member;
import com.bdlee.spring.batch.domain.Post;
import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepositorySupport extends QuerydslRepositorySupport {
    private final JPAQueryFactory queryFactory;

    private QMember member;
    private QPost post;
    private QComment comment;
    public MemberRepositorySupport(JPAQueryFactory queryFactory) {
        super(Member.class);
        this.queryFactory = queryFactory;
        this.member = QMember.member;
        this.post = QPost.post;
        this.comment = QComment.comment;
    }




    public JPAQuery<Member> findAll() {


        return queryFactory
                .selectFrom(member);
    }
}
