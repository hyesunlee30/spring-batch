package com.bdlee.spring.batch.entity;

import lombok.*;
import javax.persistence.*;

import javax.persistence.Entity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contents;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.N;
}