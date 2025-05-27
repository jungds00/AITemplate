package com.AITemplate.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Part;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.List;

@Service
public class GoogleAiService {
    private final VertexAI vertexAI;

    public GoogleAiService() throws IOException {
        this.vertexAI = new VertexAI("spring-archive-453404-j7", "asia-northeast3");
    }

    public String generatePortfolioWithAi(String username, String experience, String skills) {
        try {
            // Generative Model 설정
            GenerativeModel model = new GenerativeModel("gemini-1.5-pro", vertexAI);

            // 역할을 설정하여 Content 생성
            Content promptContent = Content.newBuilder()
                    .setRole("user")
                    .addParts(Part.newBuilder().setText(
                                    String.format("포트폴리오를 생성해주세요.\n이름: %s\n경력: %s\n기술: %s\n결과는 한국어로 제공해주세요.",
                                            username, experience, skills))
                            .build())
                    .build();

            // AI 모델을 사용하여 콘텐츠 생성 요청
            GenerateContentResponse response = model.generateContent(promptContent);

            // 응답 결과에서 첫번째 후보 선택
            List<Candidate> candidates = response.getCandidatesList();
            if (!candidates.isEmpty() && !candidates.get(0).getContent().getPartsList().isEmpty()) {
                // AI 응답을 UTF-8로 변환하여 반환
                return new String(candidates.get(0).getContent().getParts(0).getText().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            } else {
                return "포트폴리오 생성 실패: AI 응답이 없습니다.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "포트폴리오 생성 중 오류 발생: " + e.getMessage();
        }
    }
}
