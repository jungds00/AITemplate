package com.AITemplate.repository;

import com.AITemplate.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // ✅ 사용자 ID로 전체 포트폴리오 조회
    List<Portfolio> findAllByUserId(String userId);

    // ✅ 사용자 ID + 포트폴리오 ID로 단건 조회
    Optional<Portfolio> findByIdAndUserId(Long id, String userId);
}
