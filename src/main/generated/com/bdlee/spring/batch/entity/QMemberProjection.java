package com.bdlee.spring.batch.entity;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.bdlee.spring.batch.entity.QMemberProjection is a Querydsl Projection type for MemberProjection
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberProjection extends ConstructorExpression<MemberProjection> {

    private static final long serialVersionUID = 183291888L;

    public QMemberProjection(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<Long> postCount, com.querydsl.core.types.Expression<Long> commentCount) {
        super(MemberProjection.class, new Class<?>[]{long.class, long.class, long.class}, id, postCount, commentCount);
    }

}

