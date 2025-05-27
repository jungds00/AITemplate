package com.AITemplate.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.AITemplate.dto.PortfolioSearchResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Repository("portfolioSearchQueryRepositoryImpl")
@RequiredArgsConstructor
public class PortfolioSearchQueryRepositoryImpl implements PortfolioSearchQueryRepository {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public Map<String, Object> searchPortfolios(String keyword, int from, int size, String sort) {
        try {
            String[] sortParts = sort.split(",");
            String sortField = sortParts.length > 0 ? sortParts[0] : "createdAt";
            String sortDirection = sortParts.length > 1 ? sortParts[1].toLowerCase() : "desc";
            SortOrder order = sortDirection.equals("asc") ? SortOrder.Asc : SortOrder.Desc;

            Query query = Query.of(q -> q
                    .multiMatch(m -> m
                            .fields("title", "content")
                            .query(keyword)
                    )
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("portfolio")
                    .query(query)
                    .from(from)
                    .size(size)
                    .sort(sortBuilder -> sortBuilder
                            .field(f -> f
                                    .field(sortField)
                                    .order(order)
                            )
                    )
                    .highlight(h -> h
                            .fields("title", hf -> hf)
                            .fields("content", hf -> hf)
                    )
            );

            SearchResponse<Map<String, Object>> response = elasticsearchClient.search(
                    searchRequest,
                    (Class<Map<String, Object>>)(Class<?>) Map.class
            );

            List<PortfolioSearchResultDto> results = response.hits().hits().stream()
                    .map(hit -> {
                        Map<String, Object> source = hit.source();
                        if (source == null) return null;

                        String id = hit.id();
                        String userId = (String) source.getOrDefault("userId", "");
                        String username = (String) source.getOrDefault("username", "");
                        String title = (String) source.getOrDefault("title", "");
                        String content = (String) source.getOrDefault("content", "");

                        String highlightedTitle = hit.highlight().getOrDefault("title", List.of()).stream().findFirst().orElse(title);
                        String highlightedContent = hit.highlight().getOrDefault("content", List.of()).stream().findFirst().orElse(content);

                        LocalDateTime createdAt = null;
                        Object createdAtObj = source.get("createdAt");
                        if (createdAtObj instanceof Number timestamp) {
                            Instant instant = Instant.ofEpochMilli(timestamp.longValue());
                            createdAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                        }

                        return PortfolioSearchResultDto.builder()
                                .id(Long.valueOf(id))
                                .userId(userId)
                                .username(username)
                                .title(title)
                                .highlightedTitle(highlightedTitle)
                                .content(content)
                                .highlightedContent(highlightedContent)
                                .createdAt(createdAt)
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 리턴 Map 구성
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("totalCount", response.hits().total().value());
            resultMap.put("results", results);

            return resultMap;

        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 검색 중 오류 발생", e);
        }
    }


}
