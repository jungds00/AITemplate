package com.AITemplate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {

    @Schema(description = "로그인 ID (영문, 고유 식별자)", example = "asdf123")
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    private String userId;

    @Schema(description = "사용자 이름 (표시용 한글 이름)", example = "이름")
    @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
    private String username;

    @Schema(description = "이메일 주소", example = "id@example.com")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

}
