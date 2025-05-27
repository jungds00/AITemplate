package com.AITemplate.repository;

import com.AITemplate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);  // 로그인 시 영어 ID로 조회
    Optional<User> findByEmail(String email); // 이메일로 조회
    Optional<User> findByUsername(String username); // 이름으로 조회
}
