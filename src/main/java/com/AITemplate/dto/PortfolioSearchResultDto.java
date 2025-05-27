package com.AITemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSearchResultDto {
    private Long id;
    private String userId;
    private String username;
    private String title;
    private String highlightedTitle;
    private String content;
    private String highlightedContent;
    private LocalDateTime createdAt;
}
