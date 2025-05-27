package com.AITemplate.security;

import com.AITemplate.model.User;
import com.AITemplate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null) {
            throw new RuntimeException("이메일을 가져올 수 없습니다. Google OAuth 설정을 확인해주세요.");
        }

        // DB에 없으면 저장
        userRepository.findByUserId(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .userId(email)
                                .email(email)
                                .username(name != null ? name : "이름없음")
                                .password("")
                                .provider("google")
                                .build()
                ));

        return new DefaultOAuth2User(
                Collections.singleton(() -> "ROLE_USER"),
                attributes,
                "email"
        );
    }

}
