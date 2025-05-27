package com.AITemplate.elasticsearch;


import java.util.Map;

public interface PortfolioSearchQueryRepository {
    Map<String, Object> searchPortfolios(String keyword, int from, int size, String sort);

}
