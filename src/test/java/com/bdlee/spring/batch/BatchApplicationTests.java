package com.bdlee.spring.batch;

import com.bdlee.spring.batch.entity.*;
import com.bdlee.spring.batch.job.QuerydslPagingItemReaderConfiguration;
import com.bdlee.spring.batch.querydsl.QuerydslPagingItemReader;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestBatchConfig.class, QuerydslPagingItemReaderConfiguration.class})
public class BatchApplicationTests {




    @Autowired MemberRepository repository;
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private EntityManagerFactory emf;
    private QMember member = QMember.member;
    private QPost post = QPost.post;
    private QComment comment = QComment.comment;

    @Test
    public void READER_TEST() throws Exception {

        System.out.println(">>>>>> READER_TEST start");
        QuerydslPagingItemReader<MemberProjection> reader =
                new QuerydslPagingItemReader<>(emf, 1, queryFactory -> queryFactory
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


        reader.open(new ExecutionContext());

        //when
        MemberProjection read1 = reader.read();


        //then
        System.out.println("TEST END >>>>"+read1.getId());

    }


    @Test
    public void EXECUTION_JOB_TEST() throws Exception {

        System.out.println(">>>>>> EXECUTION_JOB_TEST start");
        JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);


        List<Member> members = repository.findAll();
        assertThat(members.get(0).getPoint()).isNotZero();

    }

}
