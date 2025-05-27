package com.AITemplate.controller;

import com.AITemplate.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 API", description = "로그인한 사용자의 정보를 반환하는 API입니다.")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 이름과 메시지를 반환합니다.")
    @GetMapping("/profile")
    public UserProfileResponse getProfile(@AuthenticationPrincipal Object principal) {
        if (principal instanceof UserPrincipal userPrincipal) {
            return new UserProfileResponse(userPrincipal.getDisplayName(), "사용자 프로필 정보입니다.");
        } else if (principal instanceof User user) {
            return new UserProfileResponse(user.getUsername(), "사용자 프로필 정보입니다.");
        } else {
            throw new IllegalStateException("인증된 사용자가 아닙니다.");
        }
    }

    public record UserProfileResponse(String username, String message) {}
}
