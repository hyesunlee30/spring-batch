package com.bdlee.spring.batch.job;

import com.bdlee.spring.batch.entity.*;

import com.bdlee.spring.batch.querydsl.QuerydslPagingItemReader;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;


import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;


@Configuration
@Slf4j
@RequiredArgsConstructor
@ComponentScan(basePackages = "com.bdlee.spring.batch.job")
public class QuerydslPagingItemReaderConfiguration {

    public static final String JOB_NAME = "querydslPagingReaderJob";

    private final JobLauncher jobLauncher;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;
    private QMember member = QMember.member;
    private QPost post = QPost.post;
    private QComment comment = QComment.comment;



    private final int chunkSize = 10; //트랜잭션 범위
    //private final int chunkSize = 1000; //트랜잭션 범위


    //*/1 * * * *
    //0 0 4 * * ?
//    @Bean
//    @Scheduled(cron = "*/1 * * * * ?")
//    public void scheduleRankingJob() throws Exception {
//        JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
//        jobLauncher.run(rankingJob(), jobParameters);
//    }

    @Bean
    public Job rankingJob() throws Exception {

        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .build();
    }



    @Bean
    public Step step() throws Exception {

        return stepBuilderFactory.get("querydslPagingReaderStep")
                .<MemberProjection, Member>chunk(chunkSize) //Reader의 반환타입 & Writer의 파라미터타입
                .reader(reader())           // 인풋타입의 아이템을 하나씩 반환
                .processor(processor())     // 인풋타입을 받아서 아웃풋타입으로 리턴
                .writer(writer())
                .build();
    }


    @Bean()
    public QuerydslPagingItemReader<MemberProjection> reader() {
        log.info("reader start");
        return new QuerydslPagingItemReader<>(emf, chunkSize, queryFactory -> queryFactory
                .select(Projections.bean(MemberProjection.class,
                        member.id,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(post.count())
                                        .from(post)
                                        .where(post.email.eq(member.email)
                                                .and(post.createdAt.between(LocalDateTime.now().minusMonths(1),LocalDateTime.now()))),
                                "postCount"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(comment.count())
                                        .from(comment)
                                        .where(comment.email.eq(member.email).and(comment.createdAt.between(LocalDateTime.now().minusMonths(1),LocalDateTime.now()))),
                                "commentCount"
                        )
                ))
                .from(member));

    }


    @Bean
    public ItemProcessor<MemberProjection, Member> processor() {
        return item -> item.toMember(emf);
    }


    @Bean
    public JpaItemWriter<Member> writer() {
        return new JpaItemWriterBuilder<Member>()
                .entityManagerFactory(emf)
                .build();
    }


}
