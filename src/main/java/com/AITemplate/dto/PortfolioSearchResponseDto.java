package com.AITemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSearchResponseDto {
    private long totalCount;
    private List<PortfolioSearchResultDto> results;
}
