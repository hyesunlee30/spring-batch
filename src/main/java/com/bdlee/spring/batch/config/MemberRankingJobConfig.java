package com.bdlee.spring.batch.config;

import com.bdlee.spring.batch.dto.Ranking;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MemberRankingJobConfig {
    @Bean
    public Job memberRankingJobConfig (JobBuilderFactory jobBuilderFactory, Step rankingJobStep) { //(1)
        return jobBuilderFactory.get("memberRankingJob")
                .preventRestart() //(2)
                .start(rankingJobStep) //(3)
                .build();
    }

}
