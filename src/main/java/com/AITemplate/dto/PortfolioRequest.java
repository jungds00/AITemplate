package com.AITemplate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortfolioRequest {

    @Schema(description = "포트폴리오 제목", example = "백엔드 개발자 포트폴리오")
    private String title;

    @Schema(description = "경력 내용", example = "3년간 Java/Spring 기반 웹 서비스 개발")
    private String experience;

    @Schema(description = "보유 기술 스택", example = "Java, Spring Boot, MySQL, Redis")
    private String skills;
}
