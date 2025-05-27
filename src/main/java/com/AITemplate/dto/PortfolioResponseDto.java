package com.AITemplate.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioResponseDto {
    private Long id;
    private String title;
    private String username;
    private String content;
    private String imageUrl;
}
