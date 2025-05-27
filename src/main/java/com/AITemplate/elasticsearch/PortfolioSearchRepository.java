package com.AITemplate.elasticsearch;

import com.AITemplate.model.Portfolio;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PortfolioSearchRepository extends ElasticsearchRepository<Portfolio, Long>, PortfolioSearchQueryRepository {
}