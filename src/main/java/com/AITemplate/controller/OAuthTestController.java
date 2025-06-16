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
            summary = "OAuth 로그인 성공 후 리디렉션",
            description = """
            Google OAuth2 로그인 후 발급된 JWT 토큰을 확인할 수 있는 엔드포인트입니다.
            브라우저에서 접속하면 토큰이 텍스트형태로 출력되며,
            Swagger에서는 결과값보기가 제한될 수 있습니다.
            
            http://aitemplate.p-e.kr:8080/oauth2/success?token=발급된토큰
        """
    )
    @GetMapping("/oauth-success")
    public ResponseEntity<String> handleOauthRedirect(@RequestParam String token) {
        String message = """
        로그인이 완료되었습니다.
        아래의 JWT 토큰은 인증이 필요한 API 호출시 사용됩니다.

        Bearer %s
        """.formatted(token);

        return ResponseEntity.ok(message);
    }
}
