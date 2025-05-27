package com.AITemplate.service;

import com.AITemplate.exception.ConflictException;
import com.AITemplate.model.User;
import com.AITemplate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String userId, String username, String email, String password) {
        if (userRepository.findByUserId(userId).isPresent()) {
            throw new ConflictException("이미 존재하는 사용자 ID입니다.");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ConflictException("이미 존재하는 사용자 이름입니다.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("이미 등록된 이메일입니다.");
        }

        // ✅ 비밀번호를 암호화해서 저장
        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .userId(userId)          // 로그인 시 사용하는 사용자 ID
                .username(username)      // 사용자 이름
                .email(email)            // 이메일
                .password(encodedPassword)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
