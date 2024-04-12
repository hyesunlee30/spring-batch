package com.bdlee.spring.batch.job;


import com.bdlee.spring.batch.entity.som.CommentRepository;
import com.bdlee.spring.batch.entity.som.Member;
import com.bdlee.spring.batch.entity.som.MemberRepository;
import com.bdlee.spring.batch.entity.som.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class TaskletRankingConfiguration {



    private final JobLauncher jobLauncher;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


//    @Bean
//    @Scheduled(cron = "*/1 * * * * ?")
//    public void scheduleRankingJob() throws Exception {
//        JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
//        jobLauncher.run(monthRankingJob(), jobParameters);
//    }

    @Bean
    public Job monthRankingJob() throws Exception {

        return jobBuilderFactory.get("scheduleRankingJob")
                .start(simpleStep())
                .build();
    }

    @Bean
    public Step simpleStep() {
        return this.stepBuilderFactory
                .get("simple-step")
                .tasklet(simpleTasklet())
                .build();
    }

    @Bean
    public Tasklet simpleTasklet() {
        return (stepContribution, chunkContext) -> {


            List<Member> members = memberRepository.findAll();

            for (Member member : members) {

                Long postCount = postRepository.countByCreatedAtBetweenAndEmail(
                        LocalDateTime.now().minusMonths(1),
                        LocalDateTime.now(),
                        member.getEmail());

                Long commentCount = commentRepository.countByCreatedAtBetweenAndEmail(
                        LocalDateTime.now().minusMonths(1),
                        LocalDateTime.now(),
                        member.getEmail());

                member.updatePoint(postCount*3L + commentCount * 5L);
            }

            Collections.sort(members, Comparator.comparing(Member::getPoint).reversed());

            for (int i = 0; i < members.size(); i++) {
                members.get(i).updatePoint(i+1L);
            }

            memberRepository.saveAll(members);

            return RepeatStatus.FINISHED;
        };
    }
}
