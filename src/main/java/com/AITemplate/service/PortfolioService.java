package com.AITemplate.service;

import com.AITemplate.model.Portfolio;
import com.AITemplate.repository.PortfolioRepository;
import com.AITemplate.elasticsearch.PortfolioSearchRepository;
import com.AITemplate.elasticsearch.PortfolioSearchQueryRepository;
import com.AITemplate.util.PdfGenerator;
import com.AITemplate.util.S3Uploader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioSearchRepository portfolioSearchRepository;
    private final S3Uploader s3Uploader;
    private final PortfolioSearchQueryRepository portfolioSearchQueryRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String POPULAR_KEY = "popular_portfolios";

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            PortfolioSearchRepository portfolioSearchRepository,
            S3Uploader s3Uploader,
            @Qualifier("portfolioSearchQueryRepositoryImpl") PortfolioSearchQueryRepository portfolioSearchQueryRepository,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioSearchRepository = portfolioSearchRepository;
        this.s3Uploader = s3Uploader;
        this.portfolioSearchQueryRepository = portfolioSearchQueryRepository;
        this.redisTemplate = redisTemplate;
    }


    public Portfolio savePortfolioWithImage(String userId, String username, String title, String content, String imageUrl) {
        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .username(username)
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .createdAt(LocalDateTime.now())
                .build();

        Portfolio saved = portfolioRepository.save(portfolio);
        portfolioSearchRepository.save(saved);
        return saved;
    }

    public String uploadImageAndReturnUrl(MultipartFile image) {
        try {
            return s3Uploader.upload(image.getOriginalFilename(), image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    @Cacheable(value = "userPortfolios", key = "#userId")
    public List<Portfolio> getPortfoliosByUserId(String userId) {
        return portfolioRepository.findAllByUserId(userId);
    }

    public Optional<Portfolio> getPortfolioByIdAndUserId(Long id, String userId) {
        Optional<Portfolio> result = portfolioRepository.findByIdAndUserId(id, userId);
        result.ifPresent(portfolio -> incrementViewCount(portfolio.getId()));
        return result;
    }

    public boolean updatePortfolio(Long portfolioId, String userId, String newTitle, String newContent) {
        Optional<Portfolio> optionalPortfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId);
        if (optionalPortfolio.isPresent()) {
            Portfolio portfolio = optionalPortfolio.get();
            portfolio.setTitle(newTitle);
            portfolio.setContent(newContent);
            portfolioRepository.save(portfolio);
            portfolioSearchRepository.save(portfolio);
            return true;
        }
        return false;
    }

    public boolean deletePortfolio(Long portfolioId, String userId) {
        Optional<Portfolio> optional = portfolioRepository.findByIdAndUserId(portfolioId, userId);
        if (optional.isPresent()) {
            Portfolio portfolio = optional.get();
            portfolioRepository.delete(portfolio);
            portfolioSearchRepository.deleteById(portfolioId);
            redisTemplate.opsForZSet().remove(POPULAR_KEY, String.valueOf(portfolioId));
            return true;
        }
        return false;
    }

    public Map<String, Object> searchPortfolios(String keyword, int from, int size, String sort) {
        return portfolioSearchQueryRepository.searchPortfolios(keyword, from, size, sort);
    }

    public List<Map<String,Object>> getTopPopularPortfolios(int topN) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<String> topIds = zSetOps.reverseRange(POPULAR_KEY, 0, topN - 1);

        if (topIds == null || topIds.isEmpty()) return Collections.emptyList();

        List<Long> ids = topIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Portfolio> portfolios = portfolioRepository.findAllById(ids);

        Map<Long, Portfolio> portfolioMap = portfolios.stream()
                .collect(Collectors.toMap(Portfolio::getId, p -> p));

        return ids.stream()
                .map(id -> {
                    Portfolio p = portfolioMap.get(id);
                    if (p == null) return null;

                    Double score = zSetOps.score(POPULAR_KEY, String.valueOf(id));
                    long viewCount = score != null ? score.longValue() : 0;

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("userId", p.getUserId());
                    map.put("username", p.getUsername());
                    map.put("title", p.getTitle());
                    map.put("content", p.getContent());
                    map.put("createdAt", p.getCreatedAt());
                    map.put("viewCount", viewCount);
                    return map;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void incrementViewCount(Long portfolioId) {
        redisTemplate.opsForZSet().incrementScore(POPULAR_KEY, String.valueOf(portfolioId), 1);
    }

    public String uploadPortfolioPdf(Portfolio portfolio) {
        try (ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {
            PdfGenerator.generatePortfolioPdf(portfolio, pdfOutputStream);
            byte[] pdfBytes = pdfOutputStream.toByteArray();

            String fileName = "portfolio_" + portfolio.getId() + ".pdf";
            return s3Uploader.upload(fileName, pdfBytes);
        } catch (IOException e) {
            throw new RuntimeException("PDF 업로드 실패", e);
        }
    }
}
