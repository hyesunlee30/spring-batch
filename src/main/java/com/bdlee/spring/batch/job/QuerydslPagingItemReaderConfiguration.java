package com.bdlee.spring.batch.job;



import com.bdlee.spring.batch.entity.thecatch.Coupon;
import com.bdlee.spring.batch.entity.thecatch.CouponRepository;
import com.bdlee.spring.batch.entity.thecatch.QCoupon;
import com.bdlee.spring.batch.querydsl.QuerydslPagingItemReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;


import org.springframework.batch.item.ItemWriter;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

import java.time.LocalDateTime;



@Slf4j
@RequiredArgsConstructor
@Configuration
public class QuerydslPagingItemReaderConfiguration {
    public static final String JOB_NAME = "querydslPagingReaderJob";
    public static final Integer CHUNK_SIZE = 1;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    LocalDateTime now = LocalDateTime.now();
    QCoupon coupon = QCoupon.coupon;

    private final CouponRepository couponRepository;


    @Bean
    public Job querydslPagingReaderJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("querydslPagingReaderStep")
                .<Coupon, Coupon>chunk(CHUNK_SIZE)
                .reader(reader())
                .processor(expirationCouponProcessor())
                .writer(expirationCouponWriter())
                .build();
    }

    @Bean
    public QuerydslPagingItemReader<Coupon> reader() {
        return new QuerydslPagingItemReader<>(emf, CHUNK_SIZE, queryFactory -> queryFactory
                .selectFrom(coupon)
                .where(coupon.endDate.before(now)));
    }

    private ItemProcessor<Coupon, Coupon> expirationCouponProcessor() {
        return new ItemProcessor<Coupon, Coupon>() {
            @Override
            public Coupon process(Coupon coupon) throws Exception {
                coupon.expirationCoupon();
                return coupon;
            }
        };
    }

    public ItemWriter<Coupon> expirationCouponWriter() {
        return (couponRepository::saveAll);
    }
}
