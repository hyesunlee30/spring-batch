package com.bdlee.spring.batch.job;

import com.bdlee.spring.batch.entity.*;
import com.bdlee.spring.batch.querydsl.QuerydslPagingItemReader;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManagerFactory;
import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.count;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class QuerydslPagingItemReaderJobConfiguration {

    public static final String JOB_NAME = "querydslPagingReaderJob";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;
    private final QuerydslPagingItemReaderJobParameter jobParameter;

    private QMember member = QMember.member;
    private QPost post = QPost.post;
    private QComment comment = QComment.comment;

    private int chunkSize;

    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean
    @JobScope
    public QuerydslPagingItemReaderJobParameter jobParameter() {
        return new QuerydslPagingItemReaderJobParameter();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("querydslPagingReaderStep")
                .<MemberProjection, Member>chunk(chunkSize)
                .reader(reader())
                //.processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public QuerydslPagingItemReader<MemberProjection> reader() {


        return new QuerydslPagingItemReader<>(emf, chunkSize, queryFactory -> queryFactory
                .select(Projections.bean(MemberProjection.class,
                        member.email, member.ranking,
                        JPAExpressions.select(count(post.id)).from(post),
                        JPAExpressions.select(count(comment.id)).from(comment)
                ))
                .from(member,post, comment)
                .where(post.createdAt.eq(jobParameter.getTxDate().atStartOfDay())
                .and(
                        member.email.eq(post.email).or(member.email.eq(comment.email))
                )
                .and(
                        member.active.eq(true)
                ))
        );
    }

    private ItemProcessor<MemberProjection, Member> processor() {

        return item -> {
            member.updateLanking(item.getPostCount() * 3L + item.getCommentCount() * 10L);
        }
        return null;
    }

    @Bean
    public JpaItemWriter<Member> writer() {
        return new JpaItemWriterBuilder<Member>()
                .entityManagerFactory(emf)
                .build();
    }


}
