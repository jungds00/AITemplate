package com.AITemplate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Document(indexName = "portfolio") // Elasticsearch 인덱스명
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Portfolio {

    @jakarta.persistence.Id // JPA용
    @Id                    // Elasticsearch용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;      // 사용자 ID (영문 로그인용)
    private String username;    // 사용자 이름 (한글 이름)
    private String title;       // 포트폴리오 제목

    @Column(columnDefinition = "TEXT")
    private String content;     // 포트폴리오 내용

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    private LocalDateTime createdAt; // 생성일자
}
