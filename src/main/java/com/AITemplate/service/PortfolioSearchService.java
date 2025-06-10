package com.AITemplate.service;

import com.AITemplate.dto.PortfolioSearchResultDto;
import com.AITemplate.elasticsearch.PortfolioSearchQueryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PortfolioSearchService {

    private final PortfolioSearchQueryRepository searchRepository;

    public PortfolioSearchService(
            @Qualifier("portfolioSearchQueryRepositoryImpl") // Qualifier 쓸때는 @RequiredArgsConstructor대신 생성자주입 필요
            PortfolioSearchQueryRepository searchRepository
    ) {
        this.searchRepository = searchRepository;
    }

    @SuppressWarnings("unchecked")
    public List<PortfolioSearchResultDto> searchByKeyword(String keyword, int from, int size, String sort) {
        Map<String, Object> resultMap = searchRepository.searchPortfolios(keyword, from, size, sort);
        return (List<PortfolioSearchResultDto>) resultMap.get("results");
    }
}

