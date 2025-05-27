package com.AITemplate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortfolioUpdateRequest {

    @Schema(description = "수정할 포트폴리오 제목", example = "새로운 제목")
    private String title;

    @Schema(description = "수정할 포트폴리오 내용", example = "새로운 포트폴리오 내용입니다.")
    private String content;
}
