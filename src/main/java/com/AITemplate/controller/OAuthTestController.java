package com.AITemplate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OAuth 테스트 API", description = "OAuth 인증 성공 후 JWT 토큰을 안내하는 API입니다.")
@RestController
public class OAuthTestController {

    @Operation(
            summary = "OAuth 로그인 성공 후 토큰확인",
            description = """
            Google OAuth2 로그인 성공 시 전달되는 JWT 토큰을 확인할 수 있는 엔드포인트입니다.
            토큰은 URL 파라미터로 제공되며 브라우저에서는 텍스트 형태로 출력됩니다.
            아래 예시 주소에서 token 값을 복사해 API 호출 시 Authorization 헤더에 적용시켜주세요.
            
            http://aitemplate.p-e.kr:8080/oauth2/success?token=발급된토큰
        """
    )
    @GetMapping("/oauth2/success")
    public ResponseEntity<String> displayToken(@RequestParam String token) {
        String message = """
        로그인이 완료되었습니다.
        아래의 JWT 토큰은 인증이 필요한 API 호출시 사용됩니다.

        Bearer %s
        """.formatted(token);

        return ResponseEntity.ok(message);
    }
}
