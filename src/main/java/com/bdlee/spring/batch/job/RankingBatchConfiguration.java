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
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.EntityManagerFactory;
import org.springframework.batch.core.launch.JobLauncher;

import java.time.LocalDateTime;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class RankingBatchConfiguration {

    public static final String JOB_NAME = "rankingJob";
    private final JobLauncher jobLauncher;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private QMember member = QMember.member;
    private QPost post = QPost.post;
    private QComment comment = QComment.comment;


    //*/1 * * * *
    //0 0 4 * * ?
    @Bean
    @Scheduled(cron = "*/1 * * * * ?")
    public void scheduleRankingJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
        jobLauncher.run(rankingJob(), jobParameters);
    }

    @Bean
    public Job rankingJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .build();
    }

    @Bean
    public Step step() {

        log.info(">>>>> Step");
        return stepBuilderFactory.get("querydslPagingReaderStep")
                .<MemberProjection, Member>chunk(1000)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public QuerydslPagingItemReader<MemberProjection> reader() {
        log.info("reader start");
        return new QuerydslPagingItemReader<>(entityManagerFactory, 1000, queryFactory -> queryFactory
                .select(Projections.bean(MemberProjection.class,
                        member.id,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(post.count())
                                        .from(post)
                                        .where(post.email.eq(member.email)),
                                "postCount"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(comment.count())
                                        .from(comment)
                                        .where(comment.email.eq(member.email)),
                                "commentCount"
                        )
                ))
                .from(member, post, comment)
                .where(post.createdAt.between(LocalDateTime.now().minusMonths(1),LocalDateTime.now())
                        .and(
                                member.email.eq(post.email).or(member.email.eq(comment.email))
                        )
                        .and(
                                member.active.eq(true)
                        ))
        );
    }

    @Bean
    public ItemProcessor<MemberProjection, Member> processor() {
        return item -> {
            // 기존 Member 엔티티를 조회
            Member existingMember = entityManagerFactory.createEntityManager().find(Member.class, item.getId());

            // 비즈니스 로직에 따라 point 계산
            long point = item.getPostCount() * 10L + item.getCommentCount() * 5L;

            log.info("point {}",point);
            // 비즈니스 로직에 따라 ranking 업데이트
            existingMember.updatePoin(point);

            return existingMember;
        };
    }

    @Bean
    public JpaItemWriter<Member> writer() {
        return new JpaItemWriterBuilder<Member>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}