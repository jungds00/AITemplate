package com.AITemplate.controller;

import com.AITemplate.dto.PortfolioRequest;
import com.AITemplate.dto.PortfolioResponseDto;
import com.AITemplate.dto.PortfolioUpdateRequest;
import com.AITemplate.exception.ResourceNotFoundException;
import com.AITemplate.model.Portfolio;
import com.AITemplate.security.UserPrincipal;
import com.AITemplate.service.GoogleAiService;
import com.AITemplate.service.PortfolioService;
import com.AITemplate.util.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Tag(name = "포트폴리오 API", description = "AI 기반 포트폴리오 생성 및 관리 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
@SecurityRequirement(name = "JWT")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final GoogleAiService googleAiService;
    private final S3Uploader s3Uploader;

    @Operation(
            summary = "AI 포트폴리오 생성",
            description = "개별 입력 필드(title, experience, skills)와 이미지 파일을 업로드(선택)하면, AI가 포트폴리오를 생성하여 S3에 저장합니다."
    )
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PortfolioResponseDto> generatePortfolio(
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @ParameterObject @ModelAttribute PortfolioRequest request,

            @Parameter(description = "포트폴리오 이미지 파일 (선택)", required = false)
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        String userId = userPrincipal.getUsername();
        String username = userPrincipal.getDisplayName();

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                imageUrl = s3Uploader.upload(file.getOriginalFilename(), file.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(500).build();
            }
        }

        String content = googleAiService.generatePortfolioWithAi(
                username,
                request.getExperience(),
                request.getSkills()
        );

        Portfolio saved = portfolioService.savePortfolioWithImage(
                userId,
                username,
                request.getTitle(),
                content,
                imageUrl
        );

        return ResponseEntity.ok(
                PortfolioResponseDto.builder()
                        .id(saved.getId())
                        .title(saved.getTitle())
                        .username(saved.getUsername())
                        .content(saved.getContent())
                        .imageUrl(saved.getImageUrl())
                        .build()
        );
    }


    @Operation(summary = "내 포트폴리오 목록 조회", description = "현재 로그인한 사용자의 모든 포트폴리오를 반환합니다.")
    @GetMapping
    public ResponseEntity<List<Portfolio>> getAllMyPortfolios(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String userId = userPrincipal.getUsername();
        List<Portfolio> portfolios = portfolioService.getPortfoliosByUserId(userId);
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "포트폴리오 상세 조회", description = "포트폴리오 ID를 통해 상세 내용을 조회합니다.")
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> getPortfolioDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long portfolioId
    ) {
        String userId = userPrincipal.getUsername();
        Portfolio p = portfolioService.getPortfolioByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 포트폴리오를 찾을 수 없습니다."));

        return ResponseEntity.ok(PortfolioResponseDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .username(p.getUsername())
                .content(p.getContent())
                .imageUrl(p.getImageUrl())
                .build());
    }

    @Operation(summary = "포트폴리오 수정", description = "포트폴리오의 제목과 내용을 수정합니다.")
    @PutMapping(value = "/{portfolioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updatePortfolio(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long portfolioId,
            @ModelAttribute PortfolioUpdateRequest request
    ) {
        String userId = userPrincipal.getUsername();
        boolean updated = portfolioService.updatePortfolio(
                portfolioId, userId, request.getTitle(), request.getContent());

        if (!updated) {
            throw new ResourceNotFoundException("수정할 포트폴리오가 존재하지 않거나 권한이 없습니다.");
        }

        return ResponseEntity.ok(Map.of("message", "포트폴리오가 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "포트폴리오 삭제", description = "지정한 포트폴리오를 삭제합니다.")
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Map<String, String>> deletePortfolio(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long portfolioId
    ) {
        String userId = userPrincipal.getUsername();
        boolean deleted = portfolioService.deletePortfolio(portfolioId, userId);
        if (!deleted) {
            throw new ResourceNotFoundException("삭제할 포트폴리오가 존재하지 않거나 권한이 없습니다.");
        }

        return ResponseEntity.ok(Map.of("message", "포트폴리오가 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "PDF 다운로드", description = "포트폴리오를 PDF로 생성한 후 S3에 업로드하고 다운로드 URL을 반환합니다.")
    @GetMapping("/{portfolioId}/download")
    public ResponseEntity<Map<String, String>> downloadPortfolioAsPdf(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long portfolioId
    ) throws Exception {
        String userId = userPrincipal.getUsername();
        Portfolio portfolio = portfolioService.getPortfolioByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("다운로드할 포트폴리오를 찾을 수 없습니다."));

        String downloadUrl = portfolioService.uploadPortfolioPdf(portfolio);
        return ResponseEntity.ok(Map.of("url", downloadUrl));
    }

    @Operation(summary = "포트폴리오 검색", description = "키워드를 기반으로 포트폴리오를 검색하고 하이라이팅된 결과를 반환합니다.")
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPortfolios(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Map<String, Object> result = portfolioService.searchPortfolios(keyword, from, size, sort);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "인기 포트폴리오 조회", description = "Redis 기반 캐시를 활용하여 인기 포트폴리오 상위 목록을 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularPortfolios(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<Map<String, Object>> popular = portfolioService.getTopPopularPortfolios(limit);
        return ResponseEntity.ok(popular);
    }
}
