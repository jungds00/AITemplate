package com.AITemplate.controller;

import com.AITemplate.dto.LoginRequestDto;
import com.AITemplate.dto.RegisterRequestDto;
import com.AITemplate.model.User;
import com.AITemplate.repository.UserRepository;
import com.AITemplate.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증 API", description = "회원가입 및 로그인 관련 API입니다.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "회원가입", description = "userId, 이름, 이메일, 비밀번호, 가입 경로를 입력받아 회원을 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto request) {
        String userId = request.getUserId();
        String email = request.getEmail();

        if (userRepository.findByUserId(userId).isPresent() || userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("이미 사용 중인 ID 또는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .userId(userId)
                .username(request.getUsername())
                .email(email)
                .password(encodedPassword)
                .provider(request.getProvider())
                .build();

        userRepository.save(user);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "로그인", description = "userId와 password를 통해 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(request.getUserId());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
